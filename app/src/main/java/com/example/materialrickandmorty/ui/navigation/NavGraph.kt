package com.example.materialrickandmorty.ui.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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

    // Fallback launcher for Google Sign In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        runCatching {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken ?: throw Exception("Google sign-in token is null")
            viewModel.signInWithGoogleCredential(idToken)
        }.onFailure { error ->
            Toast.makeText(context, error.localizedMessage ?: "Google sign-in failed", Toast.LENGTH_SHORT).show()
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
                            .onSuccess { idToken ->
                                viewModel.signInWithGoogleCredential(idToken)
                            }
                            .onFailure {
                                // Fallback to GoogleSignIn Intent
                                val gso = GoogleSignInHelper.getGoogleSignInOptions(context)
                                val client = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(client.signInIntent)
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
