package com.example.materialrickandmorty.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val favorites: List<Int> = emptyList(),
    val favoriteEpisodes: List<Int> = emptyList(),
    val episodesWatched: List<Int> = emptyList(),
    val settings: UserSettings = UserSettings()
)

data class UserSettings(
    val darkTheme: Boolean = true,
    val language: String = "English",
    val hideEmail: Boolean = false
)
