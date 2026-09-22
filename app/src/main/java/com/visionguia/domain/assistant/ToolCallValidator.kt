package com.visionguia.domain.assistant

enum class ConfirmationPolicy { NONE, RECOMMENDED, REQUIRED }
data class ToolCall(val name: String, val arguments: Map<String, String> = emptyMap())
data class ValidatedToolCall(val call: ToolCall, val confirmation: ConfirmationPolicy)

class ToolCallValidator {
    private val schemas = mapOf(
        "describe_scene" to emptySet<String>(),
        "find_object" to setOf("name"),
        "read_text" to emptySet(),
        "call_contact" to setOf("contact"),
        "send_sms" to setOf("contact", "message"),
        "share_location" to setOf("contact"),
        "activate_sos" to emptySet(),
        "open_app" to setOf("package"),
    )

    fun validate(call: ToolCall): Result<ValidatedToolCall> = runCatching {
        val expected = schemas[call.name] ?: error("Herramienta no permitida: ${call.name}")
        require(call.arguments.keys == expected) { "Argumentos inválidos" }
        require(call.arguments.values.none { it.isBlank() || it.length > 500 }) { "Valor inválido" }
        val policy = when (call.name) {
            "call_contact", "send_sms", "share_location", "activate_sos" -> ConfirmationPolicy.REQUIRED
            "open_app" -> ConfirmationPolicy.RECOMMENDED
            else -> ConfirmationPolicy.NONE
        }
        ValidatedToolCall(call, policy)
    }
}
