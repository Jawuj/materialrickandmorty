package com.example.materialrickandmorty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.materialrickandmorty.data.model.Character
import com.example.materialrickandmorty.data.model.Episode
import com.example.materialrickandmorty.data.model.Location
import com.example.materialrickandmorty.data.model.UserProfile
import com.example.materialrickandmorty.data.repository.AuthRepository
import com.example.materialrickandmorty.data.repository.FirestoreRepository
import com.example.materialrickandmorty.data.repository.RickAndMortyRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CharactersUiState(
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val isPagingLoading: Boolean = false,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val error: String? = null
)

data class DetailUiState(
    val character: Character? = null,
    val episodes: List<Episode> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RickAndMortyViewModel(
    private val repository: RickAndMortyRepository = RickAndMortyRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _charactersState = MutableStateFlow(CharactersUiState())
    val charactersState: StateFlow<CharactersUiState> = _charactersState.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    private val _favoriteCharacters = MutableStateFlow<List<Character>>(emptyList())
    val favoriteCharacters: StateFlow<List<Character>> = _favoriteCharacters.asStateFlow()

    private val _locationsState = MutableStateFlow<UiState<List<Location>>>(UiState.Idle)
    val locationsState: StateFlow<UiState<List<Location>>> = _locationsState.asStateFlow()

    private val _episodesState = MutableStateFlow<UiState<List<Episode>>>(UiState.Idle)
    val episodesState: StateFlow<UiState<List<Episode>>> = _episodesState.asStateFlow()

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    // Auth & Profile State
    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow.toStateFlow(authRepository.currentUser)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private var searchJob: Job? = null
    private var profileObserverJob: Job? = null

    init {
        loadCharacters(reset = true)
        observeAuthAndProfile()
    }

    private fun observeAuthAndProfile() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                profileObserverJob?.cancel()
                if (user != null) {
                    profileObserverJob = viewModelScope.launch {
                        firestoreRepository.observeUserProfile(user.uid).collect { profile ->
                            if (profile != null) {
                                _userProfile.value = profile
                                _favoriteIds.value = profile.favorites.toSet()
                                updateFavoriteCharactersList(_charactersState.value.characters)
                            } else {
                                val newProfile = UserProfile(
                                    uid = user.uid,
                                    name = user.displayName ?: "Rick Fan",
                                    email = user.email ?: "",
                                    photoUrl = user.photoUrl?.toString() ?: ""
                                )
                                firestoreRepository.saveUserProfile(newProfile)
                                _userProfile.value = newProfile
                            }
                        }
                    }
                } else {
                    _userProfile.value = UserProfile()
                    _favoriteIds.value = emptySet()
                    _favoriteCharacters.value = emptyList()
                }
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authError.value = "Please enter email and password"
            return
        }
        _isAuthLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            authRepository.loginWithEmail(email, password)
                .onSuccess {
                    _isAuthLoading.value = false
                }
                .onFailure { throwable ->
                    _isAuthLoading.value = false
                    _authError.value = throwable.localizedMessage ?: "Login failed"
                }
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank()) {
            _authError.value = "Please enter email and password"
            return
        }
        _isAuthLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, password, name)
                .onSuccess { user ->
                    val profile = UserProfile(
                        uid = user.uid,
                        name = if (name.isNotBlank()) name else (user.displayName ?: "Rick Fan"),
                        email = user.email ?: email,
                        photoUrl = user.photoUrl?.toString() ?: ""
                    )
                    firestoreRepository.saveUserProfile(profile)
                    _isAuthLoading.value = false
                }
                .onFailure { throwable ->
                    _isAuthLoading.value = false
                    _authError.value = throwable.localizedMessage ?: "Sign up failed"
                }
        }
    }

    fun signInWithGoogleCredential(idToken: String) {
        _isAuthLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            authRepository.signInWithGoogleCredential(idToken)
                .onSuccess { user ->
                    val profile = UserProfile(
                        uid = user.uid,
                        name = user.displayName ?: "Rick Fan",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString() ?: ""
                    )
                    firestoreRepository.saveUserProfile(profile)
                    _isAuthLoading.value = false
                }
                .onFailure { throwable ->
                    _isAuthLoading.value = false
                    _authError.value = throwable.localizedMessage ?: "Google sign in failed"
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun updateDisplayName(newName: String) {
        val user = currentUser.value ?: return
        val updatedProfile = _userProfile.value.copy(name = newName)
        _userProfile.value = updatedProfile
        viewModelScope.launch {
            authRepository.updateDisplayName(newName)
            firestoreRepository.updateName(user.uid, newName)
        }
    }

    fun toggleHideEmail(hideEmail: Boolean) {
        val user = currentUser.value ?: return
        val updatedSettings = _userProfile.value.settings.copy(hideEmail = hideEmail)
        _userProfile.value = _userProfile.value.copy(settings = updatedSettings)
        viewModelScope.launch {
            firestoreRepository.updateHideEmailSetting(user.uid, hideEmail)
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _charactersState.update { it.copy(searchQuery = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadCharacters(reset = true)
        }
    }

    fun loadCharacters(reset: Boolean = false) {
        val currentState = _charactersState.value
        val nextPage = if (reset) 1 else currentState.currentPage + 1

        if (reset) {
            _charactersState.update { it.copy(isLoading = true, error = null) }
        } else {
            if (currentState.isPagingLoading || !currentState.hasNextPage) return
            _charactersState.update { it.copy(isPagingLoading = true) }
        }

        viewModelScope.launch {
            val query = _charactersState.value.searchQuery
            repository.getCharacters(page = nextPage, name = query)
                .onSuccess { response ->
                    val newResults = response.results ?: emptyList()
                    val updatedList = if (reset) newResults else currentState.characters + newResults
                    val hasNext = response.info?.next != null

                    updateFavoriteCharactersList(updatedList)

                    _charactersState.update {
                        it.copy(
                            characters = updatedList,
                            isLoading = false,
                            isPagingLoading = false,
                            currentPage = nextPage,
                            hasNextPage = hasNext,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _charactersState.update {
                        it.copy(
                            isLoading = false,
                            isPagingLoading = false,
                            error = throwable.localizedMessage ?: "Error loading characters"
                        )
                    }
                }
        }
    }

    fun toggleFavorite(character: Character) {
        val user = currentUser.value
        val currentFavIds = _favoriteIds.value.toMutableSet()
        val currentFavs = _favoriteCharacters.value.toMutableList()

        if (currentFavIds.contains(character.id)) {
            currentFavIds.remove(character.id)
            currentFavs.removeAll { it.id == character.id }
        } else {
            currentFavIds.add(character.id)
            if (currentFavs.none { it.id == character.id }) {
                currentFavs.add(character)
            }
        }
        _favoriteIds.value = currentFavIds
        _favoriteCharacters.value = currentFavs

        val currentProfile = _userProfile.value
        val updatedProfile = currentProfile.copy(favorites = currentFavIds.toList())
        _userProfile.value = updatedProfile

        if (user != null && user.uid.isNotBlank()) {
            viewModelScope.launch {
                firestoreRepository.toggleFavoriteCharacter(user.uid, character.id, currentProfile.favorites)
            }
        }
    }

    fun toggleFavoriteEpisode(episodeId: Int) {
        val currentProfile = _userProfile.value
        val currentFavs = currentProfile.favoriteEpisodes.toMutableList()
        if (currentFavs.contains(episodeId)) {
            currentFavs.remove(episodeId)
        } else {
            currentFavs.add(episodeId)
        }
        val updatedProfile = currentProfile.copy(favoriteEpisodes = currentFavs)
        _userProfile.value = updatedProfile

        val user = currentUser.value
        if (user != null && user.uid.isNotBlank()) {
            viewModelScope.launch {
                firestoreRepository.toggleFavoriteEpisode(user.uid, episodeId, currentProfile.favoriteEpisodes)
            }
        }
    }

    fun toggleEpisodeWatched(episodeId: Int) {
        val currentProfile = _userProfile.value
        val currentWatched = currentProfile.episodesWatched.toMutableList()
        if (currentWatched.contains(episodeId)) {
            currentWatched.remove(episodeId)
        } else {
            currentWatched.add(episodeId)
        }
        val updatedProfile = currentProfile.copy(episodesWatched = currentWatched)
        _userProfile.value = updatedProfile

        val user = currentUser.value
        if (user != null && user.uid.isNotBlank()) {
            viewModelScope.launch {
                firestoreRepository.toggleEpisodeWatched(user.uid, episodeId, currentProfile.episodesWatched)
            }
        }
    }

    private fun updateFavoriteCharactersList(allLoadedCharacters: List<Character>) {
        val favIds = _favoriteIds.value
        val currentFavs = _favoriteCharacters.value.toMutableList()
        allLoadedCharacters.filter { favIds.contains(it.id) }.forEach { char ->
            if (currentFavs.none { it.id == char.id }) {
                currentFavs.add(char)
            }
        }
        _favoriteCharacters.value = currentFavs
    }

    fun loadLocations() {
        if (_locationsState.value is UiState.Success) return
        _locationsState.value = UiState.Loading
        viewModelScope.launch {
            repository.getLocations(page = 1)
                .onSuccess { response ->
                    _locationsState.value = UiState.Success(response.results ?: emptyList())
                }
                .onFailure { throwable ->
                    _locationsState.value = UiState.Error(throwable.localizedMessage ?: "Error loading locations")
                }
        }
    }

    fun loadEpisodes() {
        if (_episodesState.value is UiState.Success) return
        _episodesState.value = UiState.Loading
        viewModelScope.launch {
            repository.getEpisodes(page = 1)
                .onSuccess { response ->
                    _episodesState.value = UiState.Success(response.results ?: emptyList())
                }
                .onFailure { throwable ->
                    _episodesState.value = UiState.Error(throwable.localizedMessage ?: "Error loading episodes")
                }
        }
    }

    fun loadCharacterDetail(characterId: Int) {
        _detailState.value = DetailUiState(isLoading = true)
        viewModelScope.launch {
            val existingChar = _charactersState.value.characters.find { it.id == characterId }
            val characterResult = if (existingChar != null) {
                Result.success(existingChar)
            } else {
                repository.getCharacterById(characterId)
            }

            characterResult
                .onSuccess { char ->
                    _detailState.update { it.copy(character = char) }
                    if (char.episode.isNotEmpty()) {
                        repository.getEpisodesForUrls(char.episode)
                            .onSuccess { episodeList ->
                                _detailState.update { it.copy(episodes = episodeList, isLoading = false) }
                            }
                            .onFailure {
                                _detailState.update { it.copy(isLoading = false) }
                            }
                    } else {
                        _detailState.update { it.copy(isLoading = false) }
                    }
                }
                .onFailure { throwable ->
                    _detailState.value = DetailUiState(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Error fetching details"
                    )
                }
        }
    }

    private fun <T> Flow<T>.toStateFlow(initialValue: T): StateFlow<T> {
        val stateFlow = MutableStateFlow(initialValue)
        viewModelScope.launch {
            collect { value -> stateFlow.value = value }
        }
        return stateFlow.asStateFlow()
    }
}
