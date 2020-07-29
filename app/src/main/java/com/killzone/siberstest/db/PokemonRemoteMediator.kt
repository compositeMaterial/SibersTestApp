package com.killzone.siberstest.db

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.killzone.siberstest.network.PokeApiService
import com.killzone.siberstest.util.Constants
import com.killzone.siberstest.util.Constants.BASE_PAGE_SIZE
import com.killzone.siberstest.util.Constants.POKEMON_STARTING_ID
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val query: Boolean,
    private val networkService: PokeApiService,
    private val db: PokemonDatabase
) : RemoteMediator<Int, Pokemon>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Pokemon>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: POKEMON_STARTING_ID
            }
            LoadType.PREPEND -> {

                val remoteKeys = getRemoteKeyForFirstItem(state)
                if (remoteKeys == null) {
                    throw InvalidObjectException("Remote key not found")
                }
                val prevKey = remoteKeys.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKeys.prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys == null || remoteKeys.nextKey == null) {
                    throw InvalidObjectException("Remote key should not be null")
                }
                remoteKeys.nextKey
            }
        }

        try {
            val response = mutableListOf<Pokemon>()

            var start = 1
            var end = 1

            /*if (query) {
                start =  (1..770).random()
                end = start + BASE_PAGE_SIZE
            } else {
                start = if (page == POKEMON_STARTING_ID) 1 else (page - 1) * Constants.BASE_PAGE_SIZE
                end = page * Constants.BASE_PAGE_SIZE
            }*/

            start = if (page == POKEMON_STARTING_ID) 1 else (page - 1) * Constants.BASE_PAGE_SIZE
            end = page * Constants.BASE_PAGE_SIZE

            for (i in start..end) {
                response.add(networkService.getPokemonInfo(i.toString()).convertToPokemon())
            }

            val endOfPaginationReached = response.isEmpty()


            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.remoteKeysDao().clearAllRemoteKeys()
                    db.pokemonDao().deleteAll()
                }
                val prevKey = if (page == POKEMON_STARTING_ID) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = response.map {
                    RemoteKeys(pokemonId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                db.remoteKeysDao().insertAll(keys)
                db.pokemonDao().insertAll(response)
            }
            return MediatorResult.Success(endOfPaginationReached = false)

        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }


    // Получаем последниюю страницу при помощи последнего покемона
    private suspend  fun getRemoteKeyForLastItem(state: PagingState<Int, Pokemon>): RemoteKeys? {
        return state.pages.lastOrNull() { it.data.isEmpty() }?.data?.lastOrNull()
            ?.let { pokemon ->
                db.remoteKeysDao().getKeyById(pokemon.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Pokemon>): RemoteKeys? {

        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id.let { pokemonId ->
                db.remoteKeysDao().getKeyById(pokemonId!!)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Pokemon>): RemoteKeys? {
        return state.pages.firstOrNull() { it.data.isEmpty() }?.data?.firstOrNull()
            ?.let { pokemon ->
                db.remoteKeysDao().getKeyById(pokemon.id)
            }
    }


}
