package com.example.android.notepad

import android.content.Context

internal enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"), LIGHT("浅色"), DARK("深色")
}

internal enum class Paper(val label: String) {
    PLAIN("素白"), WARM("暖纸"), MINT("薄荷")
}

internal data class NotePreferences(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val paper: Paper = Paper.WARM,
    val fontSize: Int = 18,
    val order: NoteOrder = NoteOrder.MODIFIED
)

internal class PreferencesStore(context: Context) {
    private val store = context.getSharedPreferences("notepad_preferences", Context.MODE_PRIVATE)

    fun read() = NotePreferences(
        theme = ThemeMode.entries.firstOrNull { it.name == store.getString("theme", null) } ?: ThemeMode.SYSTEM,
        paper = Paper.entries.firstOrNull { it.name == store.getString("paper", null) } ?: Paper.WARM,
        fontSize = store.getInt("font", 18).coerceIn(16, 22),
        order = NoteOrder.entries.firstOrNull { it.name == store.getString("order", null) } ?: NoteOrder.MODIFIED
    )

    fun save(value: NotePreferences) {
        store.edit()
            .putString("theme", value.theme.name)
            .putString("paper", value.paper.name)
            .putInt("font", value.fontSize)
            .putString("order", value.order.name)
            .apply()
    }
}
