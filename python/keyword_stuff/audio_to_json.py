import json
import librosa
import resampy

# Load the audio file
audio_file = "resampled_audio_files/sample_2_resampled.wav"
y, sr = librosa.load(audio_file, sr=16000)

# Convert the audio data to a list
audio_data = y.tolist()

# Create a dictionary to store the audio data and sample rate
data_dict =  audio_data


# Write the data to a JSON file
with open("json_audio_files/audio_sample_2.json", "w") as f:
    json.dump(data_dict, f)
