package com.killzone.siberstest.network

import com.killzone.siberstest.db.Pokemon
import com.killzone.siberstest.util.Constants.BASE_PAGE_OFFSET
import com.killzone.siberstest.util.Constants.BASE_PAGE_SIZE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PokeApiService {

    @GET("pokemon")
    suspend fun getPokemons(
        @Query("limit") limit: Int = BASE_PAGE_SIZE,
        @Query("offset") offset: Int = BASE_PAGE_OFFSET): PokemonList

    @GET("pokemon/{pokemonId}")
    suspend fun getPokemonInfo(
        @Path("pokemonId") pokemonId: String): PokemonFullInfo

    @GET
    suspend fun getNextPokemonList(
        @Url url: String): PokemonList

}