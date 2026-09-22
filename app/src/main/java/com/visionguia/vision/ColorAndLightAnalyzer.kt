package com.visionguia.vision

import android.graphics.Bitmap
import android.graphics.Color

class ColorAndLightAnalyzer {
    fun analyze(bitmap: Bitmap): String {
        val width = bitmap.width
        val height = bitmap.height
        val centerX = width / 2
        val centerY = height / 2
        val boxSize = width.coerceAtMost(height) / 4

        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var count = 0

        for (x in centerX - boxSize until centerX + boxSize step 5) {
            for (y in centerY - boxSize until centerY + boxSize step 5) {
                if (x in 0 until width && y in 0 until height) {
                    val pixel = bitmap.getPixel(x, y)
                    sumR += Color.red(pixel)
                    sumG += Color.green(pixel)
                    sumB += Color.blue(pixel)
                    count++
                }
            }
        }

        if (count == 0) return "No se pudo analizar el color."

        val r = (sumR / count).toInt()
        val g = (sumG / count).toInt()
        val b = (sumB / count).toInt()

        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)

        val hue = hsv[0]
        val sat = hsv[1]
        val valBrightness = hsv[2]

        val lightDescription = when {
            valBrightness < 0.2f -> "muy oscuro"
            valBrightness < 0.4f -> "oscuro"
            valBrightness > 0.85f -> "muy iluminado"
            else -> "iluminación normal"
        }

        val colorDescription = when {
            sat < 0.15f && valBrightness > 0.8f -> "blanco"
            sat < 0.15f && valBrightness < 0.2f -> "negro"
            sat < 0.15f -> "gris"
            hue < 15f || hue > 345f -> "rojo"
            hue < 45f -> "naranja o marrón"
            hue < 75f -> "amarillo"
            hue < 165f -> "verde"
            hue < 265f -> "azul"
            hue < 315f -> "morado"
            else -> "rosado"
        }

        return "El ambiente está $lightDescription. El color predominante al frente es $colorDescription."
    }
}
