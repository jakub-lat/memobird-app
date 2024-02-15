package utils

import androidx.compose.runtime.Composable

expect abstract class MyContext {}

@Composable
expect fun getContext(): MyContext
