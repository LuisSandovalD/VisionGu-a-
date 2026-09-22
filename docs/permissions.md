# Permisos

| Permiso | Motivo | Cuándo |
|---|---|---|
| CAMERA | Vista previa y análisis local | Al activar visión |
| RECORD_AUDIO | Vosk offline | Al escuchar |
| ACCESS_FINE/COARSE_LOCATION | GNSS y navegación/SOS | Al activar ubicación |
| BLUETOOTH_SCAN/CONNECT | ESP32 BLE | Al elegir BLE |
| INTERNET/NETWORK_STATE | Descargar modelos y ESP32 LAN | Según función |
| POST_NOTIFICATIONS | Servicio visible | API 33+ |
| FOREGROUND_SERVICE_CAMERA/MICROPHONE | Uso visible prolongado | Solo iniciado desde UI |
| CALL_PHONE | Llamada SOS confirmada | Configuración de SOS |
| SEND_SMS | Mensaje SOS confirmado | Configuración de SOS |
| VIBRATE | Alertas hápticas | Según ajuste |

Accesibilidad y acceso a notificaciones son accesos especiales que Android solo habilita desde sus pantallas de Ajustes. La app no evade consentimiento.
