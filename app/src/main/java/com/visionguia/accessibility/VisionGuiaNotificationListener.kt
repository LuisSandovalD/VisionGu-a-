package com.visionguia.accessibility

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow

data class AccessibleNotification(val app: String, val title: String, val text: String)

class VisionGuiaNotificationListener : NotificationListenerService() {
    companion object { val notifications = MutableStateFlow<List<AccessibleNotification>>(emptyList()) }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val item = AccessibleNotification(
            app = runCatching { packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString() }.getOrDefault(sbn.packageName),
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
        )
        notifications.value = (notifications.value.filterNot { it.app == item.app && it.title == item.title && it.text == item.text } + item).takeLast(20)
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification) = Unit
}
