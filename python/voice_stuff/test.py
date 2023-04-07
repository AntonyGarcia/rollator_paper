import serial
import json

# configure serial port
ser = serial.Serial('COM5', 115200, timeout=1)

# send signal to start reading
ser.write(b'r')
print("OK")

# wait for Arduino to start sending data
while True:
    line = ser.readline().decode().strip()
    if line:
        print("Reading data...")
        break

# read values from serial and store in list
values = []
while True:
    line = ser.readline().decode().strip()
    if not line:
        break
    values.append(int(line))

# write values to JSON file
with open('values.json', 'w') as f:
    json.dump(values, f)

# close serial port
ser.close()
