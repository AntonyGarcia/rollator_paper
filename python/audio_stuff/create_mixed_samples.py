import os
import random
import json
import soundfile as sf
import numpy as np

# Folder paths
positive_samples_folder = "extracted_positive_samples"
negative_samples_folder = "extracted_negative_samples"
output_sound_folder = "mixed_sound_samples"
output_json_folder = "mixed_positive_samples"

# Read folder contents
positive_files = os.listdir(positive_samples_folder)
negative_files = os.listdir(negative_samples_folder)

# Loop through each positive sample
for positive_file in positive_files:
    # Construct the file path for the positive sample
    positive_path = os.path.join(positive_samples_folder, positive_file)

    # Load contents of the positive sample JSON file
    with open(positive_path, 'r') as positive_sample:
        positive_data = json.load(positive_sample)

    # Select a random negative sample
    negative_file = random.choice(negative_files)
    negative_path = os.path.join(negative_samples_folder, negative_file)

    # Load contents of the negative sample JSON file
    with open(negative_path, 'r') as negative_sample:
        negative_data = json.load(negative_sample)

    # Mix audio data
    mixed_audio = np.array(positive_data["data"]) + np.array(negative_data["data"])

    # Set the output audio file path with a unique name
    output_audio_file = f"{os.path.splitext(positive_file)[0]}_{os.path.splitext(negative_file)[0]}_mixed.wav"
    output_audio_path = os.path.join(output_sound_folder, output_audio_file)

    # Save the mixed audio as a WAV file
    sf.write(output_audio_path, mixed_audio, 16000)

    print(f"Audio file saved: {output_audio_path}")

    # Construct the output JSON file path
    output_json_file = f"{os.path.splitext(positive_file)[0]}_{os.path.splitext(negative_file)[0]}_mixed.json"
    output_json_path = os.path.join(output_json_folder, output_json_file)

    # Create a new JSON object for the mixed data
    mixed_data = {
        "data": mixed_audio.tolist()
    }

    # Save the mixed data as a JSON file
    with open(output_json_path, 'w') as output_json:
        json.dump(mixed_data, output_json)

    print(f"JSON file saved: {output_json_path}")
