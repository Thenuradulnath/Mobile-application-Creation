package com.example.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
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
import com.google.android.material.chip.Chip
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class DishListFragment : Fragment() {

    private var _binding: FragmentDishListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DishViewModel by viewModels()
    private lateinit var adapter: DishAdapter

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
        setupTryFilter()
        setupFilterChips()
        setupFAB()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = DishAdapter(
            onDishClick = { dish ->
                val bundle = bundleOf("dishId" to dish.id)
                findNavController().navigate(
                    R.id.action_dishListFragment_to_dishDetailFragment,
                    bundle
                )
            },
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
