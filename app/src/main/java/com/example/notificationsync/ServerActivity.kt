package com.example.notificationsync

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.PrintWriter
import java.net.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Actividad que actúa como servidor TCP para enviar notificaciones
 */
class ServerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ServerActivity"
    }

    private lateinit var tvStatus: TextView
    private lateinit var tvClients: TextView
    private lateinit var tvMessages: TextView
    private lateinit var btnStop: Button

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var port = 8888
    private val connectedClients = CopyOnWriteArrayList<ClientHandler>()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        setupUI()
        getIntentData()
        startServer()
        setupNotificationListener()
    }

    /**
     * Configura la interfaz de usuario
     */
    private fun setupUI() {
        tvStatus = findViewById(R.id.tvStatus)
        tvClients = findViewById(R.id.tvClients)
        tvMessages = findViewById(R.id.tvMessages)
        btnStop = findViewById(R.id.btnStop)

        btnStop.setOnClickListener {
            stopServer()
            finish()
        }
    }

    /**
     * Obtiene los datos del intent
     */
    private fun getIntentData() {
        port = intent.getIntExtra("port", 8888)
        val selectedApps = intent.getStringArrayListExtra("selectedApps")?.toSet() ?: emptySet()

        // Configurar las apps seleccionadas en el listener
        NotificationListener.selectedApps = selectedApps
    }

    /**
     * Configura el listener de notificaciones
     */
    private fun setupNotificationListener() {
        NotificationListener.onNotificationReceived = { appName, title, text ->
            val message = if (text.isNotEmpty()) {
                "[$appName] $title: $text"
            } else {
                "[$appName] $title"
            }

            mainHandler.post {
                addMessageToUI(message)
                broadcastMessage(message)
            }
        }
    }

    /**
     * Inicia el servidor TCP
     */
    private fun startServer() {
        Thread {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true

                mainHandler.post {
                    val ipAddress = getLocalIpAddress()
                    tvStatus.text = "Servidor activo en $ipAddress:$port"
                    updateClientsCount()
                }

                Log.d(TAG, "Servidor iniciado en puerto $port")

                // Aceptar conexiones de clientes
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { socket ->
                            val clientHandler = ClientHandler(socket) {
                                connectedClients.remove(it)
                                mainHandler.post { updateClientsCount() }
                            }

                            connectedClients.add(clientHandler)

                            mainHandler.post {
                                updateClientsCount()
                                addMessageToUI("Cliente conectado: ${socket.inetAddress.hostAddress}")
                            }

                            Log.d(TAG, "Cliente conectado: ${socket.inetAddress.hostAddress}")
                        }
                    } catch (e: SocketException) {
                        if (isRunning) {
                            Log.e(TAG, "Error aceptando conexión", e)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error iniciando servidor", e)
                mainHandler.post {
                    Toast.makeText(this@ServerActivity, "Error iniciando servidor: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    /**
     * Detiene el servidor
     */
    private fun stopServer() {
        isRunning = false

        // Cerrar conexiones de clientes
        connectedClients.forEach { it.close() }
        connectedClients.clear()

        // Cerrar servidor
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error cerrando servidor", e)
        }

        // Limpiar callback del listener
        NotificationListener.onNotificationReceived = null

        Log.d(TAG, "Servidor detenido")
    }

    /**
     * Envía un mensaje a todos los clientes conectados
     */
    private fun broadcastMessage(message: String) {
        val clientsToRemove = mutableListOf<ClientHandler>()

        for (client in connectedClients) {
            try {
                client.sendMessage(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando mensaje a cliente", e)
                clientsToRemove.add(client)
            }
        }

        // Remover clientes desconectados
        connectedClients.removeAll(clientsToRemove)
        if (clientsToRemove.isNotEmpty()) {
            updateClientsCount()
        }
    }

    /**
     * Actualiza el contador de clientes conectados
     */
    private fun updateClientsCount() {
        tvClients.text = "Clientes conectados: ${connectedClients.size}"
    }

    /**
     * Añade un mensaje a la interfaz
     */
    private fun addMessageToUI(message: String) {
        val currentText = tvMessages.text.toString()
        val newText = if (currentText.isEmpty()) {
            message
        } else {
            "$currentText\n$message"
        }
        tvMessages.text = newText
    }

    /**
     * Obtiene la IP local del dispositivo
     */
    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses

                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "Unknown"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo IP", e)
        }
        return "Unknown"
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }
}

/**
 * Maneja la conexión con un cliente específico
 */
class ClientHandler(
    private val socket: Socket,
    private val onDisconnected: (ClientHandler) -> Unit
) {
    private val writer: PrintWriter = PrintWriter(socket.getOutputStream(), true)

    /**
     * Envía un mensaje al cliente
     */
    fun sendMessage(message: String) {
        if (!socket.isClosed) {
            writer.println(message)
            if (writer.checkError()) {
                throw IOException("Error enviando mensaje")
            }
        }
    }

    /**
     * Cierra la conexión con el cliente
     */
    fun close() {
        try {
            writer.close()
            socket.close()
        } catch (e: Exception) {
            Log.e("ClientHandler", "Error cerrando conexión", e)
        }
        onDisconnected(this)
    }
}