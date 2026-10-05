package com.ehealthwares.rxsoft.util

import com.ehealthwares.rxsoft.data.remote.api.PrintDiscoveryApi
import com.ehealthwares.rxsoft.data.remote.dto.AgentInfo
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Finds print-agents on the phone's reachable local subnets by probing
 * `GET /discover` on every host (mirrors the `scan` subcommand in
 * print-agent/goprint and print-agent/rustprint).
 *
 * Enumerates *all* LAN interfaces rather than assuming one, so it works when
 * the phone is on a Wi-Fi network, on a mobile hotspot (the interface the
 * tethered counter PC is attached to), or has multiple private addresses.
 */
@Singleton
class PrinterDiscovery @Inject constructor(
    @Named("discovery") private val client: OkHttpClient,
    private val moshi: Moshi,
) {

    /** A candidate local IPv4 address and the interface it belongs to. */
    data class LocalAddress(val ip: String, val interfaceName: String)

    /**
     * All private IPv4 addresses across up interfaces. Ordered so Wi-Fi and
     * hotspot interfaces are scanned first (they're the ones a counter PC is
     * usually attached to), then everything else.
     */
    fun localAddresses(): List<LocalAddress> = runCatching {
        NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { iface ->
                iface.inetAddresses.toList()
                    .filterIsInstance<Inet4Address>()
                    .mapNotNull { addr ->
                        val ip = addr.hostAddress ?: return@mapNotNull null
                        LocalAddress(ip, iface.name)
                    }
            }
            .filter { it.ip.isPrivate() && !it.ip.isLinkLocal() }
            .distinctBy { it.ip }
            .sortedBy { priority(it.interfaceName) }
    }.getOrNull().orEmpty()

    /** Backwards-compatible single-address accessor (first preferred). */
    fun localIpv4(): String? = localAddresses().firstOrNull()?.ip

    /** Probe a single host:port for a print-agent. */
    suspend fun probe(host: String, port: String): AgentInfo? = withContext(Dispatchers.IO) {
        runCatching {
            apiFor().discover("http://$host:$port/discover")
        }.getOrNull()?.takeIf { it.isSuccessful }?.body()?.takeIf { it.service == "print-agent" }
    }

    /**
     * Scan every local /24. Emits each agent as it is found via [onFound], and
     * returns the full deduplicated list. Returns empty if there is no private
     * local address.
     */
    suspend fun scan(
        port: String = "8094",
        onFound: (AgentInfo, String) -> Unit = { _, _ -> },
    ): List<Pair<AgentInfo, String>> = withContext(Dispatchers.IO) {
        val subnets = localAddresses()
            .map { it.ip.substringBeforeLast('.') }
            .distinct()
        if (subnets.isEmpty()) return@withContext emptyList()

        val semaphore = Semaphore(MAX_CONCURRENCY)
        val seen = mutableSetOf<String>()
        val results = coroutineScope {
            subnets.flatMap { prefix ->
                (1..254).map { last ->
                    async {
                        semaphore.withPermit {
                            val ip = "$prefix.$last"
                            val info = probe(ip, port)
                            if (info != null) {
                                synchronized(seen) {
                                    if (seen.add(ip)) {
                                        onFound(info, ip)
                                        info to ip
                                    } else null
                                }
                            } else null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
        results.sortedBy { it.second.substringAfterLast('.').toIntOrNull() ?: 0 }
    }

    private fun apiFor(): PrintDiscoveryApi = discoveryApi

    private val discoveryApi: PrintDiscoveryApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PrintDiscoveryApi::class.java)
    }

    private fun String.isPrivate(): Boolean =
        startsWith("192.168.") || startsWith("10.") ||
            (startsWith("172.") && substringBeforeLast('.').substringAfter("172.").toIntOrNull()
                ?.let { it in 16..31 } == true)

    private fun String.isLinkLocal(): Boolean = startsWith("169.254.")

    /**
     * Lower is scanned first. Hotspot/tether interfaces are named per-vendor
     * (ap0, wlan1, swlan0, rndis0, softap0; some ROMs expose them generically
     * as wlan0 with a 192.168.43.x address), so name alone is a hint, not truth.
     */
    private fun priority(iface: String): Int = when {
        iface.startsWith("ap") || iface.contains("softap") -> 0
        iface.contains("swlan") || iface.contains("rndis") -> 1
        iface.startsWith("wlan") -> 2
        iface.startsWith("eth") -> 3
        else -> 4
    }

    companion object {
        const val DEFAULT_PORT = "8094"
        private const val MAX_CONCURRENCY = 64
    }
}
