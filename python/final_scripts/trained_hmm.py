import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import joblib, json, math

# Load the saved model
filename = 'trained_model_.joblib'
loaded_model = joblib.load(filename)

# Load data from the JSON file
with open("resampled_data.json") as json_file:
    data = json.load(json_file)

# Calculate the RMS value for each sample
magnitudes = []
for record in data:
    acc_squared = record["acc_x"]**2 + record["acc_y"]**2 + record["acc_z"]**2
    gy_squared = record["gy_x"]**2 + record["gy_y"]**2 + record["gy_z"]**2
    magnitude = math.sqrt((acc_squared + gy_squared) / 6)
    magnitudes.append(magnitude)

# Apply a running average filter to the magnitude data
window_size = 50
filtered_magnitudes = []
for i in range(len(magnitudes) - window_size + 1):
    window = magnitudes[i:i+window_size]
    filtered_magnitude = sum(window) / window_size
    filtered_magnitudes.append(filtered_magnitude)

# Extract the "Value" column as the observations for the HMM
observations = np.array(filtered_magnitudes).reshape(-1, 1)

# Predict the hidden states for each observation using the loaded model
hidden_states = loaded_model.predict(observations)

# Filter the hidden states
filtered_hidden_states = np.full_like(hidden_states, 0)
current_state = hidden_states[0]
cluster_length = 1
for i in range(1, len(hidden_states)):
    if hidden_states[i] == current_state:
        cluster_length += 1
    else:
        if cluster_length >=100:
            filtered_hidden_states[i-cluster_length:i] = 5
        current_state = hidden_states[i]
        cluster_length = 1

# Print the filtered hidden states
print("Filtered Hidden States:", filtered_hidden_states)

