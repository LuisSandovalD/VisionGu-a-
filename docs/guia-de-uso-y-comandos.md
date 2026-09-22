# Guía de uso y comandos de voz de VisionGuía

## 1. Funcionamiento de VisionGuía

VisionGuía es una aplicación Android de asistencia para personas ciegas o con baja visión. Utiliza el micrófono, la cámara, la voz del teléfono, la ubicación y sensores opcionales para ofrecer información útil sin enviar las conversaciones, imágenes o audio a servicios externos.

Sus funciones principales son:

- reconocer comandos de voz sin conexión mediante Vosk;
- responder mediante Android TTS;
- detectar objetos con YOLO ejecutado localmente;
- leer textos y códigos con la cámara;
- consultar hora, fecha y batería;
- controlar la linterna;
- guardar un contacto de emergencia y activar un SOS confirmado;
- conectarse a sensores ESP32 por Bluetooth BLE o WebSocket local;
- usar ubicación, orientación y sensores de movimiento;
- mantener conversaciones locales mediante Qwen y LiteRT-LM.

VisionGuía es una ayuda complementaria. No sustituye un bastón blanco, un perro guía ni la formación profesional de orientación y movilidad.

## 2. Primer inicio

Al abrir la aplicación por primera vez:

1. VisionGuía presenta un mensaje de bienvenida y seguridad.
2. Pulsa **Conceder permisos** para abrir el diálogo oficial de Android.
3. Autoriza las funciones que quieras utilizar: cámara, micrófono, ubicación, Bluetooth, llamada, SMS y notificaciones.
4. Los modelos Vosk y YOLO se instalan automáticamente desde el APK.
5. El modelo conversacional Qwen se descarga automáticamente cuando exista una conexión Wi-Fi no medida y suficiente almacenamiento.

Durante la descarga del LLM, los comandos básicos siguen funcionando. Puedes decir **“diagnóstico”** para consultar el estado de los modelos.

Los siguientes accesos especiales solo pueden activarse desde Ajustes de Android:

- **Accesibilidad → VisionGuía**;
- **Acceso a notificaciones → VisionGuía**.

## 3. Controles táctiles

- **Doble toque en la superficie principal:** comenzar a escuchar.
- **Pulsación prolongada:** repetir el último mensaje hablado.
- **Botón Escuchar:** iniciar el reconocimiento de voz.
- **Botón Detener:** cancelar la escucha, el procesamiento o la voz.
- **Botón Repetir:** repetir la última respuesta.
- **Guardar contacto SOS:** almacena el nombre y teléfono del contacto principal de emergencia.

Todos los controles tienen etiquetas destinadas a TalkBack.

## 4. Cómo dar un comando

1. Pulsa **Escuchar** o realiza un doble toque.
2. Espera a que la aplicación indique **Escuchando**.
3. Pronuncia una instrucción corta y clara.
4. VisionGuía procesa el comando y responde mediante voz.

Para obtener mejores resultados:

- habla cerca del micrófono;
- utiliza frases cortas;
- evita tapar el micrófono;
- en lugares ruidosos utiliza auriculares con micrófono;
- espera a que termine la respuesta antes de pronunciar otro comando.

## 5. Comandos de voz disponibles

### Ayuda

Puedes decir:

- **“Ayuda”**
- **“Qué puedes hacer”**

La aplicación explica las categorías principales de asistencia.

### Repetir una respuesta

Puedes decir:

- **“Repite”**
- **“Repítelo”**

También puedes mantener pulsada la pantalla o usar el botón **Repetir**.

### Detener

Puedes decir:

- **“Detente”**
- **“Silencio”**
- **“Calla”**

Se cancela la escucha actual y se detiene la voz.

### Consultar la hora

Puedes decir:

- **“Hora”**
- **“Qué hora es”**

VisionGuía responde con la hora configurada en el teléfono.

### Consultar la fecha

Puedes decir:

- **“Fecha”**
- **“Qué fecha es”**

### Consultar la batería

Puedes decir:

- **“Batería”**
- **“Dime la batería”**

La aplicación anuncia el porcentaje disponible.

### Encender o apagar la linterna

Di:

- **“Linterna”**

Cada ejecución alterna entre encendida y apagada. La función necesita una cámara con flash disponible.

### Activar el modo caminar

Di:

- **“Modo caminar”**

VisionGuía activa el estado de asistencia para caminar. Este modo no sustituye la ayuda principal de movilidad ni confirma que una ruta esté libre de peligros.

### Saber qué hay adelante

Puedes decir, sin utilizar signos de interrogación:

- **“Qué hay adelante mío”**
- **“Qué hay delante mío”**
- **“Qué tengo adelante”**
- **“Dime qué hay adelante”**
- **“Describe lo que hay adelante”**

VisionGuía analiza el siguiente fotograma y anuncia por el altavoz los objetos reconocidos, su dirección aproximada y si ocupan una zona grande de la imagen.

Además, cuando la cámara detecta una escena nueva, anuncia automáticamente los objetos. Para conservar fluidez y evitar mensajes repetidos, una escena sin cambios se repite como máximo cada ocho segundos.

### Leer texto y códigos

Puedes decir:

- **“Lee”**
- **“Lee este texto”**
- **“Leer texto”**

Después del comando:

1. apunta la cámara hacia el documento, etiqueta o código;
2. mantén el teléfono estable;
3. espera a que VisionGuía anuncie el contenido reconocido.

Los códigos QR que contienen enlaces se anuncian, pero nunca se abren automáticamente.

### Buscar un objeto

Utiliza una frase que comience con **“Encuentra”**. Por ejemplo:

- **“Encuentra una silla”**
- **“Encuentra mis llaves”**
- **“Encuentra una botella”**

La búsqueda solo puede reconocer categorías incluidas en el modelo visual. La falta de una detección no demuestra que el objeto o peligro no exista.

### Cambiar la velocidad de voz

Puedes decir:

- **“Habla más rápido”**
- **“Habla más lento”**

El cambio se aplica a las siguientes respuestas del TTS.

### Ejecutar el diagnóstico

Di:

- **“Diagnóstico”**

La aplicación anuncia:

- versión de Android;
- estado de Vosk, YOLO y el LLM;
- nivel de batería;
- disponibilidad o permisos de cámara, micrófono, ubicación, BLE, telefonía y sensores.

### Borrar el historial local

Di exactamente:

- **“Borra mi historial”**

Se eliminan los registros guardados localmente en la base de datos de la aplicación.

### Activar el SOS

Puedes decir:

- **“SOS”**
- **“Emergencia”**
- **“Necesito ayuda”**

Por seguridad, el SOS no se ejecuta inmediatamente:

1. VisionGuía solicita confirmación.
2. Di **“Confirmo”**, **“Confirmar”** o **“Sí”**.
3. Comienza una cuenta regresiva de cinco segundos.
4. Si no cancelas, se envía un SMS al contacto principal y se intenta realizar la llamada.
5. Si la ubicación está disponible, el SMS incluye sus coordenadas aproximadas.

Para cancelar puedes decir:

- **“Cancelar”**
- **“Cancela”**
- **“No”**

El SOS necesita un contacto previamente guardado, una SIM activa y permisos de SMS, teléfono y ubicación.

### Conversación local

Si una frase no coincide con un comando básico, VisionGuía intenta responder mediante el modelo local Qwen. Algunos ejemplos:

- **“Explícame qué es un semáforo sonoro”**
- **“Dame instrucciones breves para organizar una habitación”**
- **“Resume este concepto en pocas palabras”**

La conversación estará disponible cuando el diagnóstico anuncie **“LLM listo”**. El modelo puede equivocarse y no tiene permiso para afirmar que vio objetos, leyó textos o ejecutó acciones que no fueron confirmadas por las herramientas reales.

## 6. Estados mostrados por la aplicación

- **IDLE:** lista para recibir una acción.
- **LISTENING:** escuchando el micrófono.
- **PROCESSING:** interpretando un comando.
- **THINKING:** generando una respuesta con el LLM local.
- **SPEAKING:** reproduciendo una respuesta.
- **WALKING:** modo caminar activo.
- **READING:** OCR y códigos activos.
- **SEARCHING_OBJECT:** búsqueda visual de un objeto.
- **SOS:** confirmación o ejecución de emergencia.
- **ERROR:** ocurrió un problema que requiere atención.

## 7. Uso sin Internet

Después de instalar los modelos, funcionan sin Internet:

- comandos de voz mediante Vosk;
- respuestas mediante TTS instalado en Android;
- YOLO;
- OCR y códigos;
- conversación con Qwen;
- Room y preferencias;
- BLE;
- sensores y funciones locales del teléfono.

La descarga inicial de Qwen sí necesita Internet. La conexión WebSocket del ESP32 puede funcionar en una red local sin acceso a Internet.

## 8. Solución rápida de problemas

### No escucha mi voz

- comprueba el permiso de micrófono;
- ejecuta **“diagnóstico”**;
- verifica que Vosk aparezca como listo;
- pulsa **Detener** y vuelve a pulsar **Escuchar**.

### No detecta objetos o no lee texto

- comprueba el permiso de cámara;
- limpia la lente;
- mejora la iluminación;
- mantén el teléfono estable;
- comprueba que YOLO aparezca como listo.

### No responde preguntas abiertas

- conecta el teléfono a Wi-Fi;
- deja suficiente almacenamiento libre;
- espera a que **“diagnóstico”** anuncie **“LLM listo”**;
- los comandos básicos funcionan aunque el LLM todavía se esté descargando.

### El SOS no funciona

- guarda primero el contacto SOS;
- verifica la SIM y la cobertura;
- concede permisos de SMS y teléfono;
- utiliza el formato internacional del número cuando corresponda, por ejemplo `+51` para Perú.

### El ESP32 no conecta

- activa Bluetooth y dispositivos cercanos;
- verifica que el ESP32 use el firmware y UUID documentados;
- para WebSocket, conecta ambos dispositivos a la misma red local;
- revisa la guía `docs/esp32/README.md`.

## 9. Privacidad y seguridad

- Las imágenes de CameraX se procesan y descartan; no se guardan como fotografías.
- El audio se procesa localmente mediante Vosk.
- El LLM se ejecuta en el teléfono.
- Los enlaces QR no se abren automáticamente.
- Las acciones sensibles requieren validación o confirmación.
- Puedes borrar el historial local diciendo **“Borra mi historial”**.

Para información técnica adicional consulta `README.md`, `docs/models.md`, `docs/privacy.md` y `docs/hardware-verification.md`.
