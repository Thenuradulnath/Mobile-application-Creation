package com.example.mealmate.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mealmate.data.entities.Dish
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {

    @Query("SELECT * FROM dishes ORDER BY dateAdded DESC")
    fun getAllDishes(): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE category = :category ORDER BY dateAdded DESC")
    fun getDishesByCategory(category: String): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE id = :dishId")
    suspend fun getDishById(dishId: Long): Dish?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dish: Dish): Long

    @Update
    suspend fun update(dish: Dish)

    @Delete
    suspend fun delete(dish: Dish)

    @Query("UPDATE dishes SET isTried = 1, rating = :rating, tastingNotes = :notes, dateTried = :dateTried WHERE id = :dishId")
    suspend fun markAsTried(dishId: Long, rating: Float, notes: String, dateTried: Long)

    @Query("SELECT * FROM dishes WHERE isTried = 0 ORDER BY dateAdded DESC")
    fun getDishesToTry(): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE isTried = 1 ORDER BY dateTried DESC")
    fun getTriedDishes(): LiveData<List<Dish>>
}