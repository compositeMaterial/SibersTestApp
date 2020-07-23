package com.killzone.siberstest.db

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.room.*



@Database(entities = [Pokemon::class], version = 1)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun getDatabase(): PokemonDao
}

