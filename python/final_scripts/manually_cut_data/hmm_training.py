import json
import matplotlib.pyplot as plt
from datetime import datetime
import numpy as np
from hmmlearn import hmm
import joblib

cluster = 28

# Load the combined JSON data
with open('falls/data_falls_4.json') as f:
    data = json.load(f)

# Extract the values for each dimension and timestamp
acc_x = np.array([d['acc_x'] for d in data])
acc_y = np.array([d['acc_y'] for d in data])
acc_z = np.array([d['acc_z'] for d in data])
gy_x = np.array([d['gy_x'] for d in data])
gy_y = np.array([d['gy_y'] for d in data])
gy_z = np.array([d['gy_z'] for d in data])
timestamps = [d['timestamp'] for d in data]

# Normalize the accelerometer and gyroscope data to a range of 0 to 1
acc_norm = (np.linalg.norm(np.vstack([acc_x, acc_y, acc_z]), axis=0) - np.min(np.abs(acc_x+acc_y+acc_z)))/np.ptp(np.abs(acc_x+acc_y+acc_z))
gy_norm = (np.linalg.norm(np.vstack([gy_x, gy_y, gy_z]), axis=0) - np.min(np.abs(gy_x+gy_y+gy_z)))/np.ptp(np.abs(gy_x+gy_y+gy_z))

# Combine the normalized accelerometer and gyroscope data into a single metric of motion using RMS
motion = np.sqrt((acc_norm ** 2 + gy_norm ** 2) / 2)

# Apply a running average with a 50-sample window
window_size = 60
weights = np.repeat(1.0, window_size) / window_size
motion_smooth = np.convolve(motion, weights, 'valid')

# Convert the timestamps to a more human-readable format
# This assumes the timestamps are in Unix timestamp format
times = [datetime.fromtimestamp(float(t)) for t in timestamps[window_size-1:]]

n_states =50

# Initialize the HMM model
model = hmm.GaussianHMM(n_components=n_states, covariance_type="full")

# Fit the HMM model to the observations
model.fit(motion_smooth.reshape(-1, 1))

# Predict the hidden states for each observation
hidden_states = model.predict(motion_smooth.reshape(-1, 1))

# Filter the hidden states
filtered_hidden_states = np.full_like(hidden_states, 2)
current_state = hidden_states[0]
cluster_length = 1
for i in range(1, len(hidden_states)):
    if hidden_states[i] == current_state:
        cluster_length += 1
    else:
        if cluster_length >= 80:
            filtered_hidden_states[i-cluster_length:i] = 3
        current_state = hidden_states[i]
        cluster_length = 1

# Print the filtered hidden states
print("Filtered Hidden States:", filtered_hidden_states)
print("Number of States:", model.n_components)

# Save the model
filename = 'cluster_'+str(cluster)+'.joblib'
joblib.dump(model, filename)

# Load the saved model
loaded_model = joblib.load(filename)

# Plot the observations and filtered hidden states
fig, ax = plt.subplots()
ax.plot((motion_smooth*10)+1.5, label="Observations")
ax.plot(filtered_hidden_states, label="Filtered Hidden States")
ax.legend()
ax.set_xlabel("Time")
ax.set_ylabel("Value")
plt.show()






