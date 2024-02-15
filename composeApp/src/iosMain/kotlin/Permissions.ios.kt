import androidx.compose.runtime.Composable

@Composable
actual fun RequirePermissions(grantButton: @Composable (onClick: () -> Unit) -> Unit, content: @Composable () -> Unit) {
    content()
}