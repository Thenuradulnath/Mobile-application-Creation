package com.example.mealmate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mealmate.data.entities.Dish

@Database(
    entities = [Dish::class],
    version = 1,
    exportSchema = false
)
abstract class MealMateDatabase : RoomDatabase() {

    abstract fun dishDao(): DishDao

    companion object {
        @Volatile
        private var INSTANCE: MealMateDatabase? = null

        fun getDatabase(context: Context): MealMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MealMateDatabase::class.java,
                    "mealmate_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}