package com.killzone.siberstest.db

import androidx.room.*
import retrofit2.http.GET

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: Pokemon)

    @Query("DELETE FROM pokemon_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM pokemon_table")
    suspend fun getAllPokemons(): List<Pokemon>
}