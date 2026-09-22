# VisionGuía

Aplicación Android local-first de asistencia para personas ciegas o con baja visión. Integra voz, visión, navegación, telefonía y sensores opcionales ESP32. La cámara, el audio y las conversaciones se procesan en el dispositivo; Internet se usa únicamente para obtener el LLM gratuito verificado durante el primer inicio.

> Tecnología complementaria: no reemplaza bastón blanco, perro guía ni formación profesional de orientación y movilidad. La visión artificial y las distancias pueden equivocarse.

## Implementación

- Kotlin, Jetpack Compose, MVVM, Hilt, Room, DataStore y WorkManager.
- Vosk español offline incluido y autoinstalable; Android TTS `es-PE` con prioridades e interrupción.
- CameraX, OCR y códigos con ML Kit on-device.
- YOLOv8n local, exportado del peso oficial a ONNX 320×320 y ejecutado con ONNX Runtime Android.
- Los objetos detectados se anuncian por el altavoz con posición y cercanía. Las escenas sin cambios se agrupan para reducir repeticiones, consumo y lentitud.
- Qwen3 0.6B cuantizado con LiteRT-LM. `ModelDownloadWorker` lo descarga por Wi-Fi, reanuda y valida SHA-256.
- BLE y WebSocket local para ESP32, validación del protocolo, fusión de sensores y detección de caídas.
- GNSS, orientación y motor Dijkstra para grafos peatonales offline.
- SOS confirmado con cuenta regresiva, SMS y llamada; control de linterna, batería, hora y fecha.
- `AccessibilityService`, `NotificationListenerService` y diagnóstico real de capacidades.

## Compilar y probar

Requiere Android SDK 35 y JDK 17 o 21.

```powershell
.\gradlew.bat clean assembleDebug
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

APK de depuración: `app/build/outputs/apk/debug/app-debug.apk`.

## Primer inicio

1. Instala y abre el APK.
2. Concede solo los permisos de las funciones que usarás.
3. Vosk y YOLO se extraen automáticamente desde el APK a `files/models` y se comprueba su SHA-256.
4. WorkManager descarga Qwen3 automáticamente cuando exista Wi-Fi sin medición y almacenamiento suficiente. Una descarga interrumpida se reanuda.
5. Los accesos especiales de Android (Accesibilidad y acceso a notificaciones) requieren activación explícita en Ajustes por diseño del sistema operativo.

No es necesario copiar modelos ni crear archivos manualmente.

## Comandos

“ayuda”, “qué hay adelante mío”, “dime qué hay delante”, “qué hora es”, “batería”, “linterna”, “modo caminar”, “lee texto”, “encuentra una silla”, “diagnóstico”, “habla más rápido”, “repite” y “SOS”. No es necesario pronunciar signos de interrogación. Las acciones sensibles se confirman y validan antes de ejecutarse.

## Documentación

- [Arquitectura](docs/architecture.md)
- [Modelos, hashes y licencias](docs/models.md)
- [Permisos](docs/permissions.md)
- [Privacidad](docs/privacy.md)
- [Accesibilidad](docs/accessibility.md)
- [Compatibilidad](docs/compatibility.md)
- [Pruebas](docs/testing.md)
- [Informe de verificación](docs/verification-report.md)
- [Guía de uso y comandos de voz](docs/guia-de-uso-y-comandos.md)
- [Verificación física](docs/hardware-verification.md)
- [Firmware ESP32](docs/esp32/README.md)

## Límites de seguridad

No se abren enlaces QR automáticamente ni se afirma una denominación monetaria sin un modelo específico validado. El SOS real, TalkBack, la cámara, sensores, SIM y ESP32 necesitan comprobación física final. Los detalles exactos están en la guía de verificación.

Licencia del código: AGPL-3.0, necesaria por la distribución del modelo YOLOv8n. Dependencias y modelos conservan sus licencias propias; consulta [NOTICE_MODELS.md](NOTICE_MODELS.md).
