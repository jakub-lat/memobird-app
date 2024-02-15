package printService

import utils.MyContext
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.PatternMatcher
import android.provider.Settings
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidWifiManager(private val context: Context) : WifiManager {
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(ConnectivityManager::class.java)
    private val wifiManager: android.net.wifi.WifiManager =
        context.getSystemService(android.net.wifi.WifiManager::class.java)

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var network: Network? = null

    private val _state = MutableStateFlow(WifiState())

    override val state: StateFlow<WifiState>
        get() = _state

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun connect(ssid: String, password: String) {
        if (state.value.isConnected) return

        if (!wifiManager.isWifiEnabled) {
            context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
        }

        val specifier = WifiNetworkSpecifier.Builder()
//            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setSsidPattern(PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        return suspendCancellableCoroutine { continuation ->
            networkCallback = @RequiresApi(VERSION_CODES.S) object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    _state.value = _state.value.copy(isConnected = true)
                    this@AndroidWifiManager.network = network

                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    resetAll()

                    if (continuation.isActive) {
                        continuation.cancel(Exception("Failed to connect to Memobird Wi-Fi"))
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    resetAll()

                    if (continuation.isActive) {
                        continuation.cancel(Exception("Out of range of Memobird Wi-Fi"))
                    }
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val wifiInfo = (networkCapabilities.transportInfo as WifiInfo)
                    _state.value = _state.value.copy(ssid = wifiInfo.ssid.replace("\"", ""))
                }
            }
            connectivityManager.requestNetwork(request, networkCallback!!)

            continuation.invokeOnCancellation {
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
                resetAll()
            }
        }
    }

    override suspend fun bind() {
        connectivityManager.bindProcessToNetwork(network)
        _state.value = _state.value.copy(isBound = true)
    }

    override suspend fun unbind() {
        connectivityManager.bindProcessToNetwork(null)
        _state.value = _state.value.copy(isBound = false)
    }

    override suspend fun disconnect() = resetAll()

    private fun resetAll() {
        _state.value = WifiState()
        network = null
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback!!)
        }
        networkCallback = null
    }
}

actual fun getWifiManager(context: MyContext): WifiManager = AndroidWifiManager(context);