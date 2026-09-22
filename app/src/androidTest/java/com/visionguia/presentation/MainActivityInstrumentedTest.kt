package com.visionguia.presentation

import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    @Test fun activityRendersComposeContent() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
                val content = activity.findViewById<ViewGroup>(android.R.id.content)
                assertNotNull(content)
                assertTrue(content.childCount > 0)
                assertTrue(containsComposeView(content))
            }
        }
    }

    private fun containsComposeView(view: View): Boolean {
        if (view.javaClass.name.contains("AndroidComposeView")) return true
        return view is ViewGroup && (0 until view.childCount).any { containsComposeView(view.getChildAt(it)) }
    }
}
