import asyncio
from bleak import BleakClient

async def run(address, loop):
    async with BleakClient(address, loop=loop) as client:
        battery_level = await client.read_gatt_char("2A19")
        print("Battery Level:", int.from_bytes(battery_level, "little"))

address = "03:1B:A2:6B:F3:87" # replace with the address of your BLE peripheral
loop = asyncio.get_event_loop()
loop.run_until_complete(run(address, loop))