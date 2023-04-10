import csv
import json
import os
import matplotlib.pyplot as plt
import numpy as np

class Interval:
    def __init__(self, cluster, start, end, length, label):
        self.cluster = cluster
        self.start = start
        self.end = end
        self.length = length
        self.label = label

def read_intervals(cluster):
    intervals = []
    with open('intervals.csv', mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        for row in csv_reader:
            if row['cluster'] != f"step_cluster_{cluster}" and row['cluster'] != f"motion_cluster_{cluster}":
                continue
            start = int(row['start'])
            end = int(row['end'])
            length = int(row['length'])
            label = row['label']
            intervals.append(Interval(row['cluster'], start, end, length, label))
    return intervals

def read_data(cluster):
    with open(f'walker_steps/step_cluster_{cluster}.json') as f:
        data = json.load(f)

    # Extract the values for each dimension and timestamp
    acc_x = np.array([d['acc_x'] for d in data])
    acc_y = np.array([d['acc_y'] for d in data])
    acc_z = np.array([d['acc_z'] for d in data])
    gy_x = np.array([d['gy_x'] for d in data])
    gy_y = np.array([d['gy_y'] for d in data])
    gy_z = np.array([d['gy_z'] for d in data])
    timestamps = [d['timestamp'] for d in data]

    # Normalize the accelerometer and gyroscope data to a range of 0 to 1
    acc_norm = (np.linalg.norm(np.vstack([acc_x, acc_y, acc_z]), axis=0) - np.min(np.abs(acc_x+acc_y+acc_z)))/np.ptp(np.abs(acc_x+acc_y+acc_z))
    gy_norm = (np.linalg.norm(np.vstack([gy_x, gy_y, gy_z]), axis=0) - np.min(np.abs(gy_x+gy_y+gy_z)))/np.ptp(np.abs(gy_x+gy_y+gy_z))

    # Combine the normalized accelerometer and gyroscope data into a single metric of motion using RMS
    motion = np.sqrt((acc_norm ** 2 + gy_norm ** 2) / 2)

    # Apply a running average with a 50-sample window
    window_size = 60
    weights = np.repeat(1.0, window_size) / window_size
    motion_smooth = np.convolve(motion, weights, 'same')

    return timestamps, motion_smooth


def plot_intervals(cluster):
    intervals = read_intervals(cluster)
    timestamps, motion = read_data(cluster)

    color_map = {'step': 'red', 'motion': 'black'}

    for interval in intervals:
        start = interval.start
        end = interval.end
        color = color_map[interval.label]
        label = interval.label
        plt.axvline(x=start, color=color, linestyle='--', label=label)
        plt.axvline(x=end, color=color, linestyle='--')

    plt.plot(timestamps, motion, color='blue', label='Motion')

    plt.title(f'Cluster {cluster}')
    plt.xlabel('Time (s)')
    plt.ylabel('Motion (RMS)')
    plt.legend()
    plt.show()


plot_intervals(2)
