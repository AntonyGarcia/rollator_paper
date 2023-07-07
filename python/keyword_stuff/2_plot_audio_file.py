import numpy as np
import matplotlib.pyplot as plt
import librosa
from hmmlearn import hmm
import joblib

# Load the audio file and apply a moving average filter
audio_file = "resampled_audio_files/sample_1.wav"
y, sr = librosa.load(audio_file)
window_size = 250
weights = np.repeat(1.0, window_size) / window_size
y_smooth = np.convolve(abs(y), weights, 'valid')

# Initialize the HMM model
n_states = 5
model = hmm.GaussianHMM(n_components=n_states, covariance_type="full")

# Fit the HMM model to the observations
model.fit(y_smooth.reshape(-1, 1))

# Save the trained model to a file
model_file = "trained_models/cs_1.pkl"
joblib.dump(model, model_file)

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

# Plot the smoothed waveform with the filtered hidden states
plt.plot(y_smooth)
plt.plot(filtered_hidden_states, label="Filtered Hidden States")
plt.xlabel("Time (s)")
plt.ylabel("Amplitude")
plt.show()
