import json
from datetime import datetime
import numpy as np
import joblib
import os
import csv
import random

import shutil


window_size=160

class Interval:
    def __init__(self, cluster, start, end, length, label):
        self.cluster = cluster
        self.start = start
        self.end = end
        self.length = length
        self.label = label


def load_data(cluster):
    with open(f'walker_steps/step_cluster_{cluster}.json') as f:
        data = json.load(f)

    acc_x = np.array([d['acc_x'] for d in data])
    acc_y = np.array([d['acc_y'] for d in data])
    acc_z = np.array([d['acc_z'] for d in data])
    gy_x = np.array([d['gy_x'] for d in data])
    gy_y = np.array([d['gy_y'] for d in data])
    gy_z = np.array([d['gy_z'] for d in data])
    timestamps = [d['timestamp'] for d in data]

    acc_norm = (np.linalg.norm(np.vstack([acc_x, acc_y, acc_z]), axis=0) - np.min(
        np.abs(acc_x + acc_y + acc_z))) / np.ptp(np.abs(acc_x + acc_y + acc_z))
    gy_norm = (np.linalg.norm(np.vstack([gy_x, gy_y, gy_z]), axis=0) - np.min(np.abs(gy_x + gy_y + gy_z))) / np.ptp(
        np.abs(gy_x + gy_y + gy_z))
    motion = np.sqrt((acc_norm ** 2 + gy_norm ** 2) / 2)

    window_size = 60
    weights = np.repeat(1.0, window_size) / window_size
    motion_smooth = np.convolve(motion, weights, 'same')

    times = [datetime.fromtimestamp(float(t)) for t in timestamps[window_size - 1:]]

    return motion_smooth


def load_model(cluster):
    filename = f'trained_hmm_model/cluster_{cluster}.joblib'
    loaded_model = joblib.load(filename)
    return loaded_model


def predict_states(model, observations):
    hidden_states = model.predict(observations)
    return hidden_states


def filter_states(hidden_states):
    filtered_hidden_states = np.full_like(hidden_states, 2)
    current_state = hidden_states[0]
    cluster_length = 1
    for i in range(1, len(hidden_states)):
        if hidden_states[i] == current_state:
            cluster_length += 1
        else:
            if cluster_length >= 50:
                filtered_hidden_states[i - cluster_length:i] = 4
            current_state = hidden_states[i]
            cluster_length = 1
    output_states = np.zeros_like(filtered_hidden_states)
    output_states[filtered_hidden_states == 2] = 1
    return output_states


def identify_intervals(output_states, cluster):
    intervals = []
    current_interval = None
    for i in range(len(output_states)):
        if output_states[i] == 1:
            if current_interval is None:
                current_interval = Interval(f"step_cluster_{cluster}", i, i, 1, "step")
            else:
                current_interval.end = i
                current_interval.length += 1
        else:
            if current_interval is not None:
                intervals.append(current_interval)
                current_interval = None
    if current_interval is not None:
        intervals.append(current_interval)
    intervals = [interval for interval in intervals if interval.length >= 60 and interval.length <= 250 and interval.start>0]
    return intervals


def write_intervals_to_csv(intervals, filename):
    with open(filename, mode='w', newline='') as csv_file:
        writer = csv.writer(csv_file, lineterminator='\r\n')
        writer.writerow(['cluster', 'start', 'end', 'length', 'label'])
        for interval in intervals:
            writer.writerow([interval.cluster, interval.start, interval.end, interval.length, interval.label])


import os
import shutil

def extract_steps(intervals, prefix):
    if not os.path.exists('extracted_steps'):
        os.makedirs('extracted_steps')

    existing_files = os.listdir('extracted_steps')
    existing_numbers = set()
    for filename in existing_files:
        if filename.startswith(prefix + '_'):
            number = int(filename.split('_')[-1].split('.')[0])
            existing_numbers.add(number)

    all_step_data = []
    i = 1
    for interval in intervals:
        cluster = interval.cluster
        start = interval.start
        end = interval.end
        with open(f'walker_steps/{cluster}.json') as f:
            data = json.load(f)
        acc_x = [d['acc_x'] for d in data[start:end]]
        acc_y = [d['acc_y'] for d in data[start:end]]
        acc_z = [d['acc_z'] for d in data[start:end]]
        gy_x = [d['gy_x'] for d in data[start:end]]
        gy_y = [d['gy_y'] for d in data[start:end]]
        gy_z = [d['gy_z'] for d in data[start:end]]
        timestamps = [d['timestamp'] for d in data[start:end]]
        step_data = []
        for j in range(len(acc_x)):
            sample_data = {
                'acc_x': acc_x[j],
                'acc_y': acc_y[j],
                'acc_z': acc_z[j],
                'gy_x': gy_x[j],
                'gy_y': gy_y[j],
                'gy_z': gy_z[j],
                'timestamp': timestamps[j]
            }
            step_data.append(sample_data)

        # Generate a new file name if the existing file already exists
        while i in existing_numbers:
            i += 1

        with open(f'extracted_steps/{prefix}_{i}.json', 'w') as f:
            json.dump(step_data, f)
        all_step_data.append(step_data)
        i += 1

    return all_step_data


def modify_intervals(intervals):
    modified_intervals = []
    step_threshold = window_size
    for interval in intervals:
        center = (interval.start + interval.end) // 2
        start = center - (step_threshold // 2)
        end = center + (step_threshold // 2)
        length = end - start
        modified_interval = Interval(interval.cluster, start, end, length, interval.label)
        modified_intervals.append(modified_interval)
    return modified_intervals

def clear_extracted_steps():
    samples_dir = 'extracted_steps'
    for filename in os.listdir(samples_dir):
        file_path = os.path.join(samples_dir, filename)
        try:
            os.remove(file_path)
        except Exception as e:
            print(f"Error while deleting file: {file_path}")
            print(e)


def extract_idle_samples():
    data_dir = '../resampled_recorded_data'
    samples_dir = 'extracted_steps'
    sample_size = window_size

    if not os.path.exists(samples_dir):
        os.makedirs(samples_dir)

    i = 1
    for filename in os.listdir(data_dir):
        if filename.startswith('iddle_'):
            with open(os.path.join(data_dir, filename)) as f:
                data = json.load(f)
            for j in range(0, len(data) - sample_size + 1, sample_size):
                sample_data = data[j:j + sample_size]
                sample_prefix = "iddle"
                sample_filename = f"{sample_prefix}_{i}.json"

                # Check if the file already exists in samples_dir
                while os.path.exists(os.path.join(samples_dir, sample_filename)):
                    i += 1
                    sample_filename = f"{sample_prefix}_{i}.json"

                with open(os.path.join(samples_dir, sample_filename), 'w') as f:
                    json.dump(sample_data, f)
                i += 1


def create_raw_dataset():
    with open('final_datasets/raw_dataset.csv', mode='w', newline='') as csv_file:
        writer = csv.writer(csv_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        writer.writerow(['label', 'timestamp'] + ['acc_x_{}'.format(i) for i in range(window_size)] + ['acc_y_{}'.format(i) for i in range(window_size)] + ['acc_z_{}'.format(i) for i in range(window_size)] + ['gy_x_{}'.format(i) for i in range(window_size)] + ['gy_y_{}'.format(i) for i in range(window_size)] + ['gy_z_{}'.format(i) for i in range(window_size)])

        # count number of samples in each category
        n_idle = len([f for f in os.listdir('extracted_steps') if f.startswith('iddle_')])
        n_motion = len([f for f in os.listdir('extracted_steps') if f.startswith('motion_')])
        n_step = len([f for f in os.listdir('extracted_steps') if f.startswith('step_')])

        # select the minimum number of samples from each category
        min_samples = min(n_idle, n_motion, n_step)

        # randomly sample the selected number of samples from each category
        idle_samples = random.sample([f for f in os.listdir('extracted_steps') if f.startswith('iddle_')], min_samples)
        motion_samples = random.sample([f for f in os.listdir('extracted_steps') if f.startswith('motion_')], min_samples)
        step_samples = random.sample([f for f in os.listdir('extracted_steps') if f.startswith('step_')], min_samples)

        # process selected files
        for filename in idle_samples + motion_samples + step_samples:
            with open(os.path.join('extracted_steps', filename)) as f:
                data = json.load(f)

            try:
                timestamp = data[0]['timestamp']
            except KeyError:
                timestamp = data[0]['Timestamp']
            acc_x = [d['acc_x'] for d in data]
            acc_y = [d['acc_y'] for d in data]
            acc_z = [d['acc_z'] for d in data]
            gy_x = [d['gy_x'] for d in data]
            gy_y = [d['gy_y'] for d in data]
            gy_z = [d['gy_z'] for d in data]

            if filename.startswith('step_'):
                label = 'step'
            elif filename.startswith('iddle_'):
                label = 'idle'
            else:
                label = 'motion'

            writer.writerow([label, timestamp] + acc_x + acc_y + acc_z + gy_x + gy_y + gy_z)


def shuffle_and_split_dataset(input_file, train_file, test_file, train_ratio=0.8, shuffle_seed=42):
    # Load input CSV file
    with open(input_file, 'r') as csvfile:
        datareader = csv.reader(csvfile)
        header = next(datareader)
        data = list(datareader)

    # Shuffle rows with a fixed seed for reproducibility
    random.seed(shuffle_seed)
    random.shuffle(data)

    # Split shuffled data into training and testing datasets
    split_index = int(train_ratio * len(data))
    train_data = data[:split_index]
    test_data = data[split_index:]

    # Write shuffled and split datasets to output files
    if not os.path.exists('final_datasets'):
        os.makedirs('final_datasets')

    with open(train_file, 'w', newline='') as csvfile:
        datawriter = csv.writer(csvfile)
        datawriter.writerow(header)
        datawriter.writerows(train_data)

    with open(test_file, 'w', newline='') as csvfile:
        datawriter = csv.writer(csvfile)
        datawriter.writerow(header)
        datawriter.writerows(test_data)


def extract_motion_samples(original_intervals):
    motion_intervals = []
    clusters = set()
    for interval in original_intervals:
        clusters.add(interval.cluster)
    num_clusters = len(clusters)

    for cluster in clusters:
        cluster_intervals = [interval for interval in original_intervals if interval.cluster == cluster]
        for i in range(len(cluster_intervals)-1):
                start_1 = cluster_intervals[i].start
                end_1 = cluster_intervals[i].end
                start_2 = cluster_intervals[i + 1].start
                end_2 = cluster_intervals[i + 1].end
                centroid_1 = ((end_1-start_1)//2)+start_1
                centroid_2 = ((end_2 - start_2) // 2) + start_2
                middle_sample_centroid = ((centroid_2-centroid_1)//2)+centroid_1
                middle_sample_start = middle_sample_centroid-(window_size//2)
                middle_sample_end = middle_sample_centroid+(window_size//2)
                interval = Interval(cluster, middle_sample_start, middle_sample_end, window_size, "motion")
                motion_intervals.append(interval)

    return motion_intervals


def copy_files(src_path, dest_path):
    for filename in os.listdir(src_path):
        src_file = os.path.join(src_path, filename)
        dest_file = os.path.join(dest_path, filename)
        shutil.copy(src_file, dest_file)


def main():
    clear_extracted_steps()


    all_intervals = []
    original_intervals=[]
    for filename in os.listdir('walker_steps'):
        if filename.startswith('step_cluster_'):
            cluster = filename.split('_')[-1].split('.')[0]
            motion_smooth = load_data(cluster)
            loaded_model = load_model(cluster)
            observations = np.array(motion_smooth).reshape(-1, 1)
            hidden_states = predict_states(loaded_model, observations)
            output_states = filter_states(hidden_states)
            intervals = identify_intervals(output_states, cluster)
            original_intervals+=intervals
            intervals = modify_intervals(intervals)
            all_intervals += intervals


    overall_intervals = original_intervals
    motion = extract_motion_samples(original_intervals)
    overall_intervals+=motion
    write_intervals_to_csv(overall_intervals, 'intervals.csv')
    copy_files("added_steps","extracted_steps")
    copy_files("added_steps", "extracted_steps")
    extract_idle_samples()
    extract_steps(all_intervals, "step")
    extract_steps(motion,"motion")

    create_raw_dataset()
    input_file = 'final_datasets/raw_dataset.csv'
    train_file = 'final_datasets/train.csv'
    test_file = 'final_datasets/test.csv'
    shuffle_and_split_dataset(input_file, train_file, test_file, train_ratio=0.8, shuffle_seed=42)


if __name__ == '__main__':
    main()