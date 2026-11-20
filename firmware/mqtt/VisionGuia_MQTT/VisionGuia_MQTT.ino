
#include <WiFi.h>
#include <PubSubClient.h>

// =====================================
// CONFIGURACION WIFI
// =====================================
const char* WIFI_SSID = "iPhone de E";
const char* WIFI_PASSWORD = "entel123";

// =====================================
// CONFIGURACION MQTT
// =====================================
const char* MQTT_SERVER = "192.168.18.22";
const int MQTT_PORT = 1883;

const char* MQTT_TOPIC = "visionguia/sensor/distancia";
const char* MQTT_CLIENT_ID = "VisionGuia-ESP32";

// =====================================
// SENSOR HC-SR04
// =====================================
#define TRIG_PIN 5
#define ECHO_PIN 18

// =====================================
// OBJETOS
// =====================================
WiFiClient espClient;
PubSubClient mqttClient(espClient);

unsigned long lastMeasurement = 0;
const unsigned long measurementInterval = 1000;

// =====================================
// CONEXION WIFI
// =====================================
void connectWiFi() {
  Serial.print("Conectando a WiFi");

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println();
  Serial.println("WiFi conectado");
  Serial.print("IP del ESP32: ");
  Serial.println(WiFi.localIP());
}

// =====================================
// CONEXION MQTT
// =====================================
void connectMQTT() {
  while (!mqttClient.connected()) {
    Serial.print("Conectando a MQTT...");

    if (mqttClient.connect(MQTT_CLIENT_ID)) {
      Serial.println(" conectado");
    } else {
      Serial.print(" Error: ");
      Serial.print(mqttClient.state());
      Serial.println(" Reintentando en 3 segundos");
      delay(3000);
    }
  }
}

// =====================================
// LECTURA DEL SENSOR
// =====================================
float readDistance() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(3);

  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  unsigned long duration = pulseIn(ECHO_PIN, HIGH, 30000UL);

  if (duration == 0) {
    return -1;
  }

  float distance = duration * 0.0343 / 2.0;

  if (distance < 2 || distance > 400) {
    return -1;
  }

  return distance;
}

// =====================================
// PUBLICAR MEDICION MQTT
// =====================================
void publishMeasurement(float distance) {
  char payload[100];

  snprintf(
    payload,
    sizeof(payload),
    "{\"distanceCm\":%.2f,\"source\":\"real\"}",
    distance
  );

  bool published = mqttClient.publish(MQTT_TOPIC, payload);

  if (published) {
    Serial.print("Publicado: ");
    Serial.println(payload);
  } else {
    Serial.println("Error al publicar MQTT");
  }
}

// =====================================
// SETUP
// =====================================
void setup() {
  Serial.begin(115200);

  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);

  digitalWrite(TRIG_PIN, LOW);

  connectWiFi();

  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setBufferSize(256);

  connectMQTT();

  Serial.println("VisionGuia MQTT iniciado");
}

// =====================================
// LOOP
// =====================================
void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }

  if (!mqttClient.connected()) {
    connectMQTT();
  }

  mqttClient.loop();

  unsigned long now = millis();

  if (now - lastMeasurement >= measurementInterval) {
    lastMeasurement = now;

    float distance = readDistance();

    if (distance >= 0) {
      Serial.print("Distancia: ");
      Serial.print(distance);
      Serial.println(" cm");

      publishMeasurement(distance);
    } else {
      Serial.println("Sin eco valido. No se publica medicion.");
    }
  }
}
