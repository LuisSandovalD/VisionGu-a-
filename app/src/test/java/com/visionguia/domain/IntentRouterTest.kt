package com.visionguia.domain

import com.visionguia.domain.assistant.IntentRouter
import com.visionguia.domain.assistant.UserIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentRouterTest {
    private val router = IntentRouter()
    @Test fun routesBasicCommandsWithoutLlm() {
        assertEquals(UserIntent.Time, router.route("¿Qué hora es?"))
        assertEquals(UserIntent.Battery, router.route("Dime la batería"))
        assertEquals(UserIntent.Sos, router.route("necesito ayuda"))
        assertEquals(UserIntent.DescribeAhead, router.route("que hay adelante mio"))
        assertEquals(UserIntent.DescribeAhead, router.route("dime que hay delante"))
    }
    @Test fun extractsObjectAndFallsBackToConversation() {
        assertEquals(UserIntent.FindObject("llaves"), router.route("Encuentra mis llaves"))
        assertTrue(router.route("Cuéntame algo") is UserIntent.Conversation)
    }
}
