import json
import matplotlib.pyplot as plt

# load data from JSON file
with open('values.json', 'r') as f:
    data = json.load(f)

# plot data as a line chart
plt.plot(data)

# set chart title and axis labels
plt.title('Audio Samples')
plt.xlabel('Sample Index')
plt.ylabel('Sample Value')

# display chart
plt.show()
