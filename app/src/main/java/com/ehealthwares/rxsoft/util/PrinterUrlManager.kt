package com.ehealthwares.rxsoft.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterUrlManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("printer_config", Context.MODE_PRIVATE)
    fun getUrl(): String = prefs.getString(KEY, DEFAULT_URL) ?: DEFAULT_URL
    fun setUrl(url: String) {
        prefs.edit().putString(KEY, url.trimEnd('/')).commit()
    }
    companion object {
        private const val KEY = "printer_url"
        private const val DEFAULT_URL = "http://192.168.1.100:8094"
    }
}