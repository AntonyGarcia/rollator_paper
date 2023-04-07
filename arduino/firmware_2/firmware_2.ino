#include <ArduinoBLE.h>
#include <Arduino_LSM9DS1.h>

// Declare a BLE service with UUID "1001"
BLEService IMUservice("1001");

// Declare two BLE characteristics for the accelerometer and gyroscope data, both with UUIDs "2001" and "2011" respectively.
// The characteristics are readable and notify, and have a maximum data length of 20 bytes.
BLECharacteristic accelData("2001", BLERead | BLENotify, 20);
BLECharacteristic gyroData("2011", BLERead | BLENotify, 20);

void setup() {
  // Initialize the BLE module
  if (!BLE.begin()) {
    // If the BLE module fails to initialize, enter an infinite loop
    while (1) {
    }
  }

  // Initialize the IMU
  if (!IMU.begin()) {
    // If the IMU fails to initialize, enter an infinite loop
    while (1) {
    }
  }

  // Set the device name and local name to "IMU"
  BLE.setDeviceName("IMU");
  BLE.setLocalName("IMU");

  // Add the service to the BLE module
  BLE.setAdvertisedService(IMUservice);

  // Add the two characteristics to the service
  IMUservice.addCharacteristic(accelData);
  IMUservice.addCharacteristic(gyroData);

  // Add the service to the BLE module
  BLE.addService(IMUservice);

  // Set the connection interval for the BLE connection
  BLE.setConnectionInterval(8, 8);

  // Enable the BLE module to be connectable
  BLE.setConnectable(true);

  // Start advertising the BLE connection
  BLE.advertise();
}

void loop() {
  float acX, acY, acZ, gX, gY, gZ;

  // Get the connected BLE central device
  BLEDevice central = BLE.central();
  if (central) {
    // If there is a connected BLE central device, enter an infinite loop
    while (central.connected()) {
      // Read the accelerometer data from the IMU device
      IMU.readAcceleration(acX, acY, acZ);

      // Create a string with the accelerometer data
      String accelString = String(acX) + "," + String(acY) + "," + String(acZ);

      // Write the accelerometer data to the BLE characteristic
      accelData.writeValue(accelString.c_str());

      // Read the gyroscope data from the IMU device
      IMU.readGyroscope(gX, gY, gZ);

      // Create a string with the gyroscope data
      String gyroString = String(gX) + "," + String(gY) + "," + String(gZ);

      // Write the gyroscope data to the BLE characteristic
      gyroData.writeValue(gyroString.c_str());

      // Wait 7 milliseconds before sending the next data
      delay(7);
    }
  }
}
