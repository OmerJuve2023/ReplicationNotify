package com.example.notificationsync

import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket

/**
 * Actividad que actúa como cliente TCP para recibir notificaciones
 */
class ClientActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ClientActivity"
        private const val CHANNEL_ID = "notification_share_channel"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    private lateinit var etServerIP: EditText
    private lateinit var etServerPort: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvMessages: TextView

    private var clientSocket: Socket? = null
    private var isConnected = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var notificationCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        setupUI()
        createNotificationChannel()
    }

    /**
     * Configura la interfaz de usuario
     */
    private fun setupUI() {
        etServerIP = findViewById(R.id.etServerIP)
        etServerPort = findViewById(R.id.etServerPort)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvMessages = findViewById(R.id.tvMessages)

        // Valores por defecto
        etServerPort.setText("8888")

        btnConnect.setOnClickListener {
            connectToServer()
        }

        btnDisconnect.setOnClickListener {
            disconnectFromServer()
        }

        updateUIState(false)
    }

    /**
     * Crea el canal de notificaciones
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones Compartidas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones recibidas desde otros dispositivos"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Se conecta al servidor
     */
    private fun connectToServer() {
        val serverIP = etServerIP.text.toString().trim()
        val serverPortText = etServerPort.text.toString().trim()

        if (serverIP.isEmpty()) {
            Toast.makeText(this, "Ingresa la IP del servidor", Toast.LENGTH_SHORT).show()
            return
        }

        val serverPort = try {
            serverPortText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Puerto debe ser un número", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                clientSocket = Socket(serverIP, serverPort)
                isConnected = true

                mainHandler.post {
                    updateUIState(true)
                    tvStatus.text = "Conectado a $serverIP:$serverPort"
                    addMessageToUI("Conectado al servidor")
                }

                Log.d(TAG, "Conectado al servidor $serverIP:$serverPort")

                // Escuchar mensajes del servidor
                listenForMessages()

            } catch (e: Exception) {
                Log.e(TAG, "Error conectando al servidor", e)
                mainHandler.post {
                    Toast.makeText(this@ClientActivity, "Error conectando: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    updateUIState(false)
                }
            }
        }.start()
    }

    /**
     * Se desconecta del servidor
     */
    private fun disconnectFromServer() {
        isConnected = false

        try {
            clientSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error cerrando conexión", e)
        }

        updateUIState(false)
        tvStatus.text = "Desconectado"
        addMessageToUI("Desconectado del servidor")

        Log.d(TAG, "Desconectado del servidor")
    }

    /**
     * Escucha mensajes del servidor
     */
    private fun listenForMessages() {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))

                while (isConnected && clientSocket?.isConnected == true) {
                    val message = reader.readLine()
                    if (message != null) {
                        mainHandler.post {
                            addMessageToUI("Recibido: $message")
                            showNotification(message)
                        }
                        Log.d(TAG, "Mensaje recibido: $message")
                    } else {
                        // Conexión cerrada por el servidor
                        break
                    }
                }

            } catch (e: IOException) {
                if (isConnected) {
                    Log.e(TAG, "Error leyendo mensajes", e)
                    mainHandler.post {
                        Toast.makeText(this@ClientActivity, "Conexión perdida", Toast.LENGTH_SHORT).show()
                        disconnectFromServer()
                    }
                }
            }
        }.start()
    }

    /**
     * Muestra una notificación local
     */
    private fun showNotification(message: String) {
        try {
            // Verificar si las notificaciones están habilitadas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permiso de notificaciones no concedido")
                    return
                }
            }

            // Parsear el mensaje para extraer título y contenido
            val (title, content) = parseMessage(message)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(NOTIFICATION_ID_BASE + notificationCounter++, notification)

        } catch (e: Exception) {
            Log.e(TAG, "Error mostrando notificación", e)
        }
    }

    /**
     * Parsea el mensaje para extraer título y contenido
     */
    private fun parseMessage(message: String): Pair<String, String> {
        return try {
            // Formato esperado: [AppName] Title: Content
            val regex = "\\[(.+?)\\] (.+)".toRegex()
            val matchResult = regex.find(message)

            if (matchResult != null) {
                val appName = matchResult.groupValues[1]
                val rest = matchResult.groupValues[2]

                // Separar título y contenido si hay ":"
                val colonIndex = rest.indexOf(": ")
                if (colonIndex != -1) {
                    val title = rest.substring(0, colonIndex)
                    val content = rest.substring(colonIndex + 2)
                    Pair("$appName - $title", content)
                } else {
                    Pair(appName, rest)
                }
            } else {
                Pair("Notificación", message)
            }
        } catch (e: Exception) {
            Pair("Notificación", message)
        }
    }

    /**
     * Actualiza el estado de la interfaz
     */
    private fun updateUIState(connected: Boolean) {
        btnConnect.isEnabled = !connected
        btnDisconnect.isEnabled = connected
        etServerIP.isEnabled = !connected
        etServerPort.isEnabled = !connected
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

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromServer()
    }
}