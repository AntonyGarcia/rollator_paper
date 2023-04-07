#include <ArduinoBLE.h>
#include <Arduino_LSM9DS1.h>
BLEService ACC("1001");
BLECharacteristic imuData("2001", BLERead | BLENotify, 30);
void setup() {
  if (!BLE.begin()) {
    while (1);
  }
  if (!IMU.begin()) {
    while (1);
  }
  BLE.setDeviceName("IMU");
  BLE.setLocalName("IMU");
  BLE.setAdvertisedService(ACC);
  ACC.addCharacteristic(imuData);
  BLE.addService(ACC);
  BLE.setConnectionInterval(8, 8);
  //BLE.setAdvertisingInterval(25);
  BLE.setConnectable(true);
  BLE.advertise();
}

void loop() {
  float acX, acY, acZ;
  BLEDevice central = BLE.central();
  if (central) {
    while (central.connected()) {
      IMU.readAcceleration(acX, acY, acZ);
      String imuString = String(acX) + "," + String(acY) + "," + String(acZ);
      imuData.writeValue(imuString.c_str());
      delay(8);
    }
  }
}