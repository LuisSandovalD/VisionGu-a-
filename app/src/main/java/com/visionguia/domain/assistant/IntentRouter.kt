package com.visionguia.domain.assistant

import java.text.Normalizer

sealed interface UserIntent {
    data object Help : UserIntent
    data object Repeat : UserIntent
    data object Stop : UserIntent
    data object Time : UserIntent
    data object Date : UserIntent
    data object Battery : UserIntent
    data object Flashlight : UserIntent
    data object WalkMode : UserIntent
    data object DescribeAhead : UserIntent
    data object ReadText : UserIntent
    data object AnalyzeEnvironment : UserIntent
    data object Sos : UserIntent
    data object Diagnostics : UserIntent
    data class FindObject(val name: String) : UserIntent
    data class ChangeSpeechRate(val faster: Boolean) : UserIntent
    data class Conversation(val text: String) : UserIntent
}

class IntentRouter {
    fun route(raw: String): UserIntent {
        val text = normalize(raw)
        return when {
            text == "ayuda" || "que puedes hacer" in text -> UserIntent.Help
            text.startsWith("repite") || text == "repitelo" -> UserIntent.Repeat
            text in setOf("calla", "silencio", "detente") -> UserIntent.Stop
            "que hora" in text || text == "hora" -> UserIntent.Time
            "que fecha" in text || text == "fecha" -> UserIntent.Date
            "bateria" in text -> UserIntent.Battery
            "linterna" in text -> UserIntent.Flashlight
            "modo caminar" in text -> UserIntent.WalkMode
            text in setOf(
                "que hay adelante mio",
                "que hay delante mio",
                "que tengo adelante",
                "que tengo delante",
                "dime que hay adelante",
                "dime que hay delante",
                "describe lo que hay adelante",
            ) -> UserIntent.DescribeAhead
            text.startsWith("lee") || "leer texto" in text -> UserIntent.ReadText
            "color" in text || "luz" in text || "iluminacion" in text || "monedas" in text -> UserIntent.AnalyzeEnvironment
            text == "sos" || "emergencia" in text || "necesito ayuda" in text -> UserIntent.Sos
            "diagnostico" in text -> UserIntent.Diagnostics
            text.startsWith("encuentra ") -> UserIntent.FindObject(text.removePrefix("encuentra ").removePrefix("mis ").removePrefix("mi ").trim())
            "habla mas rapido" in text -> UserIntent.ChangeSpeechRate(true)
            "habla mas lento" in text -> UserIntent.ChangeSpeechRate(false)
            else -> UserIntent.Conversation(raw.trim())
        }
    }

    private fun normalize(value: String): String = Normalizer.normalize(value.lowercase().trim(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .replace("[¿?¡!.,]".toRegex(), "")
        .replace("\\s+".toRegex(), " ")
}
