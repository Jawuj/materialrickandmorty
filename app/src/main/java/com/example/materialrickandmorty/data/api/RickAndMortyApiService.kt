package com.example.materialrickandmorty.data.api

import com.example.materialrickandmorty.data.model.Character
import com.example.materialrickandmorty.data.model.CharacterResponse
import com.example.materialrickandmorty.data.model.Episode
import com.example.materialrickandmorty.data.model.EpisodeResponse
import com.example.materialrickandmorty.data.model.LocationResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApiService {

    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int? = null,
        @Query("name") name: String? = null
    ): CharacterResponse

    @GET("character/{id}")
    suspend fun getCharacterById(
        @Path("id") id: Int
    ): Character

    @GET("location")
    suspend fun getLocations(
        @Query("page") page: Int? = null
    ): LocationResponse

    @GET("episode")
    suspend fun getEpisodes(
        @Query("page") page: Int? = null
    ): EpisodeResponse

    @GET("episode/{ids}")
    suspend fun getMultipleEpisodes(
        @Path("ids") ids: String
    ): List<Episode>

    @GET("episode/{id}")
    suspend fun getSingleEpisode(
        @Path("id") id: Int
    ): Episode
}
