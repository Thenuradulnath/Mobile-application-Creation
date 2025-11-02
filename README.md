# MealMate

MealMate is a Kotlin-based Android application for discovering restaurant dishes, capturing personal tasting experiences, and tracking allergens in one place. The app is designed for food lovers who want a simple way to search what to eat, remember what they enjoyed, and avoid ingredients they are sensitive to.

## Table of Contents
- [Overview](#overview)
- [Feature Walkthrough](#feature-walkthrough)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Setup](#setup)
- [Running the App](#running-the-app)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Overview
MealMate revolves around three core flows:
1. **Browse dishes at a glance.** The home screen highlights the current restaurant and presents its dishes in a card list with quick access to descriptions, allergen badges, and tried status.
2. **Find exactly what you crave.** A built-in ingredient search lets you filter by dish name or components (for example, searching "beef" instantly narrows the list to every beef-forward plate across the restaurant catalog).
3. **Capture personal notes and restrictions.** Manage allergen tags while editing dishes, then mark them as tried with commentary and attach photos that resurface whenever you revisit the detail view.

## Feature Walkthrough
### Landing Screen
- Restaurant name banner keeps you oriented within the current venue.
- Search icon opens an autocomplete field with live suggestions pulled from dish titles and ingredients.
- Selecting a suggestion scrolls the list to the matching card and opens the detail view for more information.

### Dish Cards
- Each card shows the dish name, restaurant, condensed description, allergen chips, and category badge so you can scan key context quickly.
- Cards respond to the search query so you always see the most relevant options and expose a tried badge or rating when you revisit favourites.

### Dish Detail Screen
- Displays extended description, allergen warnings, tasting history, and any photos captured when the dish was marked as tried.
- From here you can mark a dish as tried, update your notes, or jump to the edit screen.

### Add / Edit Dish
- Provides text inputs for name, description, restaurant, and category selection.
- Allergen management combines preset chips (e.g., Eggs, Fish, Tree Nuts) with a "Custom" field for anything unique. Chips can be toggled on/off, and the final selection persists to the database.

### Tried-It Dialog
- Lets you attach a tasting photo, rate your experience, and jot down memorable notes.
- Photos and comments are stored locally and surface on the dish detail screen during future visits.

## Architecture
- **Pattern:** MVVM (Model–View–ViewModel). UI fragments observe `LiveData` exposed by `DishViewModel`, ensuring screens stay in sync with the Room database.
- **Persistence:** Room ORM with DAO interfaces (see `data/database/DishDao.kt`) for querying dishes, allergens, and tried-history.
- **Repositories:** The `DishRepository` coordinates reads and writes, wrapping suspend functions in coroutines for smooth background execution.
- **Dependency Injection:** The `MealMateApplication` class wires repositories and data sources using a simple service locator for app-wide access.
- **Navigation:** Android Jetpack Navigation Component drives fragment transitions and back stack handling from `MainActivity`.
- **Data Seeding:** On first launch, `MealMateApplication` pre-populates the Room database with curated sample dishes so you can explore the experience immediately.

## Tech Stack
- Kotlin & Android Jetpack libraries
- Room Database with Kotlin coroutines
- ViewModel + LiveData for state management
- RecyclerView with DiffUtil adapters
- Gson for serialising allergen selections in the database
- Material Components for modern UI widgets

## Setup
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
2. **Configure Android Studio**
   - Android Studio Giraffe (2022.3.1) or newer recommended.
   - Ensure Kotlin plugin 1.9+ and Android Gradle Plugin 8.x are installed.
3. **Sync Gradle**
   - Open the project in Android Studio and allow it to download dependencies on first launch.

## Running the App
- From the toolbar select the `app` run configuration.
- Choose an emulator or connect a physical device running Android 8.0 (API 26) or later.
- Click **Run** to install and launch MealMate.

## Project Structure
```text
app/
 ├─ src/main/java/com/example/mealmate/
 │   ├─ data/            # Room database entities, DAO, and repositories
 │   ├─ ui/              # Fragments, adapters, dialogs, and view models
 │   ├─ utils/           # Helper classes (extensions, image utilities)
 │   └─ MainActivity.kt  # Hosts navigation graph
 ├─ src/main/res/        # Layout XMLs, drawables, and string resources
 └─ build.gradle.kts     # Android module build configuration
```

## Testing
- Execute the standard assemble task to confirm builds succeed:
  ```bash
  ./gradlew assembleDebug
  ```
- Unit and instrumentation tests can be added in the `src/test` and `src/androidTest` directories respectively.

## Contributing
1. Fork the repository and branch from `work`.
2. Implement your feature or bug fix, keeping Kotlin lint checks in mind.
3. Run `./gradlew assembleDebug` (and any relevant tests) to ensure the project builds.
4. Open a pull request summarizing your changes and screenshots when UI updates are involved.

## License
MealMate is released under the MIT License. See the [LICENSE](LICENSE) file for full terms.
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
