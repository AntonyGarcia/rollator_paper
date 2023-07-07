import numpy as np
import matplotlib.pyplot as plt
import librosa
from hmmlearn import hmm
import joblib
import json

def calculate_initial_and_final_states(filtered_hidden_states):
    step_starts = []
    step_ends = []
    current_state = filtered_hidden_states[0]
    sample_number = 0
    for i in range(1, len(filtered_hidden_states)):
        sample_number += 1
        if filtered_hidden_states[i] != current_state:
            if filtered_hidden_states[i] == 1.0:
                step_starts.append(sample_number)
            else:
                step_ends.append(sample_number)
            current_state = filtered_hidden_states[i]

    return step_starts, step_ends

# Load the audio file and apply a moving average filter
audio_file = "../resampled_audio_files/cut_sample_16.wav"
y, sr = librosa.load(audio_file)
window_size = 1
weights = np.repeat(1.0, window_size) / window_size
y_smooth = np.convolve(abs(y), weights, 'valid')

# Load the trained model from a file
model_file = "cs_16.pkl"
model = joblib.load(model_file)

# Predict the hidden states for each observation
hidden_states = model.predict(y_smooth.reshape(-1, 1))

# Filter the hidden states
filtered_hidden_states = np.full_like(hidden_states, 1)
current_state = hidden_states[0]
cluster_length = 1
for i in range(1, len(hidden_states)):
    if hidden_states[i] == current_state:
        cluster_length += 1
    else:
        if cluster_length >= 50:
            filtered_hidden_states[i-cluster_length:i] = 0.5
        current_state = hidden_states[i]
        cluster_length = 1

# Create a time axis in seconds
t = np.arange(0, len(y)) / sr
step_starts, step_ends = calculate_initial_and_final_states(filtered_hidden_states)

for i in range(len(step_starts) - 2):
    end = step_ends[i]
    next_start = step_starts[i+1]
    if next_start - end < 8000:
        filtered_hidden_states[end:next_start] = 1

step_starts, step_ends = calculate_initial_and_final_states(filtered_hidden_states)

for start, end in zip(step_starts, step_ends):
    if end-start <= 8000:
        filtered_hidden_states[start:end] = 0

last_step_start = step_starts[-1]
filtered_hidden_states[last_step_start:] = 0

data = []

step_starts, step_ends = calculate_initial_and_final_states(filtered_hidden_states)

for start, end in zip(step_starts, step_ends):
    pair_dict = {"start": start, "end": end}
    data.append(pair_dict)

# Save the data to a JSON file
with open(model_file.replace(".pkl", ".json"), "w") as json_file:
    json.dump(data, json_file)

# Plot the smoothed waveform with the filtered hidden states
plt.plot(y_smooth)
plt.plot(filtered_hidden_states, label="Filtered Hidden States")
plt.xlabel("Time (s)")
plt.ylabel("Amplitude")
plt.show()
