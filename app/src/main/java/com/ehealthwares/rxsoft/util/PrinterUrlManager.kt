package com.ehealthwares.rxsoft.util

import android.content.Context
import com.ehealthwares.rxsoft.data.remote.dto.AgentInfo
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

    /** Resolve the full base URL a print-agent advertises for itself. */
    fun urlFor(ip: String, port: String): String = "http://$ip:$port".trimEnd('/')

    // --- Previously discovered agents, for quick re-selection ---

    fun getKnownAgents(): List<KnownAgent> {
        val raw = prefs.getString(KEY_AGENTS, null) ?: return emptyList()
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 4) return@mapNotNull null
            KnownAgent(
                url = parts[0],
                hostname = parts[1],
                printer = parts[2],
                width = parts[3].toIntOrNull() ?: 32,
            )
        }
    }

    fun addKnownAgent(agent: KnownAgent) {
        val updated = (getKnownAgents().filterNot { it.url == agent.url } + agent).takeLast(MAX_AGENTS)
        val encoded = updated.joinToString("\n") { "${it.url}|${it.hostname}|${it.printer}|${it.width}" }
        prefs.edit().putString(KEY_AGENTS, encoded).commit()
    }

    companion object {
        private const val KEY = "printer_url"
        private const val KEY_AGENTS = "known_agents"
        private const val MAX_AGENTS = 10
        private const val DEFAULT_URL = "http://192.168.1.100:8094"
    }
}

/** A print-agent remembered from a previous scan or manual entry. */
data class KnownAgent(
    val url: String,
    val hostname: String,
    val printer: String,
    val width: Int,
) {
    companion object {
        fun from(info: AgentInfo, ip: String): KnownAgent = KnownAgent(
            url = "http://$ip:${info.port}",
            hostname = info.hostname.ifBlank { ip },
            printer = info.printer,
            width = info.width,
        )
    }
}
