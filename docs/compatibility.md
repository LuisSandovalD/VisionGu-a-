# Compatibilidad y degradación

| Condición | Comportamiento |
|---|---|
| Sin GPU | ONNX Runtime y LiteRT-LM usan CPU; baja el rendimiento, no falla el inicio |
| Sin BLE | ESP32 Wi‑Fi local o sensores del teléfono |
| Sin brújula | Informa limitación; no inventa orientación |
| Sin ARCore | Sensor físico o heurística visual de baja confianza |
| Poca RAM | No cargar LLM; comandos básicos siguen activos |
| Batería baja | Se debe reducir frecuencia visual; SOS permanece disponible |
| Temperatura alta | Suspender LLM/visión antes que emergencia |
| Sin ESP32 | Cámara y sensores internos continúan |
| Sin LLM | `IntentRouter` mantiene comandos básicos |
| Sin Vosk | Botón permanece accesible, pero informa que falta el modelo |
| Sin YOLO | Vista previa sin detecciones; nunca inventa objetos |

Mínimo: API 26. Target/compile: API 35. LiteRT-LM necesita un dispositivo moderno, alrededor de 700 MB libres durante la descarga/activación y no es representativo en emuladores.
