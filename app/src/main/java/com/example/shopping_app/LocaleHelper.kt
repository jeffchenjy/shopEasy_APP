package com.example.shopping_app

import android.content.Context
import java.util.Locale


object LocaleHelper {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "language"

    fun setLocale(context: Context, languageCode: String) {
        val locale: Locale
        locale = if (languageCode.equals("")) {
            Locale.getDefault()
        } else {
            Locale(languageCode)
        }
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        context.createConfigurationContext(config)

        // 保存語言設置
        saveLanguagePreference(context, languageCode)

        context.createConfigurationContext(config)
    }
    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_LANGUAGE, languageCode)
        editor.apply()
    }

    fun loadLocale(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, "")
        setLocale(context, languageCode!!)
    }

    fun getCurrentLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "")
    }
}