package com.visionguia.vision

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.Closeable

data class BarcodeResult(val format: Int, val displayValue: String, val isUrl: Boolean)

class OnDeviceOcrEngine : Closeable {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    fun process(bitmap: Bitmap, result: (Result<String>) -> Unit) {
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result(Result.success(it.text.trim())) }
            .addOnFailureListener { result(Result.failure(it)) }
    }
    override fun close() = recognizer.close()
}

class OnDeviceBarcodeEngine : Closeable {
    private val scanner = BarcodeScanning.getClient()
    fun process(bitmap: Bitmap, result: (Result<List<BarcodeResult>>) -> Unit) {
        scanner.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { codes ->
                result(Result.success(codes.mapNotNull { code ->
                    val value = code.displayValue?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                    BarcodeResult(code.format, value, code.valueType == com.google.mlkit.vision.barcode.common.Barcode.TYPE_URL)
                }))
            }
            .addOnFailureListener { result(Result.failure(it)) }
    }
    override fun close() = scanner.close()
}
