package com.example.mealmate.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.mealmate.MealMateApplication
import com.example.mealmate.data.entities.Dish
import com.example.mealmate.data.repository.DishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DishViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DishRepository = (application as MealMateApplication).repository

    private val _allDishes = MutableStateFlow<List<Dish>>(emptyList())
    val allDishes: StateFlow<List<Dish>> = _allDishes.asStateFlow()

    private val _filteredDishes = MutableStateFlow<List<Dish>>(emptyList())
    val filteredDishes: StateFlow<List<Dish>> = _filteredDishes.asStateFlow()

    private val _currentFilter = MutableLiveData("ALL")
    val currentFilter: LiveData<String> get() = _currentFilter

    private val _selectedDish = MutableLiveData<Dish?>()
    val selectedDish: LiveData<Dish?> get() = _selectedDish

    private val _showTriedOnly = MutableStateFlow(false)
    val showTriedOnly: StateFlow<Boolean> = _showTriedOnly.asStateFlow()

    init {
        loadAllDishes()
    }

    private fun loadAllDishes() {
        viewModelScope.launch {
            // If your repository returns LiveData<List<Dish>>, convert to Flow and collect:
            repository.getAllDishes()
                .asFlow()
                .collectLatest { dishes ->
                    _allDishes.value = dishes
                    applyCombinedFilters()
                }
        }
    }

    fun setShowTriedOnly(showTried: Boolean) {
        _showTriedOnly.value = showTried
        applyCombinedFilters()
    }

    fun filterDishes(category: String) {
        _currentFilter.value = category
        applyCombinedFilters()
    }

    private fun applyCombinedFilters() {
        viewModelScope.launch {
            val category = _currentFilter.value ?: "ALL"

            val base = if (category == "ALL") {
                _allDishes.value
            } else {
                _allDishes.value.filter { it.category == category }
            }

            val finalList = if (_showTriedOnly.value) {
                base.filter { it.isTried }
            } else {
                base.filter { !it.isTried }
            }

            _filteredDishes.value = finalList
        }
    }

    fun getDishById(dishId: Long) {
        viewModelScope.launch {
            val dish = repository.getDishById(dishId)
            _selectedDish.value = dish
        }
    }

    fun addDish(dish: Dish) = viewModelScope.launch {
        repository.insertDish(dish)
    }

    fun updateDish(dish: Dish) = viewModelScope.launch {
        repository.updateDish(dish)
    }

    fun deleteDish(dish: Dish) = viewModelScope.launch {
        repository.deleteDish(dish)
        applyCombinedFilters()
    }

    fun markAsTried(dishId: Long, rating: Float, notes: String) = viewModelScope.launch {
        repository.markAsTried(dishId, rating, notes)
        getDishById(dishId)
        applyCombinedFilters()
    }

    fun clearSelectedDish() {
        _selectedDish.value = null
    }
}
