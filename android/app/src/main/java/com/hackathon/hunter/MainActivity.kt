package com.hackathon.hunter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.hackathon.hunter.ui.navigation.AppNavigation
import com.hackathon.hunter.ui.theme.HackathonHunterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HackathonHunterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val configuration = LocalConfiguration.current
                    val isExpanded = configuration.screenWidthDp >= 600
                    AppNavigation(isExpanded = isExpanded)
                }
            }
        }
    }
}
