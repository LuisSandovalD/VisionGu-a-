# Privacidad

- Cámara y audio se procesan en memoria y se descartan; no se guardan por defecto.
- No se envían imágenes, voz, conversaciones, contactos ni ubicación a la nube.
- Los modelos se descargan por HTTPS, se validan con SHA-256 y se almacenan en espacio privado.
- El ESP32 se conecta directamente por BLE o red local.
- Room conserva únicamente contactos de emergencia, lugares elegidos e historial opcional.
- “Borra mi historial” elimina el historial local. Los lugares tienen una operación separada.
- `allowBackup=false`; los modelos quedan fuera de transferencia.
- Los logs de producción no incluyen contenido hablado ni notificaciones.
