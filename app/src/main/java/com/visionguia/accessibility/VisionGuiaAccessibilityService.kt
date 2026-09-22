package com.visionguia.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class VisionGuiaAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Deliberately do not consume normal volume events. A configurable long-press listener
        // can be enabled in a future release without breaking the phone's volume controls.
        return false
    }
}
