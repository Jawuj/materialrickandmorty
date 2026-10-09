package com.example.materialrickandmorty.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.materialrickandmorty.data.auth.GoogleSignInHelper
import com.example.materialrickandmorty.ui.screens.auth.AuthScreen
import com.example.materialrickandmorty.ui.screens.detail.CharacterDetailScreen
import com.example.materialrickandmorty.ui.screens.loading.LoadingScreen
import com.example.materialrickandmorty.ui.screens.pick.PickScreen
import com.example.materialrickandmorty.ui.viewmodel.RickAndMortyViewModel
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: RickAndMortyViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    LaunchedEffect(authError) {
        authError?.takeIf { it.isNotBlank() }?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route,
        modifier = modifier
    ) {
        composable(route = Screen.Loading.route) {
            LoadingScreen(
                onLoadingComplete = {
                    if (currentUser != null) {
                        navController.navigate(Screen.Pick.route) {
                            popUpTo(Screen.Loading.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Loading.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = Screen.Auth.route) {
            // Auto-navigate to PickScreen when logged in
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate(Screen.Pick.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            }

            AuthScreen(
                isLoading = isAuthLoading,
                errorMessage = authError,
                onLoginClick = { email, pass ->
                    viewModel.loginWithEmail(email, pass)
                },
                onSignUpClick = { email, pass, name ->
                    viewModel.signUpWithEmail(email, pass, name)
                },
                onGoogleSignInClick = {
                    coroutineScope.launch {
                        GoogleSignInHelper.performCredentialManagerSignIn(context)
                            .onSuccess(viewModel::signInWithGoogleCredential)
                            .onFailure { error ->
                                viewModel.reportAuthError(
                                    "No se pudo iniciar sesión con Google: ${error.localizedMessage ?: error.javaClass.simpleName}. " +
                                        "Comprueba que Google esté habilitado en Firebase Authentication y que el SHA-1 de esta app esté registrado."
                                )
                            }
                    }
                },
                isFullBrandScreen = true,
                onGuestClick = {
                    navController.navigate(Screen.Pick.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
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
