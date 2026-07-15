package com.prajwal.phonesteering.network

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

object UdpStreamer {
    
    @Volatile
    private var latestSteeringAngle: Float = 0f
    
    private var isStreaming = AtomicBoolean(false)
    private var streamingThread: Thread? = null
    private val sequenceNumber = AtomicLong(1)
    
    fun setSteeringAngle(angle: Float) {
        latestSteeringAngle = angle
    }
    
    fun startStreaming(host: String, port: Int, onStatusUpdate: (String) -> Unit) {
        if (isStreaming.getAndSet(true)) {
            return // Already streaming
        }
        
        sequenceNumber.set(1) // Reset sequence on new stream
        
        streamingThread = thread(start = true) {
            var socket: DatagramSocket? = null
            try {
                val address = InetAddress.getByName(host)
                socket = DatagramSocket()
                
                onStatusUpdate("Streaming at target 100 Hz")
                
                while (isStreaming.get()) {
                    val startNanos = System.nanoTime()
                    
                    val angle = latestSteeringAngle
                    val seq = sequenceNumber.getAndIncrement()
                    
                    // Format explicitly as required: exactly three decimal places, locale-independent
                    val payload = String.format(Locale.US, "STEER|%.3f|%d", angle, seq)
                    val bytes = payload.toByteArray(Charsets.UTF_8)
                    
                    val packet = DatagramPacket(bytes, bytes.size, address, port)
                    socket.send(packet)
                    
                    // Target 10ms (10,000,000 ns) per loop
                    val elapsedNanos = System.nanoTime() - startNanos
                    val targetNanos = 10_000_000L
                    val sleepNanos = targetNanos - elapsedNanos
                    
                    if (sleepNanos > 0) {
                        val sleepMillis = sleepNanos / 1_000_000L
                        val sleepNanosRem = (sleepNanos % 1_000_000L).toInt()
                        Thread.sleep(sleepMillis, sleepNanosRem)
                    }
                }
                
                onStatusUpdate("Streaming stopped")
                
            } catch (e: InterruptedException) {
                // Thread interrupted on stop
                onStatusUpdate("Streaming stopped")
            } catch (e: Exception) {
                isStreaming.set(false)
                onStatusUpdate("Send error: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }
    
    fun stopStreaming() {
        isStreaming.set(false)
        streamingThread?.interrupt()
        streamingThread = null
    }
    
    fun isStreamingActive(): Boolean {
        return isStreaming.get()
    }
}
