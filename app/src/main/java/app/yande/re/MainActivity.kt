package app.yande.re

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.LocalImageLoader
import app.yande.re.ui.AppViewModel
import app.yande.re.ui.YandereApp
import app.yande.re.ui.theme.YandereTheme
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as YandereApplication
        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModel.factory(app.container.repository))
            val settings by vm.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalImageLoader provides app.container.imageLoader) {
                YandereTheme(
                    darkTheme = settings.darkMode,
                    dynamicColor = settings.dynamicColor
                ) {
                    YandereApp(viewModel = vm)
                }
            }
        }
    }
}
