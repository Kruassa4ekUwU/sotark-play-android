package com.sotark.play.ui

sealed class Screen(val route: String) {
    object Home   : Screen("home")
    object Search : Screen("search")
    object Categories : Screen("categories")
    object AppDetail  : Screen("app/{appId}") {
        fun createRoute(id: Int) = "app/$id"
    }
}
