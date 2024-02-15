import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IOSWifiManager : WifiManager {
    override val state: StateFlow<WifiState>
        get() = TODO("Not yet implemented")

    override suspend fun connect(ssid: String, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }

    override suspend fun bind() {
        TODO("Not yet implemented")
    }

    override suspend fun unbind() {
        TODO("Not yet implemented")
    }
}

actual fun getWifiManager(context: MyContext): WifiManager = IOSWifiManager()