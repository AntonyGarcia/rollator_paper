import json
import csv
import os
import random

# Define the folders containing the JSON files for positive and negative samples
positive_folders = ['extracted_positive_samples', 'mixed_positive_samples']
negative_folders = ['extracted_negative_samples', 'extracted_natural_samples']

# Define the output CSV file path
output_file = 'final_datasets/dataset.csv'

# Define the label for positive and negative samples
positive_label = 'fall'
negative_label = 'no_fall'

# Define the number of data columns (features)
num_data_columns = 48000

# Define the number of positive samples
num_positive_samples = 724

# Open the output CSV file in write mode
with open(output_file, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)

    # Process positive JSON files
    for folder in positive_folders:
        folder_path = os.path.join('.', folder)

        # Iterate over each JSON file in the folder
        for filename in os.listdir(folder_path):
            if filename.endswith('.json'):
                file_path = os.path.join(folder_path, filename)

                # Load JSON data from the file
                with open(file_path, 'r') as jsonfile:
                    data = json.load(jsonfile)

                # Extract 'data' field from the JSON and flatten it
                data_values = data['data'][:num_data_columns]

                # Write a row to the CSV file for positive sample
                row = [positive_label] + data_values
                writer.writerow(row)

    # Collect all non-empty negative samples
    negative_samples = []
    for folder in negative_folders:
        folder_path = os.path.join('.', folder)
        files = os.listdir(folder_path)

        # Iterate over each JSON file in the folder
        for filename in files:
            if filename.endswith('.json'):
                file_path = os.path.join(folder_path, filename)

                # Load JSON data from the file
                with open(file_path, 'r') as jsonfile:
                    data = json.load(jsonfile)

                # Extract 'data' field from the JSON and flatten it
                data_values = data['data'][:num_data_columns]

                # Append the data values to the negative samples list if non-empty
                if data_values:
                    negative_samples.append(data_values)

    # Shuffle the negative samples
    random.shuffle(negative_samples)

    # Adjust the number of negative samples if there are more than the positive samples
    num_negative_samples = min(len(negative_samples), num_positive_samples)

    # Write rows to the CSV file for negative samples
    for data_values in negative_samples[:num_negative_samples]:
        row = [negative_label] + data_values
        writer.writerow(row)

print('CSV file exported successfully.')

# Define the input CSV file path
input_file = 'final_datasets/dataset.csv'

# Define the training and testing CSV file paths
train_file = 'final_datasets/train.csv'
test_file = 'final_datasets/test.csv'

# Read the data from the CSV file
data = []
with open(input_file, 'r') as csvfile:
    reader = csv.reader(csvfile)
    for row in reader:
        data.append(row)

# Shuffle the data
random.shuffle(data)

# Calculate the index to split the data
split_index = int(0.8 * len(data))

# Split the data into training and testing sets
train_data = data[:split_index]
test_data = data[split_index:]

# Write the training data to the train CSV file
with open(train_file, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    for row in train_data:
        writer.writerow(row)

# Write the testing data to the test CSV file
with open(test_file, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    for row in test_data:
        writer.writerow(row)

# Write the shuffled data to the shuffled dataset CSV file
with open(input_file, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    for row in data:
        writer.writerow(row)

print('Data split into training and testing sets successfully.')
