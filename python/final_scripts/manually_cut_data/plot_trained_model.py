import json
import matplotlib.pyplot as plt
from datetime import datetime
import numpy as np
import joblib
cluster = 23

class Interval:
    def __init__(self, start, end, length):
        self.start = start
        self.end = end
        self.length = length

# Load the combined JSON data
with open('walker_steps/step_cluster_'+str(cluster)+'.json') as f:
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
motion_smooth = np.convolve(motion, weights, 'same')

# Convert the timestamps to a more human-readable format
# This assumes the timestamps are in Unix timestamp format
times = [datetime.fromtimestamp(float(t)) for t in timestamps[window_size-1:]]

# Load the saved model
filename = 'trained_hmm_model/cluster_'+str(cluster)+'.joblib'
loaded_model = joblib.load(filename)

# Extract the "Value" column as the observations for the HMM
observations = np.array(motion_smooth).reshape(-1, 1)

# Predict the hidden states for each observation using the loaded model
hidden_states = loaded_model.predict(observations)

# Filter the hidden states
filtered_hidden_states = np.full_like(hidden_states, 2)
current_state = hidden_states[0]
cluster_length = 1
for i in range(1, len(hidden_states)):
    if hidden_states[i] == current_state:
        cluster_length += 1
    else:
        if cluster_length >=50:
            filtered_hidden_states[i-cluster_length:i] = 4
        current_state = hidden_states[i]
        cluster_length = 1

# Initialize an array of zeros with the same length as filtered_hidden_states
output_states = np.zeros_like(filtered_hidden_states)

# Set the values of output_states to 1 where filtered_hidden_states is equal to 4
output_states[filtered_hidden_states == 2] = 1

intervals = []
current_interval = None
for i in range(len(output_states)):
    if output_states[i] == 1:
        if current_interval is None:
            current_interval = Interval(i, i, 1)
        else:
            current_interval.end = i
            current_interval.length += 1
    else:
        if current_interval is not None:
            intervals.append(current_interval)
            current_interval = None
if current_interval is not None:
    intervals.append(current_interval)


intervals = [interval for interval in intervals if interval.length >= 60]
intervals = intervals[1:-1]

for i in range(len(output_states)):
    output_states[i]=0

'''
for interval in intervals:
    output_states[interval.start:interval.end + 1] = 1
    
'''

for interval in intervals:
    center = (interval.start + interval.end) // 2  # Calculate the center of the interval
    start = center - 40  # Calculate the start index of the modified interval
    end = center + 40  # Calculate the end index of the modified interval
    length = end - start + 1  # Calculate the length of the modified interval

    # Modify the interval to have 200 steps centered around the middle point
    interval.start = start
    interval.end = end
    interval.length = length

    # Set the values of output_states for the modified interval to 1
    output_states[start:end+1] = 1

print(len(output_states))
print(len(acc_x))

# Plot the observations and the output states
fig, ax = plt.subplots()
ax.plot((motion_smooth*10)-2.5, label="Observations")
ax.plot(output_states, label="Output States")
ax.legend()
ax.set_xlabel("Time")
ax.set_ylabel("Value")
plt.show()