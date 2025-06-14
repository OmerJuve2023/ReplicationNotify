/*
package com.example.notificationsync

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

*/
/**
 * Actividad para configurar el servidor y seleccionar qué aplicaciones compartir
 *//*

class ServerConfigActivity : AppCompatActivity() {

    private lateinit var etPort: EditText
    private lateinit var btnStartServer: Button
    private lateinit var lvApps: ListView
    private lateinit var appListAdapter: ArrayAdapter<AppInfo>
    private val selectedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_config)

        setupUI()
        checkNotificationPermission()
        // Quitar carga automática de apps
        // loadInstalledApps()
    }

    */
/**
     * Configura la interfaz de usuario
     *//*

    private fun setupUI() {
        etPort = findViewById(R.id.etPort)
        btnStartServer = findViewById(R.id.btnStartServer)
        lvApps = findViewById(R.id.lvApps)
        val btnSelectApps = findViewById<Button>(R.id.btnSelectApps)
        val btnPrivacy = findViewById<Button>(R.id.btnPrivacy)

        // Establecer puerto por defecto
        etPort.setText("8888")

        btnStartServer.setOnClickListener {
            startServer()
        }
        btnSelectApps.setOnClickListener {
            // Abrir selector de apps del sistema
            showAppSelectionDialog()
        }
        btnPrivacy.setOnClickListener {
            NotificationListener().showPrivacyPolicy(this)
        }
    }

    private fun showAppSelectionDialog() {
        // Usar BottomSheetDialog para una experiencia más nativa y moderna
        val dialogView = layoutInflater.inflate(R.layout.dialog_app_selection, null)
        val dialogLvApps = dialogView.findViewById<ListView>(R.id.dialogLvApps)

        // Mostrar solo apps visibles en el launcher
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val apps = resolveInfos.map {
            val appName = it.loadLabel(pm).toString()
            val appIcon = it.loadIcon(pm)
            AppInfo(it.activityInfo.packageName, appName, appIcon)
        }.sortedBy { it.name }

        val tempSelected = selectedApps.toMutableSet()
        val adapter = AppListAdapter(this, apps) { packageName, isChecked ->
            if (isChecked) tempSelected.add(packageName) else tempSelected.remove(packageName)
        }
        dialogLvApps.adapter = adapter

        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setTitle("Selecciona las aplicaciones a replicar")
        dialogView.findViewById<ListView>(R.id.dialogLvApps)
        // Botón aceptar/cancelar personalizado
        // Puedes agregar botones en el layout si lo deseas
        bottomSheetDialog.setOnDismissListener {
            selectedApps.clear()
            selectedApps.addAll(tempSelected)
        }
        bottomSheetDialog.show()
    }

    */
/**
     * Verifica si el servicio de notificaciones está habilitado
     *//*

    private fun checkNotificationPermission() {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this)

        if (!enabledListeners.contains(packageName)) {
            showNotificationPermissionDialog()
        }
    }

    */
/**
     * Muestra diálogo para habilitar el acceso a notificaciones
     *//*

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso Requerido")
            .setMessage("Para funcionar como servidor, necesitas habilitar el acceso a notificaciones para esta aplicación.")
            .setPositiveButton("Configurar") { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    */
/**
     * Carga las aplicaciones instaladas para que el usuario pueda seleccionar cuáles compartir
     *//*

    private fun loadInstalledApps() {
        val packageManager = packageManager
        val apps = mutableListOf<AppInfo>()

        // Obtener aplicaciones instaladas
        val packages = packageManager.getInstalledPackages(0)

        for (packageInfo in packages) {
            // Filtrar solo apps que no sean del sistema
            val appInfo = packageInfo.applicationInfo
            if (appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)) {
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                apps.add(AppInfo(
                    packageInfo.packageName, appName,
                    icon = TODO()
                ))
            }
        }

        // Ordenar por nombre
        apps.sortBy { it.name }

        // Configurar ListView con checkboxes
        appListAdapter = AppListAdapter(this, apps) { packageName, isChecked ->
            if (isChecked) {
                selectedApps.add(packageName)
            } else {
                selectedApps.remove(packageName)
            }
        }

        lvApps.adapter = appListAdapter
    }

    */
/**
     * Verifica si es una aplicación conocida que vale la pena mostrar
     *//*

    private fun isKnownApp(packageName: String): Boolean {
        val knownApps = listOf(
            "com.whatsapp",
            "com.google.android.gm",
            "com.facebook.orca",
            "com.instagram.android",
            "com.twitter.android",
            "com.spotify.music",
            "com.google.android.youtube",
            "com.slack",
            "com.discord",
            "com.telegram.messenger"
        )
        return knownApps.contains(packageName)
    }

    */
/**
     * Inicia el servidor con la configuración seleccionada
     *//*

    private fun startServer() {
        val portText = etPort.text.toString()

        if (TextUtils.isEmpty(portText)) {
            Toast.makeText(this, "Ingresa un puerto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val port = try {
            portText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Puerto debe ser un número", Toast.LENGTH_SHORT).show()
            return
        }

        if (port < 1024 || port > 65535) {
            Toast.makeText(this, "Puerto debe estar entre 1024 y 65535", Toast.LENGTH_SHORT).show()
            return
        }

        // Pasar configuración a ServerActivity
        val intent = Intent(this, ServerActivity::class.java)
        intent.putExtra("port", port)
        intent.putStringArrayListExtra("selectedApps", ArrayList(selectedApps))
        startActivity(intent)
    }
}

*/
/**
 * Clase de datos para la información de la aplicación
 *//*

data class AppInfo(val packageName: String, val name: String)
*/
package com.example.notificationsync

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class ServerConfigActivity : AppCompatActivity() {

    private lateinit var etPort: EditText
    private lateinit var btnStartServer: Button
    private lateinit var lvApps: ListView
    private lateinit var appListAdapter: ArrayAdapter<AppInfo>
    private val selectedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_config)

        setupUI()
        checkNotificationPermission()
        // Quitar carga automática de apps
        // loadInstalledApps()
    }

    private fun setupUI() {
        etPort = findViewById(R.id.etPort)
        btnStartServer = findViewById(R.id.btnStartServer)
        lvApps = findViewById(R.id.lvApps)
        val btnSelectApps = findViewById<Button>(R.id.btnSelectApps)
        val btnPrivacy = findViewById<Button>(R.id.btnPrivacy)

        etPort.setText("8888")

        btnStartServer.setOnClickListener {
            startServer()
        }
        btnSelectApps.setOnClickListener {
            // Abrir selector de apps del sistema
            showAppSelectionDialog()
        }
        btnPrivacy.setOnClickListener {
            NotificationListener().showPrivacyPolicy(this)
        }
    }

    private fun showAppSelectionDialog() {
        // Usar BottomSheetDialog para una experiencia más nativa y moderna
        val dialogView = layoutInflater.inflate(R.layout.dialog_app_selection, null)
        val dialogLvApps = dialogView.findViewById<ListView>(R.id.dialogLvApps)

        // Mostrar solo apps visibles en el launcher
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val apps = resolveInfos.map {
            val appName = it.loadLabel(pm).toString()
            val appIcon = it.loadIcon(pm)
            AppInfo(it.activityInfo.packageName, appName, appIcon)
        }.sortedBy { it.name }

        val tempSelected = selectedApps.toMutableSet()
        val adapter = AppListAdapter(this, apps) { packageName, isChecked ->
            if (isChecked) tempSelected.add(packageName) else tempSelected.remove(packageName)
        }
        dialogLvApps.adapter = adapter

        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setTitle("Selecciona las aplicaciones a replicar")
        dialogView.findViewById<ListView>(R.id.dialogLvApps)
        // Botón aceptar/cancelar personalizado
        // Puedes agregar botones en el layout si lo deseas
        bottomSheetDialog.setOnDismissListener {
            selectedApps.clear()
            selectedApps.addAll(tempSelected)
        }
        bottomSheetDialog.show()
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
            .setMessage("Para funcionar como servidor, necesitas habilitar el acceso a notificaciones para esta aplicación.")
            .setPositiveButton("Configurar") { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadInstalledApps() {
        val packageManager = packageManager
        val apps = mutableListOf<AppInfo>()

        // Obtener aplicaciones instaladas
        val packages = packageManager.getInstalledPackages(0)

        for (packageInfo in packages) {
            // Filtrar solo apps que no sean del sistema
            val appInfo = packageInfo.applicationInfo
            if (appInfo != null && (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0)) {
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                apps.add(AppInfo(packageInfo.packageName, appName))
            }
        }

        // Ordenar por nombre
        apps.sortBy { it.name }

        // Configurar ListView con checkboxes
        appListAdapter = AppListAdapter(this, apps) { packageName, isChecked ->
            if (isChecked) {
                selectedApps.add(packageName)
            } else {
                selectedApps.remove(packageName)
            }
        }

        lvApps.adapter = appListAdapter
    }

    private fun startServer() {
        val portText = etPort.text.toString()

        if (TextUtils.isEmpty(portText)) {
            Toast.makeText(this, "Ingresa un puerto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val port = try {
            portText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Puerto debe ser un número", Toast.LENGTH_SHORT).show()
            return
        }

        if (port < 1024 || port > 65535) {
            Toast.makeText(this, "Puerto debe estar entre 1024 y 65535", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ServerActivity::class.java)
        intent.putExtra("port", port)
        intent.putStringArrayListExtra("selectedApps", ArrayList(selectedApps))
        startActivity(intent)
    }
}

data class AppInfo(val packageName: String, val name: String, val icon: Drawable)