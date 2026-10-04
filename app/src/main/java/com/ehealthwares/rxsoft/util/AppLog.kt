package com.ehealthwares.rxsoft.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thin wrapper over [android.util.Log] that also keeps a bounded in-memory
 * ring buffer of recent entries so the Settings screen can show diagnostics
 * (print-agent discovery, test prints, printer selection) without adb.
 *
 * Never logs secrets: callers pass only URLs, status codes and error messages.
 */
object AppLog {
    private const val MAX_LINES = 200
    private const val PREFS_TAG = "RxSoft"

    data class Entry(
        val timestamp: Long,
        val level: Char,
        val tag: String,
        val message: String,
    ) {
        fun format(): String {
            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
            return "$time $level/$tag: $message"
        }
    }

    private val buffer = CopyOnWriteArrayList<Entry>()

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    fun d(tag: String, message: String) = add(Log.DEBUG, 'D', tag, message)
    fun i(tag: String, message: String) = add(Log.INFO, 'I', tag, message)
    fun w(tag: String, message: String) = add(Log.WARN, 'W', tag, message)

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val full = if (throwable != null) "$message: ${throwable.message ?: throwable.javaClass.simpleName}" else message
        add(Log.ERROR, 'E', tag, full)
        if (throwable != null) Log.e("$PREFS_TAG/$tag", message, throwable)
    }

    private fun add(priority: Int, level: Char, tag: String, message: String) {
        Log.println(priority, "$PREFS_TAG/$tag", message)
        buffer.add(Entry(System.currentTimeMillis(), level, tag, message))
        while (buffer.size > MAX_LINES) buffer.removeAt(0)
        _lines.value = buffer.map { it.format() }
    }

    fun clear() {
        buffer.clear()
        _lines.value = emptyList()
    }

    /** Snapshot for sharing/copying. */
    fun dump(): String = buffer.joinToString("\n") { it.format() }
}
