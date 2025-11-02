package com.example.mealmate.utils

import com.example.mealmate.data.entities.Dish
import com.example.mealmate.data.entities.toJsonString  // <-- add this import

object SampleData {

    fun getSampleDishes(): List<Dish> {
        return listOf(
            Dish(
                name = "MW SS25 Scallop",
                description = "Roe-off scallops with tomato vinaigrette, compressed watermelon and avocado puree",
                category = "STARTER",
                allergens = listOf("Fish", "Gluten", "Mollusc", "Soy", "Sulphites").toJsonString()
            ),
            Dish(
                name = "MW SS25 Lamb Skewer",
                description = "Marinated lamb backstrap with red capsicum, onion petals, lemon herb marinade",
                category = "MAIN",
                allergens = listOf("Gluten", "Milk", "Soy").toJsonString()
            ),
            Dish(
                name = "MW SS25 Chocolate Fondant",
                description = "Warm chocolate fondant with pistachio cream, white chocolate snow, and hazelnut gelato",
                category = "DESSERT",
                allergens = listOf("Egg", "Gluten", "Milk", "Hazelnut", "Pistachio", "Soy").toJsonString()
            ),
            Dish(
                name = "MW SS25 Broccolini",
                description = "Grilled broccolini with whipped ricotta, pesto, and roasted almonds",
                category = "SIDE",
                allergens = listOf("Almond", "Egg", "Milk").toJsonString()
            ),
            Dish(
                name = "MW SS25 LUNCH Bowl - Pork",
                description = "Crispy pork belly with yellow rice, roasted cauliflower, and brussel sprout slaw",
                category = "LUNCH",
                allergens = listOf("Gluten", "Milk", "Soy", "Sulphites").toJsonString()
            ),
            Dish(
                name = "MW SS25 Barramundi",
                description = "Skin-on barramundi with lemon butter sauce, mussels, and potato",
                category = "MAIN",
                allergens = listOf("Fish", "Milk", "Mollusc", "Sulphites").toJsonString()
            ),
            Dish(
                name = "MW SS25 Pavlova",
                description = "Crisp meringue with raspberry sorbet, strawberry puree, and white chocolate chantilly",
                category = "DESSERT",
                allergens = listOf("Egg", "Gluten", "Milk").toJsonString()
            ),
            Dish(
                name = "MW SS25 Tomato & Melon Salad",
                description = "Heirloom tomatoes with compressed watermelon, stracciatella, and walnut crispy",
                category = "STARTER",
                allergens = listOf("Gluten", "Milk", "Pine nut", "Sesame", "Walnut").toJsonString()
            ),
            Dish(
                name = "MW SS25 Roasted Cauliflower",
                description = "Tandoori roasted cauliflower with coconut curry sauce and nut crumbs",
                category = "SIDE",
                allergens = listOf("Almond", "Gluten", "Milk", "Pistachio").toJsonString()
            ),
            Dish(
                name = "MW SS25 Garlic Bread",
                description = "Glazed bun with green garlic butter, parmesan, and garlic crisps",
                category = "SIDE",
                allergens = listOf("Egg", "Gluten", "Milk", "Wheat").toJsonString()
            )
        )
    }
}
