package com.example.notificationsync

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermission()
        setupUI()
    }

    private fun checkNotificationPermission() {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this)

        if (!enabledListeners.contains(packageName)) {
            showNotificationPermissionDialog()
        }
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso Requerido")
            .setMessage("Esta aplicación necesita acceso a notificaciones para funcionar correctamente.")
            .setPositiveButton("Configurar") { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupUI() {
        val btnServer = findViewById<Button>(R.id.btnServer)
        val btnClient = findViewById<Button>(R.id.btnClient)

        btnServer.setOnClickListener {
            val intent = Intent(this, ServerConfigActivity::class.java)
            startActivity(intent)
        }

        btnClient.setOnClickListener {
            val intent = Intent(this, ClientActivity::class.java)
            startActivity(intent)
        }
    }
}