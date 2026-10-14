# JavAqua

JavAqua is a comprehensive Android application designed for aquarium enthusiasts. It provides tools for managing personal aquariums, exploring a detailed lexicon of fish species, and participating in a community forum.

## Features

### 🌊 My Aquariums
- **Management**: Add and manage multiple aquariums with details like volume, temperature, and background.
- **Fish Population**: Track which fish species are currently in each of your tanks.
- **Offline Support**: Uses Room database for local caching and synchronization with Firebase Firestore.
- **Animated Previews**: Visual representation of your aquarium with fish sprites.

### 📚 Fish Atlas
- **Lexicon**: A searchable database of fish species with scientific names and care requirements.
- **Deep Details**: View detailed descriptions, compatibility info, and community posts mentioning specific fish.
- **Fast Search**: Optimized local search through cached data.

### 💬 Community Forum
- **Engagement**: Share experiences, ask questions, and interact with other aquarium keepers.
- **Interactivity**: Support for posts, nested comments, and a voting system (upvotes/downvotes).
- **Fish Tagging**: Mention fish from the Atlas using `@` tags to automatically link to their details.

### ⚙️ Personalization
- **User Profiles**: Secure authentication via Firebase Auth.
- **Themes**: Full support for Light and Dark modes with Material 3 design.
- **Settings**: Manage credentials and app preferences easily.

## Tech Stack

- **Language**: Java
- **UI Toolkit**: Material 3, XML Layouts, ViewBinding
- **Database**: 
  - **Local**: Room Persistence Library
  - **Remote**: Firebase Firestore
- **Authentication**: Firebase Authentication (with FirebaseUI)
- **Image Loading**: Glide
- **Architecture**: MVVM (Model-View-ViewModel) with Repository pattern

## Firebase Data Structure

The app uses the following Firestore collections:

- `users`: Stores user profile information (username, email).
- `aquariums`: Stores user-created aquarium data and population lists.
- `atlas_previews`: High-level summary data for the Fish Atlas used for listing and caching.
- `atlas`: Full detail documents for each fish species.
- `posts`: Community forum posts, including metadata like upvotes and tagged fish.
  - `comments`: (Sub-collection) Stores replies to specific posts.

## Getting Started

1. **Clone the repository**: `git clone https://github.com/yourusername/javaqua.git`
2. **Firebase Setup**:
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.javaqua`.
   - Download the `google-services.json` file and place it in the `app/` directory.
   - Enable Email/Password authentication and Firestore Database.
3. **Build and Run**: Open the project in Android Studio and deploy to an emulator or physical device.

---
Developed as a modern solution for aquarium management and community engagement.
