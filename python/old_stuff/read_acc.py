import asyncio
from bleak import BleakClient

async def get_data(client):
    value = await client.read_gatt_char("00002001-0000-1000-8000-00805f9b34fb")
    x, y, z = value.decode().split(',')
    acX = float(x)
    acY = float(y)
    acZ = float(z)
    return (acX, acY, acZ)

async def run(address, loop):
    async with BleakClient(address, loop=loop) as client:
        while True:
            acX, acY, acZ = await get_data(client)
            print("X={0:.2f}, Y={1:.2f}, Z={2:.2f}".format(acX, acY, acZ))
            await asyncio.sleep(0.00001)

address = "03:1B:A2:6B:F3:87" # replace with the address of your BLE peripheral
loop = asyncio.get_event_loop()
loop.run_until_complete(run(address, loop))
