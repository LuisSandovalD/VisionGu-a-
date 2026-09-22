package com.visionguia.vision

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect

data class ColorResult(val name: String, val confidence: Float, val poorLighting: Boolean)

class ColorRecognitionEngine {
    fun recognize(bitmap: Bitmap, region: Rect = Rect(0, 0, bitmap.width, bitmap.height)): ColorResult {
        val safe = Rect(region)
        require(safe.intersect(0, 0, bitmap.width, bitmap.height) && !safe.isEmpty) { "La región no intersecta la imagen" }
        var r = 0L; var g = 0L; var b = 0L; var count = 0
        val step = maxOf(1, minOf(safe.width(), safe.height()) / 40)
        for (y in safe.top until safe.bottom step step) for (x in safe.left until safe.right step step) {
            val color = bitmap.getPixel(x, y); r += Color.red(color); g += Color.green(color); b += Color.blue(color); count++
        }
        val hsv = FloatArray(3); Color.RGBToHSV((r / count).toInt(), (g / count).toInt(), (b / count).toInt(), hsv)
        val name = when {
            hsv[2] < .16f -> "negro"
            hsv[1] < .12f && hsv[2] > .82f -> "blanco"
            hsv[1] < .18f -> "gris"
            hsv[0] < 15 || hsv[0] >= 345 -> "rojo"
            hsv[0] < 38 -> if (hsv[2] < .55f) "marrón" else "naranja"
            hsv[0] < 70 -> "amarillo"
            hsv[0] < 165 -> "verde"
            hsv[0] < 255 -> "azul"
            hsv[0] < 290 -> "morado"
            else -> "rosado"
        }
        val poor = hsv[2] < .25f
        return ColorResult(name, if (poor) .45f else .8f, poor)
    }
}
