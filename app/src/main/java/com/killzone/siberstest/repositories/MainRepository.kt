package com.killzone.siberstest.repositories

import com.killzone.siberstest.db.Pokemon
import com.killzone.siberstest.db.PokemonDao
import com.killzone.siberstest.network.PokeApiService
import com.killzone.siberstest.network.PokemonList
import com.killzone.siberstest.util.Constants.BASE_PAGE_SIZE
import com.killzone.siberstest.util.Constants.MAX_OFFSET_VALUE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class MainRepository @Inject constructor(
    private val networkService: PokeApiService,
    private val dbService: PokemonDao
) {

    // Return first initial PokemonList
    suspend fun getPokemonList(): Result<PokemonList> = withContext(Dispatchers.IO) {
        try {
            val list = networkService.getPokemons()
            return@withContext Result.Success(list)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // Returns next PokemonList, that contains url to the another page
    suspend fun getNextPokemonList(url: String): Result<PokemonList> = withContext(Dispatchers.IO) {
        try {
            val list = networkService.getNextPokemonList(url)
            return@withContext Result.Success(list)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // Returns list of pokemons with detail info
    suspend fun getPokemonInfoList(list: List<String>): Result<List<Pokemon>> = withContext(Dispatchers.IO) {
        val tempList = mutableListOf<Pokemon>()
        for (element in list) {
            val item = networkService.getPokemonInfo(element)
            tempList.add(item.convertToPokemon())
        }
        return@withContext Result.Success(tempList)
    }

    // Saves received pokemons data inside database
    suspend fun cachePokemons(pokemons: List<Pokemon>) = withContext(Dispatchers.IO) {
        dbService.deleteAll()
        for (element in pokemons) {
            dbService.insert(element)
        }
    }

    // Returns saved data from database
    suspend fun getCachedPokemons(): List<Pokemon> = withContext(Dispatchers.IO){
        return@withContext dbService.getAllPokemons()
    }

    // Returns PokemonList with random url
    suspend fun getRandomPokemonList(): Result<PokemonList> = withContext(Dispatchers.IO) {
        try {
            val randomNumber = (1..MAX_OFFSET_VALUE).random()
            val list = networkService.getPokemons(BASE_PAGE_SIZE, randomNumber)
            return@withContext Result.Success(list)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

}
