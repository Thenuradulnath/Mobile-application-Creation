# MealMate

MealMate is an Android app for exploring dishes from local restaurants, capturing personal tasting notes, and managing dietary needs. The app helps diners quickly find meals that match their preferences, keep track of what they have tried, and remember any allergens to avoid.

## Key Features

- **Restaurant discovery landing page** – The home screen highlights the current restaurant and lists its dishes in a card-based layout so you can browse at a glance.
- **Ingredient-aware search** – Use the search bar to look up dishes by name or ingredient. Type "beef", "pasta", or any keyword and the app filters the list in real time, letting you jump straight to the dish detail you need.
- **Dish details & tasting notes** – Each dish screen surfaces restaurant information, tasting notes, and preparation details to help you decide what to order.
- **Allergen tracking when adding dishes** – When creating or editing a dish, select from preset allergen chips (e.g., fish, eggs, nuts) or add your own custom allergen tags so sensitive ingredients are always visible.
- **Tried-it workflow with photos** – Mark a dish as tried, add optional tasting comments, and attach photos from your experience. Attached images persist in the dish detail view.

## Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Mobile-application-Creation
   ```
2. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```
3. **Run on a device or emulator**
   Open the project in Android Studio Arctic Fox or newer, then deploy the `app` module to an Android 8.0+ device/emulator.

## Project Structure

```
app/
 ├─ src/main/java/com/example/mealmate/   # Kotlin source
 ├─ src/main/res/                        # Layouts, drawables, strings
 ├─ build.gradle.kts                     # Module Gradle script
```

## Requirements

- Android Studio Arctic Fox (2020.3.1) or newer
- Android Gradle Plugin 8.x
- Minimum SDK 24 (Android 7.0)
- Kotlin 1.9+

## Contributing

1. Fork the repository and create a feature branch from `work`.
2. Make your changes and ensure `./gradlew assembleDebug` succeeds.
3. Submit a pull request describing your updates.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
