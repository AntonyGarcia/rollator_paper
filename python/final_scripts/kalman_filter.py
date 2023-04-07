import json
import math
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from hmmlearn import hmm
import joblib

# Load data from the JSON file
with open("resampled_recorded_data/walk_1.json") as json_file:
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

'''
# Create a line chart to visualize the filtered magnitude data
plt.plot(filtered_magnitudes)
plt.xlabel("Sample Number")
plt.ylabel("Filtered RMS Value")
plt.title("Filtered RMS Value of Acceleration and Gyro Data")
plt.show()
'''

observations = np.array(filtered_magnitudes).reshape(-1, 1)

n_states = 25

# Initialize the HMM model
model = hmm.GaussianHMM(n_components=n_states, covariance_type="full")

# Fit the HMM model to the observations
model.fit(observations)

# Predict the hidden states for each observation
hidden_states = model.predict(observations)

# Filter the hidden states
filtered_hidden_states = np.full_like(hidden_states, -1)
current_state = hidden_states[0]
cluster_length = 1
for i in range(1, len(hidden_states)):
    if hidden_states[i] == current_state:
        cluster_length += 1
    else:
        if cluster_length >=100:
            filtered_hidden_states[i-cluster_length:i] = 3
        current_state = hidden_states[i]
        cluster_length = 1

# Print the filtered hidden states
print("Filtered Hidden States:", filtered_hidden_states)
print("Number of States:", model.n_components)

# Save the model
filename = 'trained_hmm_model.joblib'
joblib.dump(model, filename)

# Load the saved model
loaded_model = joblib.load(filename)

# Plot the observations and filtered hidden states
fig, ax = plt.subplots()
ax.plot(observations, label="Observations")
ax.plot(filtered_hidden_states, label="Filtered Hidden States")
ax.legend()
ax.set_xlabel("Time")
ax.set_ylabel("Value")
plt.show()