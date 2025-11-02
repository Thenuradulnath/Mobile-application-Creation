package com.example.mealmate.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmate.R
import com.example.mealmate.data.entities.Dish
import com.example.mealmate.databinding.FragmentDishListBinding
import com.example.mealmate.ui.adapters.DishAdapter
import com.example.mealmate.ui.viewmodels.DishViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class DishListFragment : Fragment() {

    private var _binding: FragmentDishListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DishViewModel by viewModels()
    private lateinit var adapter: DishAdapter
    private lateinit var searchAdapter: ArrayAdapter<String>
    private var suggestionDishes: List<Dish> = emptyList()
    private var allDishesCache: List<Dish> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupTryFilter()
        setupFilterChips()
        setupFAB()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = DishAdapter(
            onDishClick = { dish -> navigateToDish(dish) },
            onDishLongClick = { dish -> showDeleteDialog(dish) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DishListFragment.adapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            )
        }
    }

    private fun setupTryFilter() {
        binding.toggleTryGroup.addOnButtonCheckedListener(
            MaterialButtonToggleGroup.OnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@OnButtonCheckedListener
                when (checkedId) {
                    R.id.btn_to_try -> viewModel.setShowTriedOnly(false)
                    R.id.btnTried  -> viewModel.setShowTriedOnly(true)
                }
            }
        )
        viewModel.setShowTriedOnly(false)
    }

    private fun setupFilterChips() {
        val categories = listOf("ALL", "STARTER", "MAIN", "DESSERT", "SIDE", "LUNCH")

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = when (category) {
                    "ALL"     -> "All Dishes"
                    "STARTER" -> "Starters"
                    "MAIN"    -> "Main Courses"
                    "DESSERT" -> "Desserts"
                    "SIDE"    -> "Sides"
                    "LUNCH"   -> "Lunch"
                    else      -> category
                }
                isCheckable = true
                if (category == "ALL") isChecked = true
            }

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.filterDishes(category)
            }

            binding.chipGroup.addView(chip)
        }
    }

    private fun setupSearch() {
        searchAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )

        binding.searchAutoComplete.setAdapter(searchAdapter)
        binding.searchAutoComplete.doAfterTextChanged { editable ->
            updateSearchSuggestions(editable?.toString().orEmpty())
        }
        binding.searchAutoComplete.setOnItemClickListener { _, _, position, _ ->
            suggestionDishes.getOrNull(position)?.let { navigateToDish(it) }
        }
        binding.searchAutoComplete.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handleSearchAction()
            } else {
                false
            }
        }
        binding.searchInputLayout.setEndIconOnClickListener {
            handleSearchAction()
        }
    }

    private fun setupFAB() {
        binding.fabAddDish.setOnClickListener {
            findNavController().navigate(
                R.id.action_dishListFragment_to_addEditDishFragment
            )
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredDishes.collect { dishes ->
                        adapter.submitList(dishes)
                        binding.emptyState.visibility =
                            if (dishes.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.allDishes.collect { dishes ->
                        allDishesCache = dishes
                        updateRestaurantHeadline(dishes)
                        val currentQuery = binding.searchAutoComplete.text?.toString().orEmpty()
                        if (currentQuery.isNotBlank()) {
                            updateSearchSuggestions(currentQuery)
                        } else {
                            clearSearchSuggestions()
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(dish: Dish) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Dish")
            .setMessage("Remove ${dish.name} from your list?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteDish(dish) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToDish(dish: Dish) {
        hideKeyboard()
        binding.searchAutoComplete.setText("", false)
        binding.searchAutoComplete.clearFocus()
        val bundle = bundleOf("dishId" to dish.id)
        findNavController().navigate(
            R.id.action_dishListFragment_to_dishDetailFragment,
            bundle
        )
    }

    private fun updateRestaurantHeadline(dishes: List<Dish>) {
        if (dishes.isEmpty()) {
            binding.restaurantTitle.text = getString(R.string.restaurant_title_placeholder)
            return
        }

        val restaurants = dishes.mapNotNull { dish ->
            dish.restaurant.takeIf { it.isNotBlank() }
        }.distinct()

        binding.restaurantTitle.text = when (restaurants.size) {
            0 -> getString(R.string.restaurant_title_placeholder)
            1 -> getString(R.string.restaurant_title_single, restaurants.first())
            else -> getString(R.string.restaurant_title_multi, restaurants.size)
        }
    }

    private fun updateSearchSuggestions(query: String) {
        if (query.isBlank()) {
            clearSearchSuggestions()
            return
        }

        val matches = allDishesCache.filter { dish ->
            dish.name.contains(query, ignoreCase = true) ||
                dish.description.contains(query, ignoreCase = true) ||
                dish.restaurant.contains(query, ignoreCase = true) ||
                dish.getAllergenList().any { it.contains(query, ignoreCase = true) }
        }

        suggestionDishes = matches
        val displayItems = matches.map { dish ->
            "${dish.name} • ${dish.restaurant}"
        }

        searchAdapter.clear()
        searchAdapter.addAll(displayItems)
        if (displayItems.isNotEmpty()) {
            binding.searchAutoComplete.showDropDown()
        } else {
            binding.searchAutoComplete.dismissDropDown()
        }
    }

    private fun clearSearchSuggestions() {
        suggestionDishes = emptyList()
        searchAdapter.clear()
        binding.searchAutoComplete.dismissDropDown()
    }

    private fun handleSearchAction(): Boolean {
        val query = binding.searchAutoComplete.text?.toString().orEmpty()
        updateSearchSuggestions(query)
        val firstMatch = suggestionDishes.firstOrNull()
        return if (firstMatch != null) {
            navigateToDish(firstMatch)
            true
        } else {
            if (query.isNotBlank()) {
                Toast.makeText(requireContext(), R.string.search_no_results, Toast.LENGTH_SHORT)
                    .show()
            }
            hideKeyboard()
            true
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as?
            InputMethodManager
        imm?.hideSoftInputFromWindow(binding.searchAutoComplete.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
