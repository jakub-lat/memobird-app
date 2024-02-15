package screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandGithub

object InfoScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            navigator.pop()
                        }) {
                            Icon(Icons.Default.ArrowBack, "back")
                        } },
                    title = { Text("About") }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                Column(Modifier.fillMaxSize().padding(20.dp, 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("© 2024 Jakub L.")
                    Spacer(Modifier.height(2.dp))
                    Text("In memory of official Memobird app.")
                    Spacer(Modifier.height(40.dp))
                    Text("Made with Compose Multiplatform.")
                    Spacer(Modifier.height(20.dp))
                    FilledTonalButton(onClick = {
                        uriHandler.openUri("https://github.com/jakub-lat/memobird-app")
                    }) {
                        Icon(TablerIcons.BrandGithub, "github")
                        Spacer(Modifier.width(5.dp))
                        Text("GitHub repository")
                    }
                }
            }
        }
    }
}