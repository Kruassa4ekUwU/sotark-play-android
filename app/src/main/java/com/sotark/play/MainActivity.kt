package com.sotark.play

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.sotark.play.ui.Screen
import com.sotark.play.ui.screens.*
import com.sotark.play.ui.theme.SotarkPlayTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SotarkPlayTheme {
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

    val bottomRoutes = listOf(Screen.Home.route, Screen.Search.route, Screen.Publish.route)
    val showBottom   = currentRoute in bottomRoutes

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick  = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        icon  = { Icon(Icons.Filled.Home, null) },
                        label = { Text("Главная") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Search.route,
                        onClick  = {
                            navController.navigate(Screen.Search.route) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        icon  = { Icon(Icons.Filled.Search, null) },
                        label = { Text("Поиск") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Publish.route,
                        onClick  = {
                            navController.navigate(Screen.Publish.route) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        icon  = { Icon(Icons.Filled.AddBox, null) },
                        label = { Text("Загрузить") }
                    )
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
                HomeScreen(onAppClick = { id ->
                    navController.navigate(Screen.AppDetail.createRoute(id))
                })
            }
            composable(Screen.Search.route) {
                SearchScreen(onAppClick = { id ->
                    navController.navigate(Screen.AppDetail.createRoute(id))
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
            composable(
                route     = Screen.AppDetail.route,
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
