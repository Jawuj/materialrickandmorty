package com.example.materialrickandmorty.ui.screens.pick

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialrickandmorty.data.auth.GoogleSignInHelper
import com.example.materialrickandmorty.data.model.Character
import com.example.materialrickandmorty.data.model.Episode
import com.example.materialrickandmorty.data.model.Location
import com.example.materialrickandmorty.ui.screens.auth.AuthScreen
import com.example.materialrickandmorty.ui.screens.profile.ProfileScreen
import com.example.materialrickandmorty.ui.theme.CardContainer
import com.example.materialrickandmorty.ui.theme.CardText
import com.example.materialrickandmorty.ui.theme.DarkBackground
import com.example.materialrickandmorty.ui.theme.HeaderTitleText
import com.example.materialrickandmorty.ui.theme.IconPurple
import com.example.materialrickandmorty.ui.theme.NavSelectionPill
import com.example.materialrickandmorty.ui.viewmodel.RickAndMortyViewModel
import com.example.materialrickandmorty.ui.viewmodel.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun PickScreen(
    viewModel: RickAndMortyViewModel,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val charactersUiState by viewModel.charactersState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val favoriteCharacters by viewModel.favoriteCharacters.collectAsState()
    val locationsUiState by viewModel.locationsState.collectAsState()
    val episodesUiState by viewModel.episodesState.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    // Google Sign-In launcher fallback
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

    fun handleGuestAction(action: () -> Unit) {
        if (currentUser == null) {
            Toast.makeText(context, "Sign in to save favorites and watched episodes", Toast.LENGTH_SHORT).show()
            selectedTab = 4
        } else {
            action()
        }
    }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> viewModel.loadLocations()
            2 -> viewModel.loadEpisodes()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = CardContainer,
                tonalElevation = 8.dp
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IconPurple,
                    selectedTextColor = IconPurple,
                    unselectedIconColor = IconPurple,
                    unselectedTextColor = IconPurple,
                    indicatorColor = NavSelectionPill
                )

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Characters") },
                    label = { Text("Characters", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = navItemColors
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Locations") },
                    label = { Text("Locations", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = navItemColors
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Movie, contentDescription = "Episodes") },
                    label = { Text("Episodes", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = navItemColors
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = navItemColors
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile", fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = navItemColors
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (selectedTab == 4) 0.dp else 16.dp)
        ) {
            if (selectedTab != 4) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar for Characters Tab or Header with Back Button for other Tabs
            if (selectedTab == 0) {
                OutlinedTextField(
                    value = charactersUiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    placeholder = {
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = IconPurple
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = IconPurple,
                            modifier = Modifier.size(30.dp)
                        )
                    },
                    shape = RoundedCornerShape(32.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardContainer,
                        unfocusedContainerColor = CardContainer,
                        disabledContainerColor = CardContainer,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = IconPurple,
                        unfocusedTextColor = IconPurple
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
            } else if (selectedTab != 4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    IconButton(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardContainer)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IconPurple
                        )
                    }

                    val titleText = when (selectedTab) {
                        1 -> "Locations"
                        2 -> "Episodes"
                        3 -> "Favorites"
                        else -> ""
                    }

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = HeaderTitleText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> CharacterListTab(
                        characters = charactersUiState.characters,
                        isLoading = charactersUiState.isLoading,
                        isPagingLoading = charactersUiState.isPagingLoading,
                        favoriteIds = favoriteIds,
                        errorMessage = charactersUiState.error,
                        onFavoriteToggle = { character ->
                            handleGuestAction { viewModel.toggleFavorite(character) }
                        },
                        onCharacterClick = onCharacterClick,
                        onLoadMore = { viewModel.loadCharacters(reset = false) }
                    )

                    1 -> LocationsTab(
                        state = locationsUiState
                    )

                    2 -> EpisodesTab(
                        state = episodesUiState,
                        favoriteEpisodes = userProfile.favoriteEpisodes,
                        episodesWatched = userProfile.episodesWatched,
                        onFavoriteToggle = { epId ->
                            handleGuestAction { viewModel.toggleFavoriteEpisode(epId) }
                        },
                        onWatchedToggle = { epId ->
                            handleGuestAction { viewModel.toggleEpisodeWatched(epId) }
                        }
                    )

                    3 -> FavoritesTab(
                        favoriteCharacters = favoriteCharacters,
                        onFavoriteToggle = { character ->
                            handleGuestAction { viewModel.toggleFavorite(character) }
                        },
                        onCharacterClick = onCharacterClick
                    )

                    4 -> {
                        if (currentUser != null) {
                            ProfileScreen(
                                userProfile = userProfile,
                                onBackClick = { selectedTab = 0 },
                                onUpdateName = { viewModel.updateDisplayName(it) },
                                onToggleHideEmail = { viewModel.toggleHideEmail(it) },
                                onLogOutClick = { viewModel.signOut() }
                            )
                        } else {
                            AuthScreen(
                                isLoading = isAuthLoading,
                                errorMessage = authError,
                                onLoginClick = { email, pass -> viewModel.loginWithEmail(email, pass) },
                                onSignUpClick = { email, pass, name -> viewModel.signUpWithEmail(email, pass, name) },
                                onGoogleSignInClick = {
                                    coroutineScope.launch {
                                        GoogleSignInHelper.performCredentialManagerSignIn(context)
                                            .onSuccess { idToken ->
                                                viewModel.signInWithGoogleCredential(idToken)
                                            }
                                            .onFailure {
                                                val gso = GoogleSignInHelper.getGoogleSignInOptions(context)
                                                val client = GoogleSignIn.getClient(context, gso)
                                                googleSignInLauncher.launch(client.signInIntent)
                                            }
                                    }
                                },
                                isFullBrandScreen = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterListTab(
    characters: List<Character>,
    isLoading: Boolean,
    isPagingLoading: Boolean,
    favoriteIds: Set<Int>,
    errorMessage: String?,
    onFavoriteToggle: (Character) -> Unit,
    onCharacterClick: (Int) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= characters.size - 3) {
                    onLoadMore()
                }
            }
    }

    if (isLoading && characters.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CardContainer)
        }
    } else if (errorMessage != null && characters.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = CardText
            )
        }
    } else if (characters.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No characters found",
                style = MaterialTheme.typography.bodyLarge,
                color = CardText
            )
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = characters,
                key = { it.id }
            ) { character ->
                CharacterCard(
                    character = character,
                    isFavorite = favoriteIds.contains(character.id),
                    onFavoriteToggle = { onFavoriteToggle(character) },
                    onClick = { onCharacterClick(character.id) }
                )
            }

            if (isPagingLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = CardContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationsTab(
    state: UiState<List<Location>>
) {
    when (state) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CardContainer)
            }
        }

        is UiState.Success -> {
            val locations = state.data
            if (locations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No locations available", color = CardText)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(locations, key = { it.id }) { loc ->
                        LocationCard(location = loc)
                    }
                }
            }
        }

        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = CardText)
            }
        }

        else -> {}
    }
}

@Composable
fun EpisodesTab(
    state: UiState<List<Episode>>,
    favoriteEpisodes: List<Int> = emptyList(),
    episodesWatched: List<Int> = emptyList(),
    onFavoriteToggle: (Int) -> Unit = {},
    onWatchedToggle: (Int) -> Unit = {}
) {
    when (state) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CardContainer)
            }
        }

        is UiState.Success -> {
            val episodes = state.data
            if (episodes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No episodes available", color = CardText)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(episodes, key = { it.id }) { ep ->
                        EpisodeCard(
                            episode = ep,
                            isFavorite = favoriteEpisodes.contains(ep.id),
                            isWatched = episodesWatched.contains(ep.id),
                            onFavoriteToggle = { onFavoriteToggle(ep.id) },
                            onWatchedToggle = { onWatchedToggle(ep.id) }
                        )
                    }
                }
            }
        }

        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = CardText)
            }
        }

        else -> {}
    }
}

@Composable
fun FavoritesTab(
    favoriteCharacters: List<Character>,
    onFavoriteToggle: (Character) -> Unit,
    onCharacterClick: (Int) -> Unit
) {
    if (favoriteCharacters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = HeaderTitleText,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No favorite characters yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HeaderTitleText
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(favoriteCharacters, key = { it.id }) { character ->
                CharacterCard(
                    character = character,
                    isFavorite = true,
                    onFavoriteToggle = { onFavoriteToggle(character) },
                    onClick = { onCharacterClick(character.id) }
                )
            }
        }
    }
}
