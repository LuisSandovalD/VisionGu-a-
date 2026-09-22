package com.visionguia.data.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

data class BundledModels(val voskDirectory: File, val yoloFile: File)

@Singleton
class BundledModelInstaller @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun install(): Result<BundledModels> = withContext(Dispatchers.IO) {
        installMutex.withLock {
            runCatching {
                val modelRoot = File(context.filesDir, "models").apply { mkdirs() }
                val vosk = installVosk(modelRoot)
                val yolo = installFile(
                    asset = "models/yolov8n.onnx",
                    destination = File(modelRoot, "yolov8n.onnx"),
                    expectedSha256 = YOLO_SHA256,
                )
                BundledModels(vosk, yolo)
            }
        }
    }

    private fun installVosk(root: File): File {
        val destination = File(root, "vosk-es")
        val marker = File(destination, ".sha256")
        if (destination.isDirectory && marker.readTextOrNull() == VOSK_SHA256) return destination
        val archive = File(context.cacheDir, "vosk-es.zip")
        context.assets.open("models/vosk-model-small-es-0.42.zip").use { input -> archive.outputStream().use(input::copyTo) }
        require(sha256(archive) == VOSK_SHA256) { "El modelo Vosk incluido no superó SHA-256" }
        val staging = File(root, "vosk-es.installing")
        staging.deleteRecursively(); staging.mkdirs()
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val relative = entry.name.substringAfter('/', "").takeIf(String::isNotBlank) ?: continue
                val output = File(staging, relative).canonicalFile
                require(output.path.startsWith(staging.canonicalPath + File.separator)) { "Entrada ZIP insegura" }
                if (entry.isDirectory) output.mkdirs() else { output.parentFile?.mkdirs(); output.outputStream().use(zip::copyTo) }
                zip.closeEntry()
            }
        }
        require(File(staging, "am/final.mdl").isFile && File(staging, "conf/model.conf").isFile) { "Estructura Vosk incompleta" }
        destination.deleteRecursively()
        require(staging.renameTo(destination)) { "No se pudo activar Vosk" }
        marker.writeText(VOSK_SHA256)
        archive.delete()
        return destination
    }

    private fun installFile(asset: String, destination: File, expectedSha256: String): File {
        if (destination.isFile && sha256(destination) == expectedSha256) return destination
        val temporary = File(destination.path + ".installing")
        context.assets.open(asset).use { input -> temporary.outputStream().use(input::copyTo) }
        require(sha256(temporary) == expectedSha256) { "El modelo incluido no superó SHA-256: $asset" }
        destination.delete()
        require(temporary.renameTo(destination)) { "No se pudo activar $asset" }
        return destination
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun File.readTextOrNull(): String? = runCatching { readText() }.getOrNull()

    companion object {
        private val installMutex = Mutex()
        const val VOSK_SHA256 = "09b239888f633ef2f0b4e09736e3d9936acfd810bc65d53fad45261762c6511f"
        const val YOLO_SHA256 = "8952e7feac460ea79b558c968f1bb1cce9cc8182adec963010e6a72a9e3d947d"
    }
}
