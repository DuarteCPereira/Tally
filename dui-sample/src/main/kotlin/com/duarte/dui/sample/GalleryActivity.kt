package com.duarte.dui.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import com.duarte.dui.theme.DuiTheme

class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Pass `--ez dark true` to start on the dark half, for screenshots.
        val startDark = intent.getBooleanExtra("dark", false)
        setContent {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                val order = if (startDark) listOf(true, false) else listOf(false, true)
                order.forEach { dark ->
                    DuiTheme(darkTheme = dark) {
                        Column(
                            Modifier
                                .background(DuiTheme.colors.bg)
                                .windowInsetsPadding(WindowInsets.safeDrawing),
                        ) {
                            val name = if (dark) "Dark" else "Light"
                            ComponentGallery(name)
                            ThemeShowcase(name)
                        }
                    }
                }
            }
        }
    }
}
