import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class WifiState(val isConnected: Boolean = false, val isBound: Boolean = false, val ssid: String? = null)

interface WifiManager {
    val state: StateFlow<WifiState>

    suspend fun connect(ssid: String, password: String)
    suspend fun disconnect()
    suspend fun bind()
    suspend fun unbind()
}

expect fun getWifiManager(context: MyContext): WifiManager;
