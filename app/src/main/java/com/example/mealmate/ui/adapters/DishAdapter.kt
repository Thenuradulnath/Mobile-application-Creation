package com.example.mealmate.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate.R
import com.example.mealmate.data.entities.Dish
import com.example.mealmate.databinding.ItemDishBinding

class DishAdapter(
    private val onDishClick: (Dish) -> Unit,
    private val onDishLongClick: (Dish) -> Unit
) : ListAdapter<Dish, DishAdapter.DishViewHolder>(DishDiffCallback) {

    inner class DishViewHolder(private val binding: ItemDishBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: Dish) {
            binding.dishName.text = dish.name
            binding.dishDescription.text = dish.description
            binding.restaurantName.text = dish.restaurant

            // Set category badge
            binding.categoryBadge.text = when(dish.category) {
                "STARTER" -> "STARTER"
                "MAIN" -> "MAIN"
                "DESSERT" -> "DESSERT"
                "SIDE" -> "SIDE"
                "LUNCH" -> "LUNCH"
                else -> "OTHER"
            }

            // Set icon based on category
            binding.dishIcon.setImageResource(when(dish.category) {
                "STARTER" -> R.drawable.starters
                "MAIN" -> R.drawable.main
                "DESSERT" -> R.drawable.dessert
                "SIDE" -> R.drawable.side
                "LUNCH" -> R.drawable.lunch
                else -> R.drawable.main
            })

            // Setup allergens
            setupAllergens(dish.getAllergenList())

            // Setup status and rating
            if (dish.isTried) {
                binding.statusIndicator.text = "ALREADY TRIED"
                binding.statusIndicator.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.green)
                )
                binding.dishRating.visibility = android.view.View.VISIBLE
                binding.dishRating.rating = dish.rating
            } else {
                binding.statusIndicator.text = "GOING TO TRY"
                binding.statusIndicator.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.blue)
                )
                binding.dishRating.visibility = android.view.View.GONE
            }

            // Click listeners
            binding.root.setOnClickListener { onDishClick(dish) }
            binding.root.setOnLongClickListener {
                onDishLongClick(dish)
                true
            }
        }

        private fun setupAllergens(allergens: List<String>) {
            binding.allergenLayout.removeAllViews()

            allergens.forEach { allergen ->
                val imageView = android.widget.ImageView(binding.root.context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
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
            return when(allergen) {
                "Gluten", "Wheat" -> R.drawable.gluten
                "Milk" -> R.drawable.dairy
                "Egg" -> R.drawable.egg
                "Fish" -> R.drawable.fish
                "Soy" -> R.drawable.soy
                "Nuts", "Almond", "Pistachio", "Hazelnut", "Walnut" -> R.drawable.nuts
                else -> R.drawable.allergen_default
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val binding = ItemDishBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object DishDiffCallback : DiffUtil.ItemCallback<Dish>() {
    override fun areItemsTheSame(oldItem: Dish, newItem: Dish): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Dish, newItem: Dish): Boolean {
        return oldItem == newItem
    }
}