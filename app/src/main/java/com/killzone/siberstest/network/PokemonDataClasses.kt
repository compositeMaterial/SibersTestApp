package com.killzone.siberstest.network

import androidx.room.Entity
import com.killzone.siberstest.db.Pokemon

data class PokemonList (
    val count : Int,
    val next : String,
    val previous : String,
    val results : List<Result>
) {
    fun convert(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until results.size) {
            list.add(results[i].name)
        }
        return list
    }
}

data class Result (
    val name : String,
    val url : String
)

data class PokemonFullInfo (
    val abilities : List<Abilities>,
    val base_experience : Int,
    val forms : List<Forms>,
    val game_indices : List<Game_indices>,
    val height : Int,
    val held_items : List<Held_items>,
    val id : Int,
    val is_default : Boolean,
    val location_area_encounters : String,
    val moves : List<Moves>,
    val name : String,
    val order : Int,
    val species : Species,
    val sprites : Sprites,
    val stats : List<Stats>,
    val types : List<Types>,
    val weight : Int
) {
    fun convertToPokemon(): Pokemon {
        return Pokemon(
            id = this.id,
            name = this.name,
            type = this.types[0].type.name,
            imageUrl = this.sprites.front_default,
            weight = this.weight,
            height = this.height,
            attack = this.stats[0].base_stat,
            defense = this.stats[1].base_stat,
            hp = this.stats[2].base_stat
        )
    }
}



data class Abilities (
    val ability : Ability,
    val is_hidden : Boolean,
    val slot : Int
)

data class Ability (
    val name : String,
    val url : String
)

data class Forms (
    val name : String,
    val url : String
)

data class Game_indices (

    val game_index : Int,
    val version : Version
)

data class Held_items (
    val item : Item,
    val version_details : List<Version_details>
)

data class Item (
    val name : String,
    val url : String
)

data class Move (
    val name : String,
    val url : String
)

data class Move_learn_method (
    val name : String,
    val url : String
)

data class Moves (
    val move : Move,
    val version_group_details : List<Version_group_details>
)

data class Species (
    val name : String,
    val url : String
)

data class Sprites (
    val back_default : String,
    val back_female : String,
    val back_shiny : String,
    val back_shiny_female : String,
    val front_default : String,
    val front_female : String,
    val front_shiny : String,
    val front_shiny_female : String
)

data class Stat (
    val name : String,
    val url : String
)

data class Stats (
    val base_stat : Int,
    val effort : Int,
    val stat : Stat
)

data class Type (
    val name : String,
    val url : String
)

data class Types (
    val slot : Int,
    val type : Type
)


data class Version (
    val name : String,
    val url : String
)

data class Version_details (
    val rarity : Int,
    val version : Version
)

data class Version_group (
    val name : String,
    val url : String
)

data class Version_group_details (
    val level_learned_at : Int,
    val move_learn_method : Move_learn_method,
    val version_group : Version_group
)