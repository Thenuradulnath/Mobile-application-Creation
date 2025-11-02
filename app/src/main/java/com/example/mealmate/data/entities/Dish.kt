package com.example.mealmate.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String,
    val category: String, // "STARTER", "MAIN", "DESSERT", "SIDE", "LUNCH"
    val restaurant: String = "Seagrass Boutique Hospitality",
    val allergens: String, // JSON string of list
    val isTried: Boolean = false,
    val rating: Float = 0f,
    val tastingNotes: String = "",
    val photoPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateTried: Long? = null
) {
    fun getAllergenList(): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(allergens, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

fun List<String>.toJsonString(): String {
    return Gson().toJson(this)
}