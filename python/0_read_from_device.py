import asyncio, time, json
from bleak import BleakClient

def handle_accel_notification(sender, data):
    x, y, z = data.decode().split(',')
    acX = float(x)
    acY = float(y)
    acZ = float(z)
    timestamp = time.time()
    accel_data = {"acc_x": acX, "acc_y": acY, "acc_z": acZ, "Timestamp": timestamp}
    with open("data.json", "a") as json_file:
        json.dump(accel_data, json_file)
        json_file.write(',\n')

def handle_gyro_notification(sender, data):
    gx, gy, gz = data.decode().split(',')
    gX = float(gx)
    gY = float(gy)
    gZ = float(gz)
    timestamp = time.time()
    gyro_data = {"gy_x": gX, "gy_y": gY, "gy_z": gZ, "Timestamp": timestamp}
    #print(gyro_data)
    with open("data.json", "a") as json_file:
        json.dump(gyro_data, json_file)
        json_file.write(',\n')

async def run(address, loop):
    async with BleakClient(address, loop=loop) as client:
        await client.start_notify("00002001-0000-1000-8000-00805f9b34fb", handle_accel_notification)
        await client.start_notify("00002002-0000-1000-8000-00805f9b34fb", handle_gyro_notification)
        while True:
            await asyncio.sleep(0.001)

address = "03:1B:A2:6B:F3:87" # replace with the address of your BLE peripheral
loop = asyncio.get_event_loop()
loop.run_until_complete(run(address, loop))
