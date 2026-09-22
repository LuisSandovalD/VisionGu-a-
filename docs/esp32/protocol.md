# Protocolo ESP32 v1

UTF-8 JSON, máximo 512 bytes:

```json
{"v":1,"type":"ultrasonic","angle":0,"distanceCm":82.4,"confidence":0.86,"timestamp":123456}
```

`type`: `ultrasonic`, `tof`, `imu` o `camera_depth`. `angle`: -180..180 grados. `distanceCm`: 1..1000 o `null`. `confidence`: 0..1. `timestamp` es el tiempo del ESP32; Android aplica su tiempo local si falta.

Mensajes con versión desconocida, valores fuera de rango o JSON inválido se descartan. BLE utiliza notificaciones; Wi‑Fi requiere primero `{"v":1,"command":"subscribe"}`.
