package com.visionguia.vision

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.graphics.RectF
import com.visionguia.core.model.DetectedObject
import java.io.Closeable
import java.io.File
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

class YoloObjectDetector(
    modelFile: File,
    private val labels: List<String>,
    private val confidenceThreshold: Float = .45f,
    private val iouThreshold: Float = .45f,
) : Closeable {
    private val environment = OrtEnvironment.getEnvironment()
    private val sessionOptions = OrtSession.SessionOptions().apply { setIntraOpNumThreads(2) }
    private val session = environment.createSession(modelFile.absolutePath, sessionOptions)
    private val inputName = session.inputNames.first()
    private val inputShape = session.inputInfo.getValue(inputName).info.let { it as ai.onnxruntime.TensorInfo }.shape
    private val inputHeight = inputShape[2].toInt()
    private val inputWidth = inputShape[3].toInt()

    fun detect(bitmap: Bitmap, timestamp: Long = System.currentTimeMillis()): List<DetectedObject> {
        val input = preprocess(bitmap)
        OnnxTensor.createTensor(environment, input, longArrayOf(1, 3, inputHeight.toLong(), inputWidth.toLong())).use { tensor ->
            session.run(mapOf(inputName to tensor)).use { result ->
                @Suppress("UNCHECKED_CAST")
                val output = result[0].value as Array<Array<FloatArray>>
                return nms(decodeYoloV8(output[0], timestamp))
            }
        }
    }

    private fun preprocess(source: Bitmap): FloatBuffer {
        val bitmap = Bitmap.createScaledBitmap(source, inputWidth, inputHeight, true)
        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val data = FloatArray(pixels.size * 3)
        pixels.forEachIndexed { index, color ->
            data[index] = ((color shr 16) and 0xff) / 255f
            data[pixels.size + index] = ((color shr 8) and 0xff) / 255f
            data[pixels.size * 2 + index] = (color and 0xff) / 255f
        }
        if (bitmap !== source) bitmap.recycle()
        return FloatBuffer.wrap(data)
    }

    private fun decodeYoloV8(channels: Array<FloatArray>, timestamp: Long): List<DetectedObject> {
        require(channels.size >= 5) { "La salida YOLO no contiene clases" }
        val boxes = channels[0].size
        return buildList {
            for (i in 0 until boxes) {
                var bestClass = 0; var bestScore = 0f
                for (c in 4 until channels.size) if (channels[c][i] > bestScore) { bestScore = channels[c][i]; bestClass = c - 4 }
                if (bestScore < confidenceThreshold || bestClass !in labels.indices) continue
                val cx = channels[0][i]; val cy = channels[1][i]; val width = channels[2][i]; val height = channels[3][i]
                val box = RectF(
                    (cx - width / 2).coerceIn(0f, inputWidth.toFloat()),
                    (cy - height / 2).coerceIn(0f, inputHeight.toFloat()),
                    (cx + width / 2).coerceIn(0f, inputWidth.toFloat()),
                    (cy + height / 2).coerceIn(0f, inputHeight.toFloat()),
                )
                if (box.width() > 1 && box.height() > 1) add(DetectedObject(label = labels[bestClass], confidence = bestScore, boundingBox = box, timestamp = timestamp))
            }
        }
    }

    private fun nms(items: List<DetectedObject>): List<DetectedObject> {
        val sorted = items.sortedByDescending { it.confidence }.toMutableList(); val selected = mutableListOf<DetectedObject>()
        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0); selected += best
            sorted.removeAll { it.label == best.label && iou(it.boundingBox, best.boundingBox) >= iouThreshold }
        }
        return selected
    }

    private fun iou(a: RectF, b: RectF): Float {
        val intersection = max(0f, min(a.right, b.right) - max(a.left, b.left)) * max(0f, min(a.bottom, b.bottom) - max(a.top, b.top))
        return intersection / (a.width() * a.height() + b.width() * b.height() - intersection).coerceAtLeast(.001f)
    }

    override fun close() { session.close(); sessionOptions.close() }
}
