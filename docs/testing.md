# Pruebas

Comandos obligatorios:

```powershell
.\gradlew.bat clean assembleDebug
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

Las pruebas JVM cubren intenciones, validación de acciones, geometría/distancias, protocolo ESP32, fusión, caída y enrutamiento peatonal Dijkstra. Las instrumentadas cubren render Compose, persistencia Room y la instalación, apertura y ejecución offline de Vosk/YOLO.

La inicialización del LLM necesita que WorkManager termine la descarga verificada de 344 MB. Puede revisarse desde el comando hablado “diagnóstico”; el estado cambia de “LLM descargando por Wi-Fi” a “LLM listo”.

La prueba física completa y sus resultados esperados están en [hardware-verification.md](hardware-verification.md).
