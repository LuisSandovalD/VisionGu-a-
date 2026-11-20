
#define TRIG_PIN 5
#define ECHO_PIN 18

void setup() {
  Serial.begin(115200);

  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);

  digitalWrite(TRIG_PIN, LOW);

  Serial.println("VISIONGUIA - DIAGNOSTICO DE SEÑALES");
}

void loop() {
  Serial.print("ECHO antes del disparo: ");
  Serial.println(digitalRead(ECHO_PIN));

  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(5);

  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);

  digitalWrite(TRIG_PIN, LOW);

  unsigned long duracion =
    pulseIn(ECHO_PIN, HIGH, 30000);

  Serial.print("Duracion ECHO: ");
  Serial.print(duracion);
  Serial.println(" us");

  if (duracion > 0) {
    Serial.print("Distancia: ");
    Serial.print(duracion * 0.0343 / 2.0);
    Serial.println(" cm");
  } else {
    Serial.println("SIN ECO");
  }

  Serial.println("-------------------------");
  delay(500);
}

