package com.example.materialrickandmorty.data.repository

import com.example.materialrickandmorty.data.api.RickAndMortyApiService
import com.example.materialrickandmorty.data.api.RetrofitClient
import com.example.materialrickandmorty.data.model.Character
import com.example.materialrickandmorty.data.model.CharacterResponse
import com.example.materialrickandmorty.data.model.Episode
import com.example.materialrickandmorty.data.model.EpisodeResponse
import com.example.materialrickandmorty.data.model.LocationResponse
import retrofit2.HttpException

class RickAndMortyRepository(
    private val apiService: RickAndMortyApiService = RetrofitClient.apiService
) {

    suspend fun getCharacters(page: Int = 1, name: String? = null): Result<CharacterResponse> {
        return runCatching {
            val queryName = if (name.isNullOrBlank()) null else name.trim()
            apiService.getCharacters(page = page, name = queryName)
        }.recoverCatching { throwable ->
            if (throwable is HttpException && throwable.code() == 404) {
                CharacterResponse(info = null, results = emptyList())
            } else {
                throw throwable
            }
        }
    }

    suspend fun getCharacterById(id: Int): Result<Character> {
        return runCatching {
            apiService.getCharacterById(id)
        }
    }

    suspend fun getLocations(page: Int = 1): Result<LocationResponse> {
        return runCatching {
            apiService.getLocations(page = page)
        }
    }

    suspend fun getEpisodes(page: Int = 1): Result<EpisodeResponse> {
        return runCatching {
            apiService.getEpisodes(page = page)
        }
    }

    suspend fun getEpisodesForUrls(urls: List<String>): Result<List<Episode>> {
        return runCatching {
            val ids = urls.mapNotNull { url ->
                url.trimEnd('/').split('/').lastOrNull()?.toIntOrNull()
            }
            if (ids.isEmpty()) return@runCatching emptyList()

            if (ids.size == 1) {
                val single = apiService.getSingleEpisode(ids.first())
                listOf(single)
            } else {
                val idsString = ids.joinToString(",")
                apiService.getMultipleEpisodes(idsString)
            }
        }
    }
}
