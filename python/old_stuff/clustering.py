import json
import numpy as np
import hmmlearn.hmm
import matplotlib.pyplot as plt

# Load the data from the file
with open('data.json') as f:
    data = json.load(f)

# Prepare the data
acceleration = np.array([[sample['x'], sample['y'], sample['z']] for sample in data])

# Compute the derivative of the acceleration
derivative = np.diff(acceleration, axis=0)

# Train the HMM
model = hmmlearn.hmm.GaussianHMM(n_components=3, covariance_type="full")
model.fit(derivative)

# Decode the HMM
hidden_states = model.predict(derivative)

# Extract the steps
steps = []
start = 0
for i in range(1, len(hidden_states)):
    if hidden_states[i] != hidden_states[i - 1]:
        steps.append(derivative[start:i])
        start = i
steps.append(derivative[start:])

# Save the step sizes
step_sizes = [step.shape[0] for step in steps]

# Remove steps with size lower than 50
steps = [step for step, size in zip(steps, step_sizes) if size >= 50]
step_sizes = [size for size in step_sizes if size >= 50]

# Plot the derivatives
plt.plot(derivative)
plt.title("Derivatives of acceleration")
plt.xlabel("Index")
plt.ylabel("Derivative")

# Plot the vertical lines for the start and end of each step
for step in steps:
    start = np.where(derivative == step[0])[0][0]
    end = np.where(derivative == step[-1])[0][0]
    plt.axvline(x=start, color='red', linestyle='--')
    plt.axvline(x=end, color='red', linestyle='--')

# Show the plot
plt.show()
