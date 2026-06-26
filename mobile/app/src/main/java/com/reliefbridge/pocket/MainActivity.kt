package com.reliefbridge.pocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private val GreenPrimary = Color(0xFF245B45)
private val OrangePrimary = Color(0xFFD96C2C)
private val Background = Color(0xFFF7F4EC)

private val AppColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    secondary = OrangePrimary,
    background = Background,
    surface = Color.White,
)

enum class Screen(val label: String, val icon: String) {
    TRIAGE("Triage", "🚨"),
    BENCHMARK("Benchmark", "⚡"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = AppColors) {
                ReliefBridgePocketApp()
            }
        }
    }
}

@Composable
fun ReliefBridgePocketApp() {
    val vm: AppViewModel = viewModel()
    var selectedScreen by remember { mutableStateOf(Screen.TRIAGE) }

    Scaffold(
        containerColor = Background,
        topBar = { AppBar() },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
            ) {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        icon = {
                            Text(
                                text = screen.icon,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GreenPrimary,
                            selectedTextColor = GreenPrimary,
                            indicatorColor = Color(0xFFF0F7F3),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        when (selectedScreen) {
            Screen.TRIAGE -> TriageScreen(
                vm = vm,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            Screen.BENCHMARK -> BenchmarkScreen(
                vm = vm,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun AppBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenPrimary)
            .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ReliefBridge Pocket",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(10.dp))
            ArmBadge()
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Private on-device triage · Arm Android",
            color = Color(0xFFB0D4C0),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ArmBadge() {
    Text(
        text = "Arm optimized",
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1A4232))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = Color(0xFF7EC8A4),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
    )
}
