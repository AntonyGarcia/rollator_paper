import os
import json

folder_path = 'extracted_positive_samples'  # Replace with the actual folder path

# Initialize variables to store the highest and lowest values
highest_value = float('-inf')
lowest_value = float('inf')

# Iterate over all files in the folder
for filename in os.listdir(folder_path):
    if filename.endswith('.json'):  # Process only JSON files
        file_path = os.path.join(folder_path, filename)

        with open(file_path, 'r') as file:
            data = json.load(file)

            # Extract the 'data' field from the JSON
            json_data = data['data']

            # Find the highest and lowest values in the 'data' list
            max_value = max(json_data)
            min_value = min(json_data)

            # Update the highest and lowest values if necessary
            if max_value > highest_value:
                highest_value = max_value
            if min_value < lowest_value:
                lowest_value = min_value

# Calculate the scaling factor
scaling_factor = 25000 / highest_value

# Iterate over all files again to perform the mapping
for filename in os.listdir(folder_path):
    if filename.endswith('.json'):
        file_path = os.path.join(folder_path, filename)

        with open(file_path, 'r+') as file:
            data = json.load(file)

            # Extract the 'data' field from the JSON
            json_data = data['data']

            # Map the values using the scaling factor and round to 4 decimal places
            mapped_data = [round(value * scaling_factor, 2) for value in json_data]

            # Update the 'data' field with the mapped values
            data['data'] = mapped_data

            # Write the updated JSON back to the file
            file.seek(0)
            json.dump(data, file, indent=4)
            file.truncate()

import os
import json

folder_path = 'extracted_positive_samples'  # Replace with the actual folder path

# Initialize variables to store the highest and lowest values
highest_value = float('-inf')
lowest_value = float('inf')

# Iterate over all files in the folder
for filename in os.listdir(folder_path):
    if filename.endswith('.json'):  # Process only JSON files
        file_path = os.path.join(folder_path, filename)

        with open(file_path, 'r') as file:
            data = json.load(file)

            # Extract the 'data' field from the JSON
            json_data = data['data']

            # Find the highest and lowest values in the 'data' list
            max_value = max(json_data)
            min_value = min(json_data)

            # Update the highest and lowest values if necessary
            if max_value > highest_value:
                highest_value = max_value
            if min_value < lowest_value:
                lowest_value = min_value

# Print the highest and lowest values
print(f'Highest value: {highest_value}')
print(f'Lowest value: {lowest_value}')

