package com.example.materialrickandmorty.ui.navigation

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Pick : Screen("pick")
    object Detail : Screen("character_detail/{characterId}") {
        fun createRoute(characterId: Int) = "character_detail/$characterId"
    }
}
