package com.moqi.im.quickreply

import android.content.Context
import com.moqi.im.theme.ThemePalette
import org.json.JSONArray

object QuickReplyStore {
    private const val KEY = "quick_replies"

    fun load(context: Context): List<String> {
        val raw = context.getSharedPreferences(ThemePalette.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY, null)
            ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val text = array.optString(index).trim()
                    if (text.isNotBlank()) add(text)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, items: List<String>) {
        val normalized = items.map { it.trim() }.filter { it.isNotBlank() }
        val array = JSONArray()
        normalized.forEach { array.put(it) }
        context.getSharedPreferences(ThemePalette.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }

    fun add(context: Context, text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false
        val items = load(context).toMutableList()
        if (items.any { it == trimmed }) return false
        items.add(0, trimmed)
        save(context, items)
        return true
    }

    fun removeAt(context: Context, index: Int): Boolean {
        val items = load(context).toMutableList()
        if (index !in items.indices) return false
        items.removeAt(index)
        save(context, items)
        return true
    }
}
