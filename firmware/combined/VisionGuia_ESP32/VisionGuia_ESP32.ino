
#include <WiFi.h>
#include <PubSubClient.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// =============================================
// 1. CONFIGURACION WIFI
// =============================================

const char* WIFI_SSID = "FAM_MAYES";
const char* WIFI_PASSWORD = "28201418";

// =============================================
// 2. CONFIGURACION MQTT
// =============================================

const char* MQTT_SERVER = "192.168.18.22";
const int MQTT_PORT = 1883;

const char* MQTT_TOPIC = "visionguia/sensor/distancia";
const char* MQTT_CLIENT_ID = "VisionGuia-ESP32-Combined";

// =============================================
// 3. SENSOR HC-SR04
// =============================================

#define TRIG_PIN 5
#define ECHO_PIN 18

const unsigned long MEASUREMENT_INTERVAL = 1000;

// =============================================
// 4. UUID BLE - DEBEN COINCIDIR CON ANDROID
// =============================================

#define SERVICE_UUID "7e400001-b5a3-f393-e0a9-e50e24dcca9e"
#define SENSOR_UUID  "7e400003-b5a3-f393-e0a9-e50e24dcca9e"

// =============================================
// 5. OBJETOS Y ESTADO
// =============================================

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

BLECharacteristic* sensorCharacteristic = nullptr;

bool bleConnected = false;

unsigned long lastMeasurement = 0;
unsigned long lastWifiAttempt = 0;
unsigned long lastMqttAttempt = 0;

const unsigned long RECONNECT_INTERVAL = 3000;

// =============================================
// 6. CALLBACKS BLE
// =============================================

class VisionGuiaServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* server) override {
        bleConnected = true;
        Serial.println("Android conectado por BLE");
    }

    void onDisconnect(BLEServer* server) override {
        bleConnected = false;
        Serial.println("Android desconectado");
        BLEDevice::startAdvertising();
    }
};

// =============================================
// 7. INICIALIZAR BLE
// =============================================

void setupBLE() {
    BLEDevice::init("VisionGuia-ESP32");
    BLEDevice::setMTU(185);

    BLEServer* server = BLEDevice::createServer();

    server->setCallbacks(new VisionGuiaServerCallbacks());

    BLEService* service = server->createService(SERVICE_UUID);

    sensorCharacteristic = service->createCharacteristic(
        SENSOR_UUID,
        BLECharacteristic::PROPERTY_READ |
        BLECharacteristic::PROPERTY_NOTIFY
    );

    sensorCharacteristic->addDescriptor(new BLE2902());

    sensorCharacteristic->setValue("{\"status\":\"ready\"}");

    service->start();

    BLEAdvertising* advertising = BLEDevice::getAdvertising();

    advertising->addServiceUUID(SERVICE_UUID);
    advertising->setScanResponse(true);

    BLEDevice::startAdvertising();

    Serial.println("BLE iniciado: VisionGuia-ESP32");
}

// =============================================
// 8. CONEXION WIFI NO BLOQUEANTE
// =============================================

void maintainWiFi() {
    if (WiFi.status() == WL_CONNECTED) return;

    unsigned long now = millis();

    if (now - lastWifiAttempt < RECONNECT_INTERVAL) return;

    lastWifiAttempt = now;

    Serial.println("Intentando conectar WiFi...");

    if (lastWifiAttempt == RECONNECT_INTERVAL) {
        WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    } else {
        WiFi.reconnect();
    }
}

// =============================================
// 9. CONEXION MQTT
// =============================================

void maintainMQTT() {
    if (WiFi.status() != WL_CONNECTED) return;
    if (mqttClient.connected()) return;

    unsigned long now = millis();

    if (now - lastMqttAttempt < RECONNECT_INTERVAL) return;

    lastMqttAttempt = now;

    Serial.print("Conectando a MQTT...");

    if (mqttClient.connect(MQTT_CLIENT_ID)) {
        Serial.println(" conectado");
    } else {
        Serial.print(" Error: ");
        Serial.println(mqttClient.state());
    }
}

// =============================================
// 10. LECTURA HC-SR04
// =============================================

float readDistance() {
    digitalWrite(TRIG_PIN, LOW);
    delayMicroseconds(3);

    digitalWrite(TRIG_PIN, HIGH);
    delayMicroseconds(10);

    digitalWrite(TRIG_PIN, LOW);

    unsigned long duration = pulseIn(
        ECHO_PIN,
        HIGH,
        30000UL
    );

    if (duration == 0) return -1;

    float distance = duration * 0.0343f / 2.0f;

    if (distance < 2 || distance > 400) return -1;

    return distance;
}

// =============================================
// 11. PUBLICAR MQTT
// CONSERVA EL FORMATO DEL FIRMWARE ANTERIOR
// =============================================

void publishMQTT(float distance) {
    if (!mqttClient.connected()) return;

    char payload[100];

    snprintf(
        payload,
        sizeof(payload),
        "{\"distanceCm\":%.2f,\"source\":\"real\"}",
        distance
    );

    if (mqttClient.publish(MQTT_TOPIC, payload)) {
        Serial.print("MQTT: ");
        Serial.println(payload);
    } else {
        Serial.println("Error publicando MQTT");
    }
}

// =============================================
// 12. ENVIAR NOTIFICACION BLE
// PROTOCOLO COMPATIBLE CON ANDROID
// =============================================

void publishBLE(float distance) {
    if (!bleConnected || sensorCharacteristic == nullptr) return;

    char payload[160];

    snprintf(
        payload,
        sizeof(payload),
        "{\"v\":1,\"type\":\"ultrasonic\",\"angle\":0,\"distanceCm\":%.2f,\"confidence\":0.95,\"timestamp\":%lu}",
        distance,
        millis()
    );

    sensorCharacteristic->setValue(
        (uint8_t*)payload,
        strlen(payload)
    );

    sensorCharacteristic->notify();

    Serial.print("BLE: ");
    Serial.println(payload);
}

// =============================================
// 13. SETUP
// =============================================

void setup() {
    Serial.begin(115200);

    pinMode(TRIG_PIN, OUTPUT);
    pinMode(ECHO_PIN, INPUT);

    digitalWrite(TRIG_PIN, LOW);

    setupBLE();

    WiFi.mode(WIFI_STA);
    WiFi.setAutoReconnect(true);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
    mqttClient.setBufferSize(256);

    Serial.println("--------------------------------");
    Serial.println("VISIONGUIA - FIRMWARE COMBINADO");
    Serial.println("BLE + MQTT + HC-SR04");
    Serial.println("--------------------------------");
}

// =============================================
// 14. LOOP PRINCIPAL
// =============================================

void loop() {
    maintainWiFi();

    maintainMQTT();

    if (mqttClient.connected()) {
        mqttClient.loop();
    }

    unsigned long now = millis();

    if (now - lastMeasurement >= MEASUREMENT_INTERVAL) {
        lastMeasurement = now;

        float distance = readDistance();

        if (distance >= 0) {
            Serial.print("Distancia: ");
            Serial.print(distance);
            Serial.println(" cm");

            publishBLE(distance);
            publishMQTT(distance);
        } else {
            Serial.println("Sin eco valido");
        }
    }

    delay(5);
}
