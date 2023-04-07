import matplotlib.pyplot as plt
import json
import matplotlib

with open("fixed_recorded_data/iddle_1.json", "r") as f:
    data = json.load(f)

fixed_data = [d for d in data if all(key in d for key in ["acc_x", "acc_y", "acc_z","Timestamp"])]

with open("resampled_recorded_data/iddle_1.json", "r") as f:
    data = json.load(f)


resampled_data = [d for d in data if all(key in d for key in ["acc_x", "acc_y", "acc_z","timestamp"])]

# Extract the acc_x values from both datasets
fixed_acc_x = [entry['acc_x'] for entry in fixed_data[120:]]
resampled_acc_x = [entry['acc_x'] for entry in resampled_data]

# Plot the acc_x values
plt.plot(fixed_acc_x, label='fixed_data')
plt.plot(resampled_acc_x, label='resampled_data')

# Add a legend and labels
plt.legend()
plt.xlabel('Index')
plt.ylabel('acc_x')

# Show the plot
plt.show()
