package com.example.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mealmate.R
import android.widget.AdapterView
import android.widget.TextView
import com.example.mealmate.data.entities.toJsonString
import com.example.mealmate.data.entities.Dish
import com.example.mealmate.databinding.FragmentAddEditDishBinding
import com.example.mealmate.ui.viewmodels.DishViewModel
import com.google.android.material.chip.Chip
import java.util.Locale

class AddEditDishFragment : Fragment() {

    private var _binding: FragmentAddEditDishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DishViewModel by viewModels()

    private var isEditing = false
    private var currentDish: Dish? = null
    private var dishId: Long = 0L
    private val defaultAllergens = listOf(
        "Gluten",
        "Egg",
        "Fish",
        "Milk",
        "Soy",
        "Nuts",
        "Shellfish",
        "Peanuts",
        "Sesame",
        "Wheat"
    )
    private val allergenOptions = mutableSetOf<String>()

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
        setupAllergenChips()

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
        setupAllergenChips(dish.getAllergenList())
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

        binding.btnAddAllergen.setOnClickListener {
            val input = binding.etCustomAllergen.text?.toString()?.trim().orEmpty()
            if (input.isEmpty()) {
                binding.tilCustomAllergen.error = "Allergen name is required"
            } else {
                binding.tilCustomAllergen.error = null
                addCustomAllergen(input)
                binding.etCustomAllergen.text?.clear()
            }
        }

        binding.etCustomAllergen.doAfterTextChanged {
            if (!it.isNullOrBlank()) {
                binding.tilCustomAllergen.error = null
            }
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

        val allergens = getSelectedAllergens().toJsonString()

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

    private fun setupAllergenChips(selectedAllergens: List<String> = emptyList()) {
        val chipGroup = binding.chipGroupAllergens
        chipGroup.removeAllViews()
        allergenOptions.clear()

        val combinedAllergens = (defaultAllergens + selectedAllergens).distinct()
        combinedAllergens.forEach { allergen ->
            allergenOptions.add(allergen)
            val chip = createAllergenChip(allergen)
            chip.isChecked = selectedAllergens.contains(allergen)
            chipGroup.addView(chip)
        }
    }

    private fun createAllergenChip(allergen: String): Chip {
        return Chip(requireContext()).apply {
            text = allergen
            isCheckable = true
            isCheckedIconVisible = true
        }
    }

    private fun addCustomAllergen(allergen: String) {
        val formatted = allergen.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        val existingChip = findChipByText(formatted)
        if (existingChip != null) {
            existingChip.isChecked = true
            return
        }
        if (allergenOptions.add(formatted)) {
            val chip = createAllergenChip(formatted).apply {
                isChecked = true
            }
            binding.chipGroupAllergens.addView(chip)
        }
    }

    private fun findChipByText(text: String): Chip? {
        val chipGroup = binding.chipGroupAllergens
        for (index in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(index) as? Chip
            if (chip?.text?.toString().equals(text, ignoreCase = true)) {
                return chip
            }
        }
        return null
    }

    private fun getSelectedAllergens(): List<String> {
        val chipGroup = binding.chipGroupAllergens
        val selected = mutableListOf<String>()
        for (index in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(index) as? Chip
            if (chip?.isChecked == true) {
                selected.add(chip.text.toString())
            }
        }
        return selected
    }
}
