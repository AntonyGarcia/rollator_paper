#include <Arduino.h>
#include "model.h"
Eloquent::ML::Port::RandomForest classifier;

const int BUFFER_SIZE = 960;
float buffer[BUFFER_SIZE];
int buffer_count = 0;

void setup() {
    Serial.begin(115200);
    Serial.println(4, DEC);
}

void loop() {
    while (Serial.available() > 0) {
        String input = Serial.readStringUntil(',');
        double value = input.toDouble();  
        buffer[buffer_count++] = (float)value;
        if (buffer_count == BUFFER_SIZE) {
            int prediction = classifier.predict(buffer);
            Serial.println("Prediction: "+String(prediction));
            buffer_count = 0;
            memset(buffer, 0, sizeof(buffer)); // reset the buffer
        }
    }
}

