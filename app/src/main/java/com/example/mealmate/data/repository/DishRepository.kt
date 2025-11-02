package com.example.mealmate.data.repository

import androidx.lifecycle.LiveData
import com.example.mealmate.data.database.DishDao
import com.example.mealmate.data.entities.Dish
import kotlinx.coroutines.flow.Flow

class DishRepository(private val dishDao: DishDao) {

    fun getAllDishes(): LiveData<List<Dish>> = dishDao.getAllDishes()

    fun getDishesByCategory(category: String): LiveData<List<Dish>> =
        dishDao.getDishesByCategory(category)

    suspend fun getDishById(dishId: Long): Dish? = dishDao.getDishById(dishId)

    suspend fun insertDish(dish: Dish): Long = dishDao.insert(dish)

    suspend fun updateDish(dish: Dish) = dishDao.update(dish)

    suspend fun deleteDish(dish: Dish) = dishDao.delete(dish)

    suspend fun markAsTried(dishId: Long, rating: Float, notes: String) {
        dishDao.markAsTried(dishId, rating, notes, System.currentTimeMillis())
    }

    fun getDishesToTry(): LiveData<List<Dish>> = dishDao.getDishesToTry()

    fun getTriedDishes(): LiveData<List<Dish>> = dishDao.getTriedDishes()
}