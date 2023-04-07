import json
import math
import os

def add_to_json_file(file_name, data):
    if not os.path.exists(file_name):
        with open(file_name, "w") as f:
            f.write("[]")
    with open(file_name, "r") as f:
        json_data = json.load(f)
    json_data += data
    with open(file_name, "w") as f:
        json.dump(json_data, f)


def get_selected_data(acc_data, target_timestamp):
    before_data = None
    after_data = None
    for i, d in enumerate(acc_data):
        if d["Timestamp"] < target_timestamp:
            before_data = d
        elif d["Timestamp"] >= target_timestamp:
            after_data = d
            break
    return before_data, after_data

def interpolate_data(before_timestamp, after_timestamp, target_timestamp):
    timestamp_difference = after_timestamp["Timestamp"] - before_timestamp["Timestamp"]
    target_timestamp_difference = target_timestamp - before_timestamp["Timestamp"]

    before_acc_x = before_timestamp["acc_x"]
    after_acc_x = after_timestamp["acc_x"]
    acc_x = before_acc_x + round((after_acc_x - before_acc_x) * (target_timestamp_difference / timestamp_difference), 4)

    before_acc_y = before_timestamp["acc_y"]
    after_acc_y = after_timestamp["acc_y"]
    acc_y = before_acc_y + round((after_acc_y - before_acc_y) * (target_timestamp_difference / timestamp_difference), 4)

    before_acc_z = before_timestamp["acc_z"]
    after_acc_z = after_timestamp["acc_z"]
    acc_z = before_acc_z + round((after_acc_z - before_acc_z) * (target_timestamp_difference / timestamp_difference), 4)

    interpolated_data = {
        "acc_x": acc_x,
        "acc_y": acc_y,
        "acc_z": acc_z,
        "Timestamp": round(target_timestamp, 4)
    }
    return interpolated_data

def interpolate_gyro(before_timestamp, after_timestamp, target_timestamp):
    timestamp_difference = after_timestamp["Timestamp"] - before_timestamp["Timestamp"]
    target_timestamp_difference = target_timestamp - before_timestamp["Timestamp"]

    before_acc_x = before_timestamp["gy_x"]
    after_acc_x = after_timestamp["gy_x"]
    acc_x = before_acc_x + round((after_acc_x - before_acc_x) * (target_timestamp_difference / timestamp_difference), 4)

    before_acc_y = before_timestamp["gy_y"]
    after_acc_y = after_timestamp["gy_y"]
    acc_y = before_acc_y + round((after_acc_y - before_acc_y) * (target_timestamp_difference / timestamp_difference), 4)

    before_acc_z = before_timestamp["gy_z"]
    after_acc_z = after_timestamp["gy_z"]
    acc_z = before_acc_z + round((after_acc_z - before_acc_z) * (target_timestamp_difference / timestamp_difference), 4)

    interpolated_data = {
        "gy_x": acc_x,
        "gy_y": acc_y,
        "gy_z": acc_z,
        "Timestamp": round(target_timestamp, 4)
    }
    return interpolated_data


def reformat_resampled_data(file_name):
    with open(file_name, "r") as f:
        data = json.load(f)
    formatted_data = []
    for d in data:
        formatted_data.append({
            "acc_x": d["acc_x"],
            "acc_y": d["acc_y"],
            "acc_z": d["acc_z"],
            "gy_x": d["gy_x"],
            "gy_y": d["gy_y"],
            "gy_z": d["gy_z"],
            "timestamp": d["Timestamp"]
        })
    with open(file_name, "w") as f:
        json.dump(formatted_data, f)

input_dir = "fixed_recorded_data"
output_dir = "resampled_recorded_data"

# create the output directory if it doesn't exist
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

# loop through all files in the input directory
for file_name in os.listdir(input_dir):
    if file_name.endswith(".json"):
        input_file_path = os.path.join(input_dir, file_name)
        output_file_path = os.path.join(output_dir, file_name)

        with open(input_file_path, "r") as f:
            data = json.load(f)

        acc_data = [d for d in data if all(key in d for key in ["acc_x", "acc_y", "acc_z","Timestamp"])]
        gy_data = [d for d in data if all(key in d for key in ["gy_x", "gy_y", "gy_z","Timestamp"])]

        timestamps = [d["Timestamp"] for d in acc_data]

        lower_timestamp = min(timestamps)
        rounded_lower_timestamp = math.ceil(lower_timestamp)
        higher_timestamp = max(timestamps)
        rounded_higher_timestamp = math.floor(higher_timestamp)
        time_step = 0.0125
        current_time = rounded_lower_timestamp-time_step

        buffer = []  # create an empty list to store the dictionaries
        buffer_size = 1000  # set the buffer size

        while (current_time<rounded_higher_timestamp):
            current_time = current_time+time_step
            num = "{:.4f}".format(current_time)
            current_time = float(num)
            before_timestamp, after_timestamp = get_selected_data(acc_data, current_time)
            acceleration = interpolate_data(before_timestamp, after_timestamp, current_time)
            before_timestamp, after_timestamp = get_selected_data(gy_data, current_time)
            gyro = interpolate_gyro(before_timestamp, after_timestamp, current_time)
            acceleration.update(gyro)
            rounded_acceleration = {k: round(v, 4) for k, v in acceleration.items()}
            buffer.append(rounded_acceleration)  # add the dictionary to the buffer

            # check if the buffer is full
            if len(buffer) == buffer_size:
                # save the buffer to the output file and clear the buffer
                add_to_json_file(output_file_path, buffer)
                buffer = []

        # check if the buffer is not empty
        if len(buffer) > 0:
            # save the remaining dictionaries to the output file
            add_to_json_file(output_file_path, buffer)

        reformat_resampled_data(output_file_path)
