package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.CustomWirdScreen
import com.example.ui.screens.DhikrListScreen
import com.example.ui.screens.DhikrReaderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AzkarViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {

    private val viewModel: AzkarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.userPreferences.collectAsState()

            MyApplicationTheme(
                themeMode = preferences.themeMode,
                colorTheme = preferences.colorTheme
            ) {
                // Arabic RTL Layout direction for authentic Islamic app experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AzkarMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AzkarMainApp(viewModel: AzkarViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    BackHandler(enabled = currentScreen !is ScreenDestination.Home) {
        viewModel.navigateBack()
    }

    val isReaderMode = currentScreen is ScreenDestination.Reader

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isReaderMode) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    // 1. الرئيسية (Home)
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Home,
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenDestination.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "الرئيسية"
                            )
                        },
                        label = { Text("الرئيسية") },
                        modifier = Modifier.testTag("nav_home"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // 2. الأذكار (Dhikr Library & 3 Sections)
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.DhikrList,
                        onClick = { viewModel.navigateTo(ScreenDestination.DhikrList()) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenDestination.DhikrList) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "الأذكار"
                            )
                        },
                        label = { Text("الأذكار") },
                        modifier = Modifier.testTag("nav_azkar"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // 3. أورادي (Custom Wirds)
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.CustomWirds,
                        onClick = { viewModel.navigateTo(ScreenDestination.CustomWirds) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenDestination.CustomWirds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "أورادي"
                            )
                        },
                        label = { Text("أورادي") },
                        modifier = Modifier.testTag("nav_wirds"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // 4. الإنجازات (Achievements & Streaks)
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Achievements,
                        onClick = { viewModel.navigateTo(ScreenDestination.Achievements) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenDestination.Achievements) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                                contentDescription = "الإنجازات"
                            )
                        },
                        label = { Text("الإنجازات") },
                        modifier = Modifier.testTag("nav_achievements"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // 5. الإعدادات (Settings)
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Settings,
                        onClick = { viewModel.navigateTo(ScreenDestination.Settings) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenDestination.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "الإعدادات"
                            )
                        },
                        label = { Text("الإعدادات") },
                        modifier = Modifier.testTag("nav_settings"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                is ScreenDestination.Home -> HomeScreen(viewModel = viewModel)
                is ScreenDestination.DhikrList -> DhikrListScreen(
                    viewModel = viewModel,
                    initialCategory = screen.initialCategory
                )
                is ScreenDestination.CustomWirds -> CustomWirdScreen(viewModel = viewModel)
                is ScreenDestination.Achievements -> AchievementsScreen(viewModel = viewModel)
                is ScreenDestination.Settings -> SettingsScreen(viewModel = viewModel)
                is ScreenDestination.Reader -> DhikrReaderScreen(viewModel = viewModel)
            }
        }
    }
}
