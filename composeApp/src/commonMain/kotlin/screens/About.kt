package screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandGithub
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

object AboutScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        val icon = painterResource("drawable/ic_launcher_foreground.webp")

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
                Column(Modifier.fillMaxSize().padding(40.dp, 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(icon, "App icon")
                    Text("Memobird App", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(10.dp))
                    Text("© 2024 Jakub L.")
                    Spacer(Modifier.height(15.dp))
                    Text("Unofficial app for Memobird G2, as the original one is not working anymore.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    Text("Made with Compose Multiplatform.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(7.dp))
                    Text("Special thanks to LeMinaw/openmemobird.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(30.dp))
                    FilledTonalButton(onClick = {
                        uriHandler.openUri("https://github.com/jakub-lat/memobird-app")
                    }) {
                        Icon(TablerIcons.BrandGithub, "github")
                        Spacer(Modifier.width(8.dp))
                        Text("GitHub repository")
                    }
                }
            }
        }
    }
}