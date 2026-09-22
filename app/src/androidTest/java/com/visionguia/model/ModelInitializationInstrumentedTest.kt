package com.visionguia.model

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionguia.data.model.BundledModelInstaller
import com.visionguia.vision.YoloObjectDetector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.vosk.Model

@RunWith(AndroidJUnit4::class)
class ModelInitializationInstrumentedTest {
    @Test fun bundledVoskAndYoloInitializeAndRunOffline() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val installed = BundledModelInstaller(context).install().getOrThrow()
        assertTrue(installed.voskDirectory.isDirectory)
        assertTrue(installed.yoloFile.isFile)

        val vosk = Model(installed.voskDirectory.absolutePath)
        vosk.close()

        val labels = List(80) { "clase-$it" }
        val detector = YoloObjectDetector(installed.yoloFile, labels)
        val bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888)
        val detections = detector.detect(bitmap)
        assertEquals(0, detections.size)
        bitmap.recycle()
        detector.close()
    }
}
