package utils

import androidx.compose.runtime.Composable

@Composable
expect fun RequirePermissions(grantButton: @Composable (onClick: () -> Unit) -> Unit, content: @Composable () -> Unit)