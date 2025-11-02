package com.example.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mealmate.R
import com.example.mealmate.data.entities.Dish
import com.example.mealmate.databinding.FragmentDishDetailBinding
import com.example.mealmate.ui.viewmodels.DishViewModel

class DishDetailFragment : Fragment() {

    private var _binding: FragmentDishDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DishViewModel by viewModels()

    private var dishId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Extract dishId from arguments; 0 indicates a new or invalid dish
        dishId = arguments?.getLong("dishId", 0L) ?: 0L
        if (dishId != 0L) {
            viewModel.getDishById(dishId)
        }
        observeDish()
        setupClickListeners()
    }

    private fun observeDish() {
        viewModel.selectedDish.observe(viewLifecycleOwner) { dish ->
            dish?.let { displayDish(it) }
        }
    }

    private fun displayDish(dish: Dish) {
        binding.dishName.text = dish.name
        binding.restaurantName.text = dish.restaurant
        binding.dishDescription.text = dish.description

        // Set category badge text
        binding.categoryBadge.text = when (dish.category) {
            "STARTER" -> "STARTER"
            "MAIN"    -> "MAIN"
            "DESSERT" -> "DESSERT"
            "SIDE"    -> "SIDE"
            "LUNCH"   -> "LUNCH"
            else      -> "OTHER"
        }

        // Set icon based on category
        binding.dishIcon.setImageResource(
            when (dish.category) {
                "STARTER" -> R.drawable.starters
                "MAIN"    -> R.drawable.main
                "DESSERT" -> R.drawable.dessert
                "SIDE"    -> R.drawable.side
                "LUNCH"   -> R.drawable.lunch
                else      -> R.drawable.main
            }
        )

        setupAllergens(dish.getAllergenList())

        if (dish.isTried) {
            binding.statusIndicator.text = "TRIED"
            binding.statusIndicator.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.green)
            )
            binding.tastedSection.visibility = View.VISIBLE
            binding.detailRating.rating = dish.rating
            binding.tastingNotes.text =
                if (dish.tastingNotes.isEmpty()) "No tasting notes added." else dish.tastingNotes
            binding.btnMarkTried.text = "Update Tasting Notes"
        } else {
            binding.statusIndicator.text = "TO TRY"
            binding.statusIndicator.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.blue)
            )
            binding.tastedSection.visibility = View.GONE
            binding.btnMarkTried.text = "Mark as Tried"
        }
    }

    private fun setupAllergens(allergens: List<String>) {
        binding.allergenLayout.removeAllViews()
        allergens.forEach { allergen ->
            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.allergen_icon_size),
                    resources.getDimensionPixelSize(R.dimen.allergen_icon_size)
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.allergen_icon_margin)
                }
                setImageResource(getAllergenIcon(allergen))
                contentDescription = allergen
            }
            binding.allergenLayout.addView(imageView)
        }
    }

    private fun getAllergenIcon(allergen: String): Int {
        return when (allergen) {
            "Gluten", "Wheat"                           -> R.drawable.gluten
            "Milk"                                      -> R.drawable.dairy
            "Egg"                                       -> R.drawable.egg
            "Fish"                                      -> R.drawable.fish
            "Soy"                                       -> R.drawable.soy
            "Nuts", "Almond", "Pistachio",
            "Hazelnut", "Walnut"                        -> R.drawable.nuts
            else                                        -> R.drawable.allergen_default
        }
    }

    private fun setupClickListeners() {
        binding.btnMarkTried.setOnClickListener {
            viewModel.selectedDish.value?.let { dish ->
                showMarkTriedDialog(dish)
            }
        }

        binding.btnEdit.setOnClickListener {
            viewModel.selectedDish.value?.let { dish ->
                val bundle = bundleOf("dishId" to dish.id)
                findNavController().navigate(
                    R.id.action_dishDetailFragment_to_addEditDishFragment,
                    bundle
                )
            }
        }

        binding.btnDelete.setOnClickListener {
            viewModel.selectedDish.value?.let { dish ->
                showDeleteDialog(dish)
            }
        }
    }

    private fun showMarkTriedDialog(dish: Dish) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_mark_tried, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val notesEditText = dialogView.findViewById<EditText>(R.id.notesEditText)

        // Prepopulate existing rating and notes if editing a tried dish
        ratingBar.rating = dish.rating
        notesEditText.setText(dish.tastingNotes)

        AlertDialog.Builder(requireContext())
            .setTitle(if (dish.isTried) "Update Tasting Notes" else "Mark as Tried")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val rating = ratingBar.rating
                val notes = notesEditText.text.toString().trim()
                viewModel.markAsTried(dish.id, rating, notes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(dish: Dish) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Dish")
            .setMessage("Are you sure you want to delete ${dish.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteDish(dish)
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.clearSelectedDish()
    }
}
