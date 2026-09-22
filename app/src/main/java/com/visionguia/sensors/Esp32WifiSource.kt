package com.visionguia.sensors

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class Esp32WifiSource(
    private val client: OkHttpClient,
    private val endpoint: String,
    private val parser: Esp32ProtocolParser = Esp32ProtocolParser(),
) : SensorSource {
    private val mutable = MutableSharedFlow<com.visionguia.core.model.SensorReading>(extraBufferCapacity = 32)
    override val readings: SharedFlow<com.visionguia.core.model.SensorReading> = mutable
    private var socket: WebSocket? = null

    override suspend fun start() {
        require(endpoint.startsWith("ws://192.168.") || endpoint.startsWith("ws://10.") || endpoint.startsWith("ws://172.")) {
            "ESP32 Wi-Fi solo admite una dirección de red local"
        }
        socket = client.newWebSocket(Request.Builder().url(endpoint).build(), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { webSocket.send("{\"v\":1,\"command\":\"subscribe\"}") }
            override fun onMessage(webSocket: WebSocket, text: String) { parser.parse(text, "esp32-wifi").getOrNull()?.let(mutable::tryEmit) }
        })
    }
    override suspend fun stop() { socket?.close(1000, "VisionGuía detenido"); socket = null }
}
