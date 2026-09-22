# Cableado HC-SR04

| HC-SR04 | ESP32 |
|---|---|
| VCC | 5V |
| GND | GND |
| TRIG | GPIO 5 |
| ECHO | GPIO 18 mediante divisor 1 kΩ / 2 kΩ |

El divisor reduce 5 V a aproximadamente 3.3 V. No conectes ECHO directamente. Mantén masa común. Para un VL53L0X usa I²C a 3.3 V y adapta el firmware/tipo a `tof`.
