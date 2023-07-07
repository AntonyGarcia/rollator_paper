import os
import librosa
import json

# Path to the audio file
audio_path = 'resampled_audio_files/natural.wav'

# Duration of each subsample in seconds
subsample_duration = 3

# Number of subsamples to extract
num_subsamples = 2000

# Load the audio file using librosa
audio, sr = librosa.load(audio_path, sr=16000)

# Calculate the total number of samples in each subsample
subsample_length = int(subsample_duration * sr)

# Create the directory to store the subsample files if it doesn't exist
output_dir = 'extracted_natural_samples'
os.makedirs(output_dir, exist_ok=True)

# Extract subsamples
for i in range(num_subsamples):
    # Calculate the start and end indices of each subsample
    start = i * subsample_length
    end = start + subsample_length

    # Extract the subsample from the audio
    subsample = audio[start:end].tolist()

    # Create a dictionary with the subsample data
    data = {"data": subsample}

    # Convert the dictionary to JSON format
    json_data = json.dumps(data)

    # Save the JSON data to a separate file
    filename = f'subsample_{i}.json'
    filepath = os.path.join(output_dir, filename)
    with open(filepath, 'w') as file:
        file.write(json_data)
