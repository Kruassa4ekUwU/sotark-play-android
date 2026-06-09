package com.sotark.play.ui

sealed class Screen(val route: String) {
    object Home      : Screen("home")
    object Search    : Screen("search")
    object Publish   : Screen("publish")
    object Settings  : Screen("settings")
    object History   : Screen("history")
    object AppDetail : Screen("app_detail/{appId}") {
        fun createRoute(id: Int) = "app_detail/$id"
    }
}
