package com.example.materialrickandmorty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.materialrickandmorty.data.model.Character
import com.example.materialrickandmorty.data.model.Episode
import com.example.materialrickandmorty.data.model.Location
import com.example.materialrickandmorty.data.repository.RickAndMortyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val repository: RickAndMortyRepository = RickAndMortyRepository()
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

    private var searchJob: Job? = null

    init {
        loadCharacters(reset = true)
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
                            error = throwable.localizedMessage ?: "Error al cargar personajes"
                        )
                    }
                }
        }
    }

    fun toggleFavorite(character: Character) {
        val current = _favoriteIds.value.toMutableSet()
        val currentFavs = _favoriteCharacters.value.toMutableList()

        if (current.contains(character.id)) {
            current.remove(character.id)
            currentFavs.removeAll { it.id == character.id }
        } else {
            current.add(character.id)
            if (currentFavs.none { it.id == character.id }) {
                currentFavs.add(character)
            }
        }
        _favoriteIds.value = current
        _favoriteCharacters.value = currentFavs
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
                    _locationsState.value = UiState.Error(throwable.localizedMessage ?: "Error al cargar ubicaciones")
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
                    _episodesState.value = UiState.Error(throwable.localizedMessage ?: "Error al cargar episodios")
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
                        error = throwable.localizedMessage ?: "Error al obtener detalles"
                    )
                }
        }
    }
}
