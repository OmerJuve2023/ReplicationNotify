package com.example.notificationsync

import android.app.AlertDialog
import android.content.Context
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
            // Lista de apps de mensajería que nunca se deben replicar
            val excludedPackages = setOf(
                "com.whatsapp",
                "com.facebook.orca",
                "com.google.android.apps.messaging",
                "com.android.mms",
                "com.android.messaging",
                "com.instagram.android",
                "com.twitter.android",
                "com.telegram.messenger"
            )
            // Solo replicar si está seleccionada y no es de mensajería
            if (selectedApps.contains(packageName) && !excludedPackages.contains(packageName)) {
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

    /**
     * Muestra la política de privacidad
     */
    fun showPrivacyPolicy(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Política de Privacidad")
            .setMessage("Esta aplicación accede a tus notificaciones solo para replicarlas entre dispositivos seleccionados por ti. No se almacenan ni comparten datos personales con terceros. Puedes elegir qué aplicaciones replicar y excluir apps de mensajería. Para más información visita: https://www.example.com/privacy-policy")
            .setPositiveButton("Aceptar", null)
            .show()
    }
}