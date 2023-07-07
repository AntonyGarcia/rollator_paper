import json
import matplotlib.pyplot as plt

# Load the JSON file
with open('extracted_positive_samples/fall_subsample_1.json') as file:
    data = json.load(file)

# Extract the 'data' array
data_array = data['data']

# Create x-axis values
x = range(len(data_array))

# Create the plot
plt.plot(x, data_array)
plt.xlabel('Sample Index')
plt.ylabel('Amplitude')
plt.title('Data Plot')
plt.show()
