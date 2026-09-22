package com.visionguia.data.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class ModelSpec(
    val id: String,
    val fileName: String,
    val url: String,
    val sha256: String,
    val minimumFreeBytes: Long,
)

sealed interface ModelDownloadState {
    data class Progress(val bytes: Long, val total: Long?) : ModelDownloadState
    data class Ready(val file: File) : ModelDownloadState
    data class Failed(val reason: String) : ModelDownloadState
}

@Singleton
class ModelManager @Inject constructor(
    @ApplicationContext context: Context,
    private val client: OkHttpClient,
) {
    private val directory = File(context.filesDir, "models").apply { mkdirs() }

    fun modelFile(spec: ModelSpec) = File(directory, spec.fileName)

    suspend fun isVerified(spec: ModelSpec): Boolean = withContext(Dispatchers.IO) {
        val file = modelFile(spec)
        file.isFile && sha256(file).equals(spec.sha256, ignoreCase = true)
    }

    fun download(spec: ModelSpec): Flow<ModelDownloadState> = flow {
        require(spec.url.startsWith("https://")) { "Solo se permiten descargas HTTPS" }
        require(spec.sha256.matches(Regex("[a-fA-F0-9]{64}"))) { "SHA-256 inválido" }
        val target = modelFile(spec)
        if (target.parentFile?.usableSpace.orZero() < spec.minimumFreeBytes) {
            emit(ModelDownloadState.Failed("Almacenamiento insuficiente")); return@flow
        }
        val partial = File(target.path + ".part")
        if (isVerified(spec)) {
            partial.delete()
            emit(ModelDownloadState.Ready(target)); return@flow
        }
        val offset = partial.takeIf(File::exists)?.length() ?: 0L
        val request = Request.Builder().url(spec.url).apply { if (offset > 0) header("Range", "bytes=$offset-") }.build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) error("HTTP ${response.code}")
                val append = offset > 0 && response.code == 206
                val base = if (append) offset else 0L
                val total = response.body!!.contentLength().takeIf { it >= 0 }?.plus(base)
                java.io.RandomAccessFile(partial, "rw").use { output ->
                    if (append) output.seek(offset) else output.setLength(0)
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var written = base
                    response.body!!.byteStream().use { input ->
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            written += count
                            emit(ModelDownloadState.Progress(written, total))
                        }
                    }
                }
            }
            if (!sha256(partial).equals(spec.sha256, ignoreCase = true)) {
                partial.delete(); emit(ModelDownloadState.Failed("La suma SHA-256 no coincide")); return@flow
            }
            if (target.exists()) check(target.delete()) { "No se pudo reemplazar el modelo anterior" }
            check(partial.renameTo(target)) { "No se pudo finalizar el archivo" }
            emit(ModelDownloadState.Ready(target))
        } catch (e: Exception) {
            emit(ModelDownloadState.Failed(e.message ?: "Descarga interrumpida"))
        }
    }.flowOn(Dispatchers.IO)

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) { val read = input.read(buffer); if (read < 0) break; digest.update(buffer, 0, read) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    private fun Long?.orZero() = this ?: 0L
}
