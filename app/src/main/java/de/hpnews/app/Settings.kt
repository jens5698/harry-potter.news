package de.hpnews.app

import android.content.Context
import de.hpnews.core.Simplifier

/** Einstellungen, lokal auf dem Gerät gespeichert. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) = prefs.edit().putString("api_key", value.trim()).apply()

    var model: String
        get() = prefs.getString("model", Simplifier.DEFAULT_MODEL) ?: Simplifier.DEFAULT_MODEL
        set(value) = prefs.edit().putString("model", value).apply()
}
