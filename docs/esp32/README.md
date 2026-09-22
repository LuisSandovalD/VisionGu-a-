# ESP32 para VisionGuía

Se incluyen dos firmwares reales de referencia para un HC-SR04 frontal: BLE y Wi‑Fi/WebSocket local. Ajusta pines, credenciales y placa antes de compilar con Arduino IDE/CLI.

- BLE usa el servicio `7e400001-b5a3-f393-e0a9-e50e24dcca9e` y notifica en `7e400003-b5a3-f393-e0a9-e50e24dcca9e`.
- Wi‑Fi escucha WebSocket en el puerto 81 dentro de la LAN.
- Frecuencia: 10 Hz; rango aceptado por Android: 1–1000 cm.

El HC-SR04 entrega ECHO a 5 V: usa divisor resistivo o level shifter antes del GPIO del ESP32. El sensor y el teléfono son ayudas complementarias, no dispositivos de seguridad certificados.
