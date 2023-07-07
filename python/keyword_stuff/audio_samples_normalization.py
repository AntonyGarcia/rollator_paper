import json
import random
import os

# Specify the path to the directory containing the JSON files
directory_path = 'extracted_positive_samples'

# Define the desired total sample count
desired_sample_count = 48000

# Iterate over the files in the directory
for filename in os.listdir(directory_path):
    if filename.endswith('.json'):  # Process only JSON files
        file_path = os.path.join(directory_path, filename)

        # Read the JSON file
        with open(file_path) as file:
            json_data = json.load(file)

        # Extract the start and end sample values
        start_sample = json_data['start_sample']
        end_sample = json_data['end_sample']

        # Calculate the current sample count
        current_sample_count = end_sample - start_sample

        # Calculate the number of additional samples needed
        additional_sample_count = desired_sample_count - current_sample_count

        # Calculate the number of samples to add before and after the audio sample
        samples_before = additional_sample_count // 2
        samples_after = additional_sample_count - samples_before

        # Generate white noise samples for before and after
        white_noise_before = [random.uniform(-0.0001, 0.0001) for _ in range(samples_before)]
        white_noise_after = [random.uniform(-0.0001, 0.0001) for _ in range(samples_after)]

        # Extend the data array with white noise samples
        json_data['data'] = white_noise_before + json_data['data'] + white_noise_after

        # Update the start and end sample values
        json_data['start_sample'] = start_sample - samples_before
        json_data['end_sample'] = end_sample + samples_after

        # Save the modified JSON data back to the file, overwriting it
        with open(file_path, 'w') as file:
            json.dump(json_data, file, indent=4)

        print(f"Modified file: {filename}")

print("All JSON files have been modified and overwritten.")
