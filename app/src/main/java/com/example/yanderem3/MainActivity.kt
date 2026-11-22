package com.example.yanderem3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.LocalImageLoader
import com.example.yanderem3.ui.AppViewModel
import com.example.yanderem3.ui.YandereApp
import com.example.yanderem3.ui.theme.YandereM3Theme
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
                YandereM3Theme(
                    darkTheme = settings.darkMode,
                    dynamicColor = settings.dynamicColor
                ) {
                    YandereApp(viewModel = vm)
                }
            }
        }
    }
}
