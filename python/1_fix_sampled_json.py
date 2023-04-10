import json

with open("raw_recorded_data/data_falls_8.json", "r") as json_file:
    data = json_file.read()

fixed_data = "[\n" + data[:-2] + "\n]"

with open("fixed_recorded_data/data_falls_8.json", "w") as fixed_json_file:
    fixed_json_file.write(fixed_data)
