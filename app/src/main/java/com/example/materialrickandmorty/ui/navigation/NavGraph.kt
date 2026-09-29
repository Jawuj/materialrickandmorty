package com.example.materialrickandmorty.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.materialrickandmorty.ui.screens.detail.CharacterDetailScreen
import com.example.materialrickandmorty.ui.screens.loading.LoadingScreen
import com.example.materialrickandmorty.ui.screens.pick.PickScreen
import com.example.materialrickandmorty.ui.viewmodel.RickAndMortyViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: RickAndMortyViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route,
        modifier = modifier
    ) {
        composable(route = Screen.Loading.route) {
            LoadingScreen(
                onLoadingComplete = {
                    navController.navigate(Screen.Pick.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Pick.route) {
            PickScreen(
                viewModel = viewModel,
                onCharacterClick = { characterId ->
                    navController.navigate(Screen.Detail.createRoute(characterId))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("characterId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getInt("characterId") ?: 1
            CharacterDetailScreen(
                characterId = characterId,
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
