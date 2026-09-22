#include <WiFi.h>
#include <WebSocketsServer.h>

static const char *WIFI_SSID = "VISIONGUIA_LOCAL";
static const char *WIFI_PASSWORD = "cambiar-clave-local";
static const int TRIG_PIN = 5;
static const int ECHO_PIN = 18;
WebSocketsServer webSocket(81);

float distanceCm() {
  digitalWrite(TRIG_PIN, LOW); delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH); delayMicroseconds(10); digitalWrite(TRIG_PIN, LOW);
  unsigned long duration = pulseIn(ECHO_PIN, HIGH, 30000);
  return duration == 0 ? -1 : duration * 0.0343f / 2.0f;
}

void setup() {
  pinMode(TRIG_PIN, OUTPUT); pinMode(ECHO_PIN, INPUT);
  WiFi.mode(WIFI_STA); WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) delay(250);
  webSocket.begin();
}

void loop() {
  webSocket.loop();
  static unsigned long previous = 0;
  if (millis() - previous < 100) return;
  previous = millis();
  float cm = distanceCm(); char payload[180];
  if (cm > 0) snprintf(payload, sizeof(payload), "{\"v\":1,\"type\":\"ultrasonic\",\"angle\":0,\"distanceCm\":%.1f,\"confidence\":0.85,\"timestamp\":%lu}", cm, millis());
  else snprintf(payload, sizeof(payload), "{\"v\":1,\"type\":\"ultrasonic\",\"angle\":0,\"distanceCm\":null,\"confidence\":0,\"timestamp\":%lu}", millis());
  webSocket.broadcastTXT(payload);
}
