package com.example.saludencasa.Modelo

import okhttp3.*
import okio.ByteString

class WebSocketManager(private val listener: WebSocketListener) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connectWebSocket(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun disconnectWebSocket() {
        webSocket?.cancel()
    }
}