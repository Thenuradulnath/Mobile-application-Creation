package com.example.mealmate

import android.app.Application
import com.example.mealmate.data.database.MealMateDatabase
import com.example.mealmate.data.repository.DishRepository
import com.example.mealmate.utils.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MealMateApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { MealMateDatabase.getDatabase(this) }
    val repository by lazy { DishRepository(database.dishDao()) }

    override fun onCreate() {
        super.onCreate()
        prePopulateDatabase()
    }

    private fun prePopulateDatabase() {
        applicationScope.launch {
            // Check if database is empty and add sample data
            val dishes = database.dishDao().getAllDishes()
            if (dishes.value.isNullOrEmpty()) {
                SampleData.getSampleDishes().forEach { dish ->
                    database.dishDao().insert(dish)
                }
            }
        }
    }
}