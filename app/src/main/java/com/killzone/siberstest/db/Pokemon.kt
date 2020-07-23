package com.killzone.siberstest.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.killzone.siberstest.network.*
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "pokemon_table")
data class Pokemon (
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val imageUrl: String,
    val weight: Int,
    val height: Int,
    val attack: Int,
    val defense: Int,
    val hp: Int
) : Parcelable



