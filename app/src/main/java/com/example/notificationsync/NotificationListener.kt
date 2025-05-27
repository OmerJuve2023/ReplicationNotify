package com.example.notificationsync

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Servicio que escucha las notificaciones del sistema
 */
class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        var instance: NotificationListener? = null
        var selectedApps: Set<String> = emptySet()
        var onNotificationReceived: ((String, String, String) -> Unit)? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "NotificationListener creado")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "NotificationListener destruido")
    }

    /**
     * Se llama cuando llega una nueva notificación
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            val packageName = notification.packageName

            // Verificar si esta app está en la lista de seleccionadas
            if (selectedApps.contains(packageName)) {
                processNotification(notification)
            }
        }
    }

    /**
     * Procesa la notificación y la envía a los clientes conectados
     */
    private fun processNotification(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val extras = notification.extras

            // Obtener información de la notificación
            val appName = getAppName(sbn.packageName)
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""

            // Crear mensaje para enviar
            val message = if (text.isNotEmpty()) {
                "[$appName] $title: $text"
            } else {
                "[$appName] $title"
            }

            Log.d(TAG, "Notificación procesada: $message")

            // Enviar a callback si está configurado
            onNotificationReceived?.invoke(appName, title, text)

        } catch (e: Exception) {
            Log.e(TAG, "Error procesando notificación", e)
        }
    }

    /**
     * Obtiene el nombre de la aplicación a partir del package name
     */
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}