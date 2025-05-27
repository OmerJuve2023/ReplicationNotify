package com.example.notificationsync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView

/**
 * Adaptador personalizado para mostrar aplicaciones con checkboxes
 */
class AppListAdapter(
    context: Context,
    private val apps: List<AppInfo>,
    private val onSelectionChanged: (String, Boolean) -> Unit
) : ArrayAdapter<AppInfo>(context, 0, apps) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_app_selection, parent, false)

        val app = getItem(position)!!

        val checkBox = view.findViewById<CheckBox>(R.id.cbApp)
        val textView = view.findViewById<TextView>(R.id.tvAppName)

        textView.text = app.name

        // Configurar checkbox
        checkBox.setOnCheckedChangeListener(null) // Limpiar listener anterior
        checkBox.isChecked = false // Reset estado

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            onSelectionChanged(app.packageName, isChecked)
        }

        return view
    }
}