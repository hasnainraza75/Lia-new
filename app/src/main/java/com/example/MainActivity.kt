package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.MainNavTab
import com.example.ui.components.NovaBottomNav
import com.example.ui.screens.*
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.NovaTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.preferences.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val orbState by viewModel.orbState.collectAsState()

            // Handle back navigation
            BackHandler(enabled = currentScreen != Screen.Splash && currentScreen != Screen.Home) {
                viewModel.navigateBack()
            }

            NovaTheme(themeMode = preferences.themeMode) {
                val colors = LocalCustomColors.current

                // Determine active tab for bottom navigation
                val currentTab = when (currentScreen) {
                    Screen.Home -> MainNavTab.HOME
                    Screen.Chat -> MainNavTab.CHAT
                    Screen.History -> MainNavTab.HISTORY
                    Screen.SettingsMain,
                    Screen.SettingsVoiceLanguage,
                    Screen.SettingsConnections,
                    Screen.SettingsLookFeel,
                    Screen.SettingsQuickActions,
                    Screen.SettingsPhoneControl,
                    Screen.SettingsChatsMemory,
                    Screen.SettingsPrivacyData,
                    Screen.SettingsPrivacyAbout,
                    Screen.SettingsProfile -> MainNavTab.SETTINGS
                    else -> MainNavTab.HOME
                }

                val showBottomNav = currentScreen != Screen.Splash

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = colors.bgMain,
                    bottomBar = {
                        if (showBottomNav) {
                            NovaBottomNav(
                                currentTab = currentTab,
                                orbState = orbState,
                                onTabSelected = { tab ->
                                    when (tab) {
                                        MainNavTab.HOME -> viewModel.navigateTo(Screen.Home)
                                        MainNavTab.CHAT -> viewModel.navigateTo(Screen.Chat)
                                        MainNavTab.HISTORY -> viewModel.navigateTo(Screen.History)
                                        MainNavTab.SETTINGS -> viewModel.navigateTo(Screen.SettingsMain)
                                    }
                                },
                                onCentralOrbClick = {
                                    viewModel.toggleVoiceSession()
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val scr = currentScreen) {
                            Screen.Splash -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        viewModel.navigateTo(Screen.Home)
                                    }
                                )
                            }
                            Screen.Home -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToChat = { viewModel.navigateTo(Screen.Chat) },
                                    onNavigateToQuickActions = { viewModel.navigateTo(Screen.SettingsQuickActions) },
                                    onNavigateToConnections = { viewModel.navigateTo(Screen.SettingsConnections) }
                                )
                            }
                            Screen.Chat -> {
                                ChatScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.History -> {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsMain -> {
                                SettingsMainScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsVoiceLanguage -> {
                                VoiceLanguageSettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsConnections -> {
                                ConnectionsSettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsLookFeel -> {
                                LookFeelSettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsQuickActions -> {
                                QuickActionsScreen(
                                    viewModel = viewModel,
                                    onNavigateToChat = { viewModel.navigateTo(Screen.Chat) },
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsPhoneControl -> {
                                PhoneControlScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsChatsMemory -> {
                                ChatsMemoryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsPrivacyData -> {
                                PrivacyDataScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsPrivacyAbout -> {
                                PrivacyAboutScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SettingsProfile -> {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            is Screen.WebsitePreview -> {
                                WebsitePreviewScreen(
                                    title = scr.title,
                                    htmlCode = scr.htmlCode,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                            is Screen.SketchPreview -> {
                                SketchPreviewScreen(
                                    initialPrompt = scr.prompt,
                                    onNavigateBack = { viewModel.navigateBack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
