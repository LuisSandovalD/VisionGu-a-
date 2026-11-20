#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

static const int TRIG_PIN = 5;
static const int ECHO_PIN = 18;
static BLECharacteristic *sensorCharacteristic;

float distanceCm() {
  digitalWrite(TRIG_PIN, LOW); delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH); delayMicroseconds(10); digitalWrite(TRIG_PIN, LOW);
  unsigned long duration = pulseIn(ECHO_PIN, HIGH, 30000);
  if (duration == 0) return -1;
  return duration * 0.0343f / 2.0f;
}

void setup() {
  pinMode(TRIG_PIN, OUTPUT); pinMode(ECHO_PIN, INPUT);
  BLEDevice::init("VisionGuia-ESP32");
  BLEServer *server = BLEDevice::createServer();
  BLEService *service = server->createService("7e400001-b5a3-f393-e0a9-e50e24dcca9e");
  sensorCharacteristic = service->createCharacteristic("7e400003-b5a3-f393-e0a9-e50e24dcca9e", BLECharacteristic::PROPERTY_NOTIFY);
  sensorCharacteristic->addDescriptor(new BLE2902());
  service->start();
  BLEAdvertising *advertising = BLEDevice::getAdvertising();
  advertising->addServiceUUID(service->getUUID()); advertising->start();
}

void loop() {
  float cm = distanceCm();
  char payload[180];
  if (cm > 0) snprintf(payload, sizeof(payload), "{\"v\":1,\"type\":\"ultrasonic\",\"angle\":0,\"distanceCm\":%.1f,\"confidence\":0.85,\"timestamp\":%lu}", cm, millis());
  else snprintf(payload, sizeof(payload), "{\"v\":1,\"type\":\"ultrasonic\",\"angle\":0,\"distanceCm\":null,\"confidence\":0,\"timestamp\":%lu}", millis());
  sensorCharacteristic->setValue((uint8_t*)payload, strlen(payload)); sensorCharacteristic->notify();
  delay(100);
}
