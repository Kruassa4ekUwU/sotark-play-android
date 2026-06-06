package com.sotark.play

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.sotark.play.data.AppLanguage
import com.sotark.play.ui.Screen
import com.sotark.play.ui.screens.*
import com.sotark.play.ui.theme.SotarkPlayTheme
import com.sotark.play.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val darkTheme by settingsVm.darkTheme.collectAsState()
            val language  by settingsVm.language.collectAsState()

            // Apply locale
            val ctx = LocalContext.current
            LaunchedEffect(language) {
                val locale = Locale(language.code)
                Locale.setDefault(locale)
                val config = Configuration(ctx.resources.configuration)
                config.setLocale(locale)
                ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics)
            }

            SotarkPlayTheme(darkTheme = darkTheme) {
                SotarkPlayApp()
            }
        }
    }
}

@Composable
fun SotarkPlayApp() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route
    val bottomRoutes  = listOf(Screen.Home.route, Screen.Search.route,
                               Screen.Publish.route, Screen.Settings.route)
    val showBottom    = currentRoute in bottomRoutes

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick  = { navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true } } },
                        icon  = { Icon(Icons.Filled.Home, null) },
                        label = { Text(stringResource(com.sotark.play.R.string.home)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Search.route,
                        onClick  = { navController.navigate(Screen.Search.route) {
                            popUpTo(Screen.Home.route) } },
                        icon  = { Icon(Icons.Filled.Search, null) },
                        label = { Text(stringResource(com.sotark.play.R.string.search)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Publish.route,
                        onClick  = { navController.navigate(Screen.Publish.route) {
                            popUpTo(Screen.Home.route) } },
                        icon  = { Icon(Icons.Filled.AddBox, null) },
                        label = { Text(stringResource(com.sotark.play.R.string.upload)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick  = { navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Home.route) } },
                        icon  = { Icon(Icons.Filled.Settings, null) },
                        label = { Text(stringResource(com.sotark.play.R.string.settings)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)) {
            composable(Screen.Home.route) {
                HomeScreen(onAppClick = { navController.navigate(Screen.AppDetail.createRoute(it)) })
            }
            composable(Screen.Search.route) {
                SearchScreen(onAppClick = { navController.navigate(Screen.AppDetail.createRoute(it)) })
            }
            composable(Screen.Publish.route) {
                PublishScreen(
                    onBack    = { navController.popBackStack() },
                    onSuccess = { navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true } } }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.AppDetail.route,
                arguments = listOf(navArgument("appId") { type = NavType.IntType })
            ) { back ->
                val appId = back.arguments?.getInt("appId") ?: return@composable
                AppDetailScreen(appId = appId, onBack = { navController.popBackStack() })
            }
        }
    }
}
