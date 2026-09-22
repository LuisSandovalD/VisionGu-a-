package com.visionguia.domain

import com.visionguia.domain.assistant.ConfirmationPolicy
import com.visionguia.domain.assistant.ToolCall
import com.visionguia.domain.assistant.ToolCallValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolCallValidatorTest {
    private val validator = ToolCallValidator()
    @Test fun sensitiveCallsRequireConfirmation() {
        val result = validator.validate(ToolCall("call_contact", mapOf("contact" to "María"))).getOrThrow()
        assertEquals(ConfirmationPolicy.REQUIRED, result.confirmation)
    }
    @Test fun rejectsUnknownToolsAndExtraArguments() {
        assertTrue(validator.validate(ToolCall("shell", emptyMap())).isFailure)
        assertTrue(validator.validate(ToolCall("read_text", mapOf("url" to "x"))).isFailure)
    }
}
