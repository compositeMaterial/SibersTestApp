package com.killzone.siberstest.di

import android.content.Context
import androidx.room.Room
import com.killzone.siberstest.db.PokemonDatabase
import com.killzone.siberstest.network.PokeApiService
import com.killzone.siberstest.util.Constants.BASE_URL
import com.killzone.siberstest.util.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideRetrofitObject() = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
        .create(PokeApiService::class.java)

    @Provides
    @Singleton
    fun provideTrackerDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app, PokemonDatabase::class.java, DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideTrackerDao(db: PokemonDatabase) = db.getDatabase()

}