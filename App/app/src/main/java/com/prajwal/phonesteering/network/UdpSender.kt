package com.prajwal.phonesteering.network

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

object UdpSender {

    /**
     * Sends a single UDP packet off the main thread.
     * Returns the result via the provided callback.
     */
    fun sendPacketAsync(host: String, port: Int, payload: String, onResult: (Result<Unit>) -> Unit) {
        thread(start = true) {
            var socket: DatagramSocket? = null
            try {
                val address = InetAddress.getByName(host)
                socket = DatagramSocket()
                val bytes = payload.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(bytes, bytes.size, address, port)
                socket.send(packet)
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            } finally {
                socket?.close()
            }
        }
    }
}
