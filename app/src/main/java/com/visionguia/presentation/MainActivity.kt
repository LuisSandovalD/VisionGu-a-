package com.visionguia.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.visionguia.core.model.DetectedObject
import com.visionguia.vision.YoloObjectDetector
import com.visionguia.vision.OnDeviceOcrEngine
import com.visionguia.vision.OnDeviceBarcodeEngine
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { VisionGuiaScreen() } }
    }
}

@Composable
private fun VisionGuiaScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var permissionTick by remember { mutableIntStateOf(0) }
    val permissions = remember {
        buildList {
            add(Manifest.permission.CAMERA); add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= 31) { add(Manifest.permission.BLUETOOTH_SCAN); add(Manifest.permission.BLUETOOTH_CONNECT) }
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.CALL_PHONE); add(Manifest.permission.SEND_SMS)
        }.toTypedArray()
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionTick++ }
    val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val bluetoothGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    val allPermissionsGranted = permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    VisionGuiaContent(
        state = state,
        cameraGranted = cameraGranted,
        allPermissionsGranted = allPermissionsGranted,
        envMode = state.state == com.visionguia.core.model.AssistantState.ANALYZING_ENVIRONMENT,
        onRequestPermissions = { launcher.launch(permissions) },
        onStartListening = viewModel::startListening,
        onStop = viewModel::stopAll,
        onRepeat = viewModel::repeat,
        onSaveContact = viewModel::saveEmergencyContact,
        onDetections = viewModel::onDetections,
        onText = viewModel::onOcrText,
    )
    LaunchedEffect(permissionTick) {
        // Actualiza el estado después de responder a los permisos de Android.
    }

    LaunchedEffect(bluetoothGranted) {
        viewModel.onBluetoothPermissionChanged(bluetoothGranted)
    }
}

@Composable
internal fun VisionGuiaContent(
    state: MainUiState,
    cameraGranted: Boolean,
    allPermissionsGranted: Boolean,
    envMode: Boolean,
    onRequestPermissions: () -> Unit,
    onStartListening: () -> Unit,
    onStop: () -> Unit,
    onRepeat: () -> Unit,
    onSaveContact: (String, String) -> Unit,
    onDetections: (List<DetectedObject>) -> Unit,
    onText: (String) -> Unit,
) {
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    Surface(color = Color.Black, contentColor = Color.White) {
        Box(Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onDoubleTap = { onStartListening() }, onLongPress = { onRepeat() })
        }.semantics {
            contentDescription = "Superficie principal. Doble toque para escuchar. Pulsación prolongada para repetir."
            stateDescription = state.state.name
        }) {
            if (cameraGranted) CameraPreview(Modifier.fillMaxSize(), state.state == com.visionguia.core.model.AssistantState.READING, envMode, onDetections, onText)
            Box(Modifier.fillMaxSize().background(Color(0x99000000)))
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("VISIONGUÍA", fontSize = 30.sp, color = Color(0xFFFFD600), modifier = Modifier.semantics { heading() })
                    Text(state.state.name, fontSize = 20.sp, modifier = Modifier.semantics { stateDescription = state.state.name })
                    Text(state.message, fontSize = 22.sp)
                    if (state.partialText.isNotBlank()) Text("Escuché: ${state.partialText}", fontSize = 18.sp)
                    Text(state.modelStatus, fontSize = 14.sp, color = Color.LightGray)
                    Text("Objetos detectados: ${state.detectionCount}", fontSize = 14.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!allPermissionsGranted) Button(onClick = onRequestPermissions, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Solicitar permisos necesarios mediante Android" }) {
                        Text("Conceder permisos", fontSize = 18.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(contactName, { contactName = it }, label = { Text("Contacto SOS") }, modifier = Modifier.weight(1f).semantics { contentDescription = "Nombre del contacto de emergencia" })
                        OutlinedTextField(contactPhone, { contactPhone = it }, label = { Text("Teléfono") }, modifier = Modifier.weight(1f).semantics { contentDescription = "Teléfono del contacto de emergencia" })
                    }
                    Button(onClick = { onSaveContact(contactName, contactPhone) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar contacto SOS") }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray), modifier = Modifier.semantics { role = Role.Button; contentDescription = "Detener escucha y voz" }) { Text("Detener") }
                        Button(onClick = onStartListening, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black), modifier = Modifier.size(112.dp).semantics { role = Role.Button; contentDescription = "Comenzar a escuchar" }) { Text("Escuchar") }
                        Button(onClick = onRepeat, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray), modifier = Modifier.semantics { role = Role.Button; contentDescription = "Repetir último mensaje" }) { Text("Repetir") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(modifier: Modifier, readingMode: Boolean, envMode: Boolean, onDetections: (List<DetectedObject>) -> Unit, onText: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val model = remember { File(context.filesDir, "models/yolov8n.onnx") }
    val detector = remember(model.exists()) {
        if (model.isFile) runCatching { YoloObjectDetector(model, COCO_LABELS) }.getOrNull() else null
    }
    val ocr = remember { OnDeviceOcrEngine() }
    val barcode = remember { OnDeviceBarcodeEngine() }
    val envAnalyzer = remember { com.visionguia.vision.ColorAndLightAnalyzer() }
    AndroidView(factory = { PreviewView(it).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }, modifier = modifier) { view ->
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
            var lastFrame = 0L
            analysis.setAnalyzer(executor) { image ->
                try {
                    if (System.currentTimeMillis() - lastFrame >= if (readingMode || envMode) 1_500 else 750) {
                        lastFrame = System.currentTimeMillis()
                        val bitmap = image.toBitmapRgba()
                        if (readingMode) {
                            val barcodeBitmap = bitmap.copy(bitmap.config ?: android.graphics.Bitmap.Config.ARGB_8888, false)
                            ocr.process(bitmap) { it.getOrNull()?.let(onText); bitmap.recycle() }
                            barcode.process(barcodeBitmap) { result ->
                                result.getOrNull()?.firstOrNull()?.let { code ->
                                    val warning = if (code.isUrl) "Enlace detectado, no se abrirá automáticamente: " else "Código detectado: "
                                    onText(warning + code.displayValue.take(500))
                                }
                                barcodeBitmap.recycle()
                            }
                        } else if (envMode) {
                            val envResult = envAnalyzer.analyze(bitmap)
                            onText(envResult)
                            bitmap.recycle()
                        }
                        else { if (detector != null) onDetections(detector.detect(bitmap, lastFrame)); bitmap.recycle() }
                    }
                } finally { image.close() }
            }
            runCatching { provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis) }
        }, ContextCompat.getMainExecutor(context))
    }
    DisposableEffect(Unit) { onDispose { executor.shutdown(); detector?.close(); ocr.close(); barcode.close() } }
}

private fun androidx.camera.core.ImageProxy.toBitmapRgba(): android.graphics.Bitmap {
    val plane = planes[0]
    val buffer = plane.buffer.apply { rewind() }
    val bytes = ByteArray(buffer.remaining()).also(buffer::get)
    val pixels = IntArray(width * height)
    for (y in 0 until height) for (x in 0 until width) {
        val i = y * plane.rowStride + x * plane.pixelStride
        if (i + 3 < bytes.size) pixels[y * width + x] = android.graphics.Color.argb(bytes[i + 3].toInt() and 255, bytes[i].toInt() and 255, bytes[i + 1].toInt() and 255, bytes[i + 2].toInt() and 255)
    }
    return android.graphics.Bitmap.createBitmap(pixels, width, height, android.graphics.Bitmap.Config.ARGB_8888)
}

private val COCO_LABELS = listOf("persona","bicicleta","auto","motocicleta","avión","autobús","tren","camión","bote","semáforo","hidrante","señal de pare","parquímetro","banca","pájaro","gato","perro","caballo","oveja","vaca","elefante","oso","cebra","jirafa","mochila","paraguas","bolso","corbata","maleta","frisbee","esquís","snowboard","pelota","cometa","bate","guante","skateboard","tabla de surf","raqueta","botella","copa","taza","tenedor","cuchillo","cuchara","tazón","plátano","manzana","sándwich","naranja","brócoli","zanahoria","hot dog","pizza","dona","pastel","silla","sofá","planta","cama","mesa","inodoro","televisor","laptop","mouse","control remoto","teclado","celular","microondas","horno","tostadora","lavabo","refrigerador","libro","reloj","florero","tijeras","oso de peluche","secador","cepillo de dientes")
