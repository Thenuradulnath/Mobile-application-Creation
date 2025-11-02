package com.example.mealmate.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.material.button.MaterialButton

class DishDetailFragment : Fragment() {

    private var _binding: FragmentDishDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DishViewModel by viewModels()

    private var dishId: Long = 0L
    private var pendingPhotoUri: Uri? = null
    private var pendingPhotoPreview: ImageView? = null
    private var pendingRemovePhotoButton: MaterialButton? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                pendingPhotoUri = it
                updateDialogPhotoPreview()
            }
        }

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
            if (dish.photoPath.isNullOrBlank()) {
                binding.tastingPhoto.visibility = View.GONE
                binding.tastingPhoto.setImageDrawable(null)
            } else {
                binding.tastingPhoto.visibility = View.VISIBLE
                binding.tastingPhoto.setImageURI(Uri.parse(dish.photoPath))
            }
        } else {
            binding.statusIndicator.text = "TO TRY"
            binding.statusIndicator.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.blue)
            )
            binding.tastedSection.visibility = View.GONE
            binding.btnMarkTried.text = "Mark as Tried"
            binding.tastingPhoto.visibility = View.GONE
            binding.tastingPhoto.setImageDrawable(null)
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
        val addPhotoButton = dialogView.findViewById<MaterialButton>(R.id.btnAddPhoto)
        val removePhotoButton = dialogView.findViewById<MaterialButton>(R.id.btnRemovePhoto)
        val photoPreview = dialogView.findViewById<ImageView>(R.id.photoPreview)

        // Prepopulate existing rating and notes if editing a tried dish
        ratingBar.rating = dish.rating
        notesEditText.setText(dish.tastingNotes)

        pendingPhotoPreview = photoPreview
        pendingRemovePhotoButton = removePhotoButton
        pendingPhotoUri = dish.photoPath?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
        updateDialogPhotoPreview()

        addPhotoButton.setOnClickListener {
            pendingPhotoPreview = photoPreview
            pendingRemovePhotoButton = removePhotoButton
            pickImageLauncher.launch("image/*")
        }
        removePhotoButton.setOnClickListener {
            pendingPhotoUri = null
            updateDialogPhotoPreview()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (dish.isTried) "Update Tasting Notes" else "Mark as Tried")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val rating = ratingBar.rating
                val notes = notesEditText.text.toString().trim()
                viewModel.markAsTried(dish.id, rating, notes, pendingPhotoUri?.toString())
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnDismissListener {
            pendingPhotoPreview = null
            pendingRemovePhotoButton = null
            pendingPhotoUri = null
        }

        dialog.show()
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
        pendingPhotoPreview = null
        pendingRemovePhotoButton = null
        pendingPhotoUri = null
    }

    private fun updateDialogPhotoPreview() {
        val preview = pendingPhotoPreview ?: return
        val removeButton = pendingRemovePhotoButton

        val uri = pendingPhotoUri
        if (uri != null) {
            preview.visibility = View.VISIBLE
            preview.setImageURI(uri)
            removeButton?.visibility = View.VISIBLE
        } else {
            preview.setImageDrawable(null)
            preview.visibility = View.GONE
            removeButton?.visibility = View.GONE
        }
    }
}
