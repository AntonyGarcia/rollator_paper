import os
import json

def process_json_files(path, output_file):
    data = {}  # Dictionary to store data from all JSON files

    for root, dirs, files in os.walk(path):
        for file_name in files:
            if file_name.endswith('.json'):
                # Open the JSON file
                with open(os.path.join(root, file_name), 'r') as json_file:
                    try:
                        # Load the JSON data
                        json_data = json.load(json_file)
                        # Add the loaded data to the main dictionary
                        data[file_name.replace("cs_","cut_sample_").replace("json","wav")] = json_data
                    except json.JSONDecodeError as e:
                        print(f"Error decoding {file_name}: {str(e)}")

    # Write the main dictionary to the output file
    with open(output_file, 'w') as output_json_file:
        json.dump(data, output_json_file)

    print(f"Created {output_file}")

# Provide the path to the directory containing the JSON files
directory_path = 'trained_models'

# Provide the desired name for the output JSON file
output_file = 'output.json'

# Process the JSON files in the directory and create the output file
process_json_files(directory_path, output_file)
