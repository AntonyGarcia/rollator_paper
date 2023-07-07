import json
import librosa
import os
import csv

# Directory for storing the subsample files
subsample_directory = "extracted_samples"

# Load the JSON file
with open('output.json') as f:
    data = json.load(f)

# Count the existing subsample files
existing_files = len([f for f in os.listdir(subsample_directory) if os.path.isfile(os.path.join(subsample_directory, f))])

# Create a list to store subsample_data dictionaries
subsample_data_list = []

# Iterate over the file names and dictionaries
for file_name, dictionaries in data.items():
    y, sr = librosa.load("resampled_audio_files/" + file_name)

    # Iterate over the dictionaries and create subsamples
    for dictionary in dictionaries:
        start_sample = dictionary['start']
        end_sample = dictionary['end']
        if (end_sample-start_sample>8000) and (end_sample-start_sample<48000):
            subsample = y[start_sample:end_sample]

            # Convert subsample to a list of floats
            subsample = subsample.tolist()

            # Convert subsample to JSON format
            subsample_data = {
                'file_name': file_name,
                'start_sample': start_sample,
                'end_sample': end_sample,
                'data': list(subsample)
            }

            subsample_data_2 = {
                'file_name': file_name,
                'start_sample': start_sample,
                'end_sample': end_sample
            }

            # Append subsample_data to the list
            subsample_data_list.append(subsample_data_2)

            # Create a JSON file for each subsample with a unique name
            subsample_file_name = f"fall_subsample_{existing_files + 1}.json"
            subsample_file_path = os.path.join(subsample_directory, subsample_file_name)
            with open(subsample_file_path, 'w') as outfile:
                json.dump(subsample_data, outfile)

            print(f"Subsample created: {subsample_file_path}")

            # Increment the count of existing files
            existing_files += 1

# Export subsample_data_list to a CSV file
csv_file_path = 'subsample_data.csv'
fieldnames = ['file_name', 'start_sample', 'end_sample']

with open(csv_file_path, 'w', newline='') as csvfile:
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(subsample_data_list)

print(f"CSV file created: {csv_file_path}")
