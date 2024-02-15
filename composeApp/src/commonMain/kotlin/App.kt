import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScaleTransition
import screens.PrintScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(PrintScreen) { navigator ->
            ScaleTransition(navigator)
        }
    }
}