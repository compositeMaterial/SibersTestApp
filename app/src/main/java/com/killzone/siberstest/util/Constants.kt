package com.killzone.siberstest.util

import com.bumptech.glide.GlideBuilder

object Constants {
    const val BASE_URL = "https://pokeapi.co/api/v2/"
    const val DATABASE_NAME = "pokemon.db"

    const val BASE_PAGE_SIZE = 30
    const val BASE_PAGE_OFFSET = 0
    const val MAX_OFFSET_VALUE = 770

    const val INITIAL_LOAD_STATE = true
    const val CONTINUOUS_LOAD_STATE = false
}

enum class SpinnerState {
    INIT_SPINNER,
    LOAD_SPINNER,
    INVISIBLE
}

enum class UserChoice {
    ATTACK,
    DEFENSE,
    HP,
    ATTACK_AND_DEFENSE,
    ATTACK_AND_HP,
    DEFENSE_AND_HP,
    ALL
}
