# Informe de verificación — 20 de septiembre de 2026

## VERIFICADO AUTOMÁTICAMENTE

- `clean assembleDebug`: correcto.
- `test`: 14 pruebas JVM, 0 fallos, 0 omitidas. Incluyen el reconocimiento de “qué hay adelante mío” y el formato hablado de dirección y cercanía.
- `lintDebug`: 0 errores (48 advertencias informativas de compatibilidad/UX).
- `connectedAndroidTest` en Motorola moto g53 5G, Android 14: Room, Compose, instalación concurrente Vosk/YOLO e inferencia YOLO, 0 fallos. La prueba LLM se omite en la corrida aislada de Gradle porque el modelo no forma parte del APK de pruebas.
- En la verificación actual, el APK de pruebas instrumentadas se compiló correctamente, pero su ejecución se detuvo con `No connected devices` porque el Motorola ya no estaba conectado.
- LLM por ejecución ADB separada en el mismo Motorola: Qwen verificado, LiteRT-LM inicializado y generación no vacía; 1 prueba, 0 fallos, 82.017 s.
- Integridad: Vosk, YOLO de origen, YOLO ONNX y Qwen coinciden con los hashes de `docs/models.md`.
- APK final: `app/build/outputs/apk/debug/app-debug.apk`, 352,206,600 bytes, SHA-256 `b2e4485312133a5efdbb8b6c97286b90e4e24b4276184ecef8663593d5040be1`.
- La implementación anterior quedó instalada y abierta en el Motorola, con Vosk, YOLO y Qwen en su almacenamiento privado. El APK actualizado no se instaló porque el teléfono ya no aparece conectado por ADB.

## IMPLEMENTADO — PENDIENTE DE HARDWARE

- Cámara real, OCR, QR/códigos y exactitud YOLO en escenas reales.
- Anuncio automático por altavoz de cada escena nueva, ubicación aproximada y cercanía de objetos.
- Orden “qué hay adelante mío” mediante Vosk y respuesta TTS con cámara real.
- Reconocimiento Vosk y TTS con ruido, altavoces y auriculares reales.
- GNSS, brújula, acelerómetro, detección de caída y prueba térmica prolongada.
- BLE/WebSocket con un ESP32 físico y sensores reales.
- SOS con SIM, SMS y llamada reales.
- Recorrido manual TalkBack, `AccessibilityService` y `NotificationListenerService`.

Para cada elemento, dispositivo, permisos, pasos y resultado esperado: `docs/hardware-verification.md`.

## NO IMPLEMENTADO

Vacío. Las comprobaciones restantes dependen de interacción o hardware físico, no de código ausente.
