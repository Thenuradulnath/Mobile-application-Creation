package com.example.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mealmate.R
import android.widget.AdapterView
import android.widget.TextView
import com.example.mealmate.data.entities.Dish
import com.example.mealmate.databinding.FragmentAddEditDishBinding
import com.example.mealmate.ui.viewmodels.DishViewModel

class AddEditDishFragment : Fragment() {

    private var _binding: FragmentAddEditDishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DishViewModel by viewModels()

    private var isEditing = false
    private var currentDish: Dish? = null
    private var dishId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryField()

        dishId = arguments?.getLong("dishId", 0L) ?: 0L
        isEditing = dishId != 0L

        setupUI()
        setupClickListeners()

        if (isEditing) {
            viewModel.getDishById(dishId)
            observeDish()
        } else {
            setupNewDish()
        }
    }


    private fun setupCategoryField() {
        val categories = listOf("STARTER", "MAIN", "DESSERT", "SIDE", "LUNCH")
        when (val view = binding.spCategory) {
            is android.widget.Spinner -> {
                val adapter = android.widget.ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Explicitly cast view to Spinner before setting the adapter to avoid overload ambiguity
                val spinner = view as android.widget.Spinner
                spinner.adapter = adapter
            }
            is android.widget.AutoCompleteTextView -> {
                val adapter = android.widget.ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )
                view.setAdapter(adapter)
            }
        }
    }



    private fun setupUI() {
        binding.toolbar.title = if (isEditing) {
            "Edit Dish"
        } else {
            "Add New Dish"
        }
    }


    private fun observeDish() {
        viewModel.selectedDish.observe(viewLifecycleOwner) { dish ->
            dish?.let {
                currentDish = it
                populateForm(it)
            }
        }
    }


    private fun populateForm(dish: Dish) {
        binding.etDishName.setText(dish.name)
        binding.etDescription.setText(dish.description)
        binding.etRestaurant.setText(dish.restaurant)
        val currentCategory = dish.category
        when (val view = binding.spCategory) {
            is android.widget.AdapterView<*> -> {
                // Look up the categories array and set the selection index
                val categoriesArray = try {
                    resources.getStringArray(R.array.categories)
                } catch (e: Exception) {
                    // If the array isn't found, fall back to a single-element array
                    arrayOf(currentCategory)
                }
                val index = categoriesArray.indexOf(currentCategory)
                if (index >= 0) {
                    view.setSelection(index)
                }
            }
            is TextView -> {
                // Set the text directly for a TextView or AutoCompleteTextView
                view.setText(currentCategory)
            }
        }
    }

    private fun setupNewDish() {
        currentDish = null
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                saveDish()
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateForm(): Boolean {
        val name = binding.etDishName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (name.isEmpty()) {
            binding.etDishName.error = "Dish name is required"
            return false
        }
        if (description.isEmpty()) {
            binding.etDescription.error = "Description is required"
            return false
        }
        return true
    }

    private fun saveDish() {
        val name = binding.etDishName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val restaurant = binding.etRestaurant.text.toString().trim()
        val category: String = when (val view = binding.spCategory) {
            is AdapterView<*> -> {
                val pos = view.selectedItemPosition
                val categoriesArray = resources.getStringArray(R.array.categories)
                if (pos in categoriesArray.indices) categoriesArray[pos] else ""
            }
            is TextView -> {
                view.text?.toString() ?: ""
            }
            else -> ""
        }

        val allergens = if (isEditing) {
            currentDish?.allergens ?: "[]"
        } else {
            "[]"
        }

        val dish = if (isEditing) {
            currentDish!!.copy(
                name = name,
                description = description,
                restaurant = restaurant,
                category = category,
                allergens = allergens
            )
        } else {
            Dish(
                name = name,
                description = description,
                restaurant = restaurant,
                category = category,
                allergens = allergens
            )
        }

        if (isEditing) {
            viewModel.updateDish(dish)
        } else {
            viewModel.addDish(dish)
        }

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.clearSelectedDish()
    }
}