package com.sotark.play

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        val prefs  = base.getSharedPreferences("sotark_prefs", Context.MODE_PRIVATE)
        val code   = prefs.getString("language", "en") ?: "en"
        val locale = when (code) {
            "iw", "he" -> Locale.Builder().setLanguage("iw").build()
            else        -> Locale(code)
        }
        Locale.setDefault(locale)
        val config = base.resources.configuration.also { cfg ->
            cfg.setLocale(locale)
            if (code == "iw" || code == "he") cfg.setLayoutDirection(locale)
        }
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val darkTheme      by settingsVm.darkTheme.collectAsState()
            val ukrainianTheme by settingsVm.ukrainianTheme.collectAsState()
            val secretTheme    by settingsVm.secretTheme.collectAsState()
            val language       by settingsVm.language.collectAsState()

            var prevLang by remember { mutableStateOf<AppLanguage?>(null) }
            LaunchedEffect(language) {
                if (prevLang != null && prevLang != language) recreate()
                prevLang = language
            }

            SotarkPlayTheme(
                darkTheme      = darkTheme,
                ukrainianTheme = ukrainianTheme,
                secretTheme    = secretTheme
            ) {
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

    val bottomRoutes = listOf(
        Screen.Home.route, Screen.Search.route,
        Screen.Publish.route, Screen.Settings.route
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}
        LaunchedEffect(Unit) { permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar {
                    listOf(
                        Triple(Screen.Home.route,     Icons.Filled.Home,     R.string.home),
                        Triple(Screen.Search.route,   Icons.Filled.Search,   R.string.search),
                        Triple(Screen.Publish.route,  Icons.Filled.AddBox,   R.string.upload),
                        Triple(Screen.Settings.route, Icons.Filled.Settings, R.string.settings),
                    ).forEach { (route, icon, label) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick  = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            },
                            icon  = { Icon(icon, null) },
                            label = { Text(stringResource(label)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onAppClick = {
                    navController.navigate(Screen.AppDetail.createRoute(it))
                })
            }
            composable(Screen.Search.route) {
                SearchScreen(onAppClick = {
                    navController.navigate(Screen.AppDetail.createRoute(it))
                })
            }
            composable(Screen.Publish.route) {
                PublishScreen(
                    onBack    = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack         = { navController.popBackStack() },
                    onHistoryClick = { navController.navigate(Screen.History.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onBack     = { navController.popBackStack() },
                    onAppClick = { navController.navigate(Screen.AppDetail.createRoute(it)) }
                )
            }
            composable(
                Screen.AppDetail.route,
                arguments = listOf(navArgument("appId") { type = NavType.IntType })
            ) { back ->
                val appId = back.arguments?.getInt("appId") ?: return@composable
                AppDetailScreen(
                    appId  = appId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
