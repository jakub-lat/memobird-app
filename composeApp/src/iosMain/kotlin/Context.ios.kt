import androidx.compose.runtime.Composable

actual abstract class MyContext {}

class IosContext : MyContext() {}

@Composable
actual fun getContext(): MyContext = IosContext()
