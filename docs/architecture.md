# Arquitectura

`presentation` expone estado inmutable mediante `StateFlow`. `domain` contiene enrutamiento y algoritmos puros. `data` gestiona Room, DataStore y modelos. Las capacidades de voz, visión, sensores, teléfono y emergencia dependen de interfaces pequeñas y errores tipados.

Flujo de voz: gesto → Vosk local → `IntentRouter` → acción validada → `SpeechOrchestrator` → TTS. Las reglas resuelven órdenes frecuentes; solo el texto no reconocido llega al LLM local.

Flujo visual: CameraX (`KEEP_ONLY_LATEST`) → RGBA → YOLOv8n ONNX Runtime → NMS → tracking → posición/distancia → anuncio priorizado. Los frames se descartan inmediatamente.

Flujo ESP32: BLE/WebSocket local → parser JSON versionado → `SensorReading` → `SensorFusionEngine`. Las mediciones físicas válidas prevalecen sobre heurísticas visuales.

Las prioridades habladas son peligro, navegación, conversación y ambiente. Un mensaje de mayor prioridad cancela el activo. Los comandos nuevos cancelan el procesamiento anterior.
