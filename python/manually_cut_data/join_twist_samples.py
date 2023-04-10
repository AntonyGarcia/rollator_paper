import os
import json

# Define the directory path where the JSON files are located
directory_path = 'walker_twist'

# Initialize an empty list to store the data from each file
data = []

# Loop through each file in the directory
for filename in os.listdir(directory_path):
    if filename.endswith('.json'):
        # Open the file and load its contents as JSON
        with open(os.path.join(directory_path, filename)) as f:
            file_data = json.load(f)
        # Add the data from the file to the overall data list
        data.extend(file_data)

# Write the combined data to a new JSON file
with open('walker_twist/combined_data.json', 'w') as f:
    json.dump(data, f)
