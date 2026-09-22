# Verificación física final

## Cámara, OCR, códigos y YOLO

- Dispositivo: teléfono Android con cámara trasera.
- Permiso: Cámara.
- Pasos: abrir VisionGuía; conceder Cámara; enfocar una persona y una silla; comprobar el anuncio automático; cambiar la escena; decir “qué hay adelante mío”; después decir “lee texto” y enfocar texto impreso y un QR.
- Esperado: vista CameraX estable, diagnóstico sin error, objetos anunciados por el altavoz con dirección y cercanía, respuesta hablada a la orden, texto leído y QR reportado sin abrir enlaces. Una escena idéntica no debe saturar el altavoz y puede repetirse después de ocho segundos.

## Voz y TalkBack

- Dispositivo: teléfono con micrófono y motor TTS; auriculares recomendados.
- Permisos: Micrófono; activar TalkBack y el servicio de accesibilidad desde Ajustes.
- Pasos: doble toque, decir “qué hora es”; recorrer todos los controles con TalkBack; mantener pulsado para repetir.
- Esperado: Vosk reconoce offline, TTS responde, foco/etiquetas son claros y detener interrumpe el audio.

## GNSS, brújula, movimiento y caída

- Dispositivo: teléfono con ubicación, rotación/acelerómetro; prueba de caída solo con el teléfono protegido.
- Permiso: Ubicación precisa.
- Pasos: salir a espacio abierto, ejecutar “diagnóstico”, caminar un trayecto conocido y simular lecturas con el teléfono protegido.
- Esperado: ubicación/orientación disponibles, guía coherente y la caída inicia confirmación/SOS sin disparar una acción silenciosa.

## SOS, llamada y SMS

- Dispositivo: teléfono con SIM activa y un segundo teléfono receptor.
- Permisos: Teléfono y SMS.
- Pasos: guardar un contacto de prueba; decir “SOS”, confirmar y observar la cuenta regresiva; repetir cancelando antes del final.
- Esperado: SMS y llamada solo tras confirmación; cancelar evita ambos; el receptor obtiene el mensaje.

## ESP32 por BLE y Wi-Fi local

- Dispositivo: ESP32 con el firmware de `docs/esp32`, sensores conectados y ambos equipos en la misma LAN para WebSocket.
- Permisos: Dispositivos cercanos/Bluetooth; Ubicación en Android 11 o inferior.
- Pasos: cargar firmware, emparejar por BLE, enviar lecturas válidas y fuera de rango; repetir con WebSocket local.
- Esperado: conexión/reconexión, lecturas válidas fusionadas, paquetes inválidos descartados y avisos con prioridad correcta.

## Notificaciones y accesibilidad

- Dispositivo: teléfono con otra app que produzca una notificación.
- Permisos: activar “Acceso a notificaciones” y “Accesibilidad” para VisionGuía en Ajustes.
- Pasos: enviar una notificación de prueba y accionar los controles configurados.
- Esperado: solo se expone el contenido permitido y las acciones globales requieren el servicio habilitado.

## Térmica y autonomía

- Dispositivo: teléfono objetivo conectado a herramientas de perfilado.
- Permisos: ninguno adicional.
- Pasos: ejecutar cámara/YOLO durante 30 minutos y registrar temperatura, FPS, memoria y batería.
- Esperado: sin cierres ni memoria creciente; Android puede limitar carga y la aplicación degrada frecuencia antes de una condición crítica.
