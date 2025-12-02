# Legozia File Manager

A modern, feature-rich file manager application for Android built with Kotlin and Jetpack Compose.

## Features

- **📁 Comprehensive File Browsing**: Navigate through your device's storage with ease.
- **📊 Storage Analysis**: Visualize storage usage with category breakdowns and identify large files.
- **⚡ Quick Access**:
  - **Recent Files**: Quickly access your most recently modified files.
  - **Favorites**: Bookmark important files and folders for instant access.
- **🛠️ File Operations**:
  - Copy, Move, Delete, and Rename files and folders.
  - Create new folders.
  - **Zip/Unzip**: Compress files into archives and extract them.
  - **Share**: Share files with other apps.
- **🔍 Smart Sorting & Viewing**:
  - Sort by Name, Size, Date, or Type.
  - Switch between List and Grid view modes.
- **🎨 Modern UI**:
  - Material Design 3 implementation.
  - Dynamic Dark/Light theme support.
  - File type icons and previews.
- **ℹ️ File Details**: View detailed properties of files (path, size, modified date, type).

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Manual / ViewModelFactory
- **Asynchronous Processing**: Coroutines & Flow
- **Image Loading**: Coil
- **Navigation**: Navigation Compose
- **Permissions**: Accompanist Permissions
- **Local Storage**: DataStore Preferences (for settings)

## Project Structure

```
com.legozia.files/
├── data/               # Data sources and preferences
│   ├── FilePreferences.kt
│   └── ThemePreferences.kt
├── model/              # Data models
│   ├── FileItem.kt
│   ├── FileOperation.kt
│   └── ...
├── repo/               # Repositories and business logic
│   ├── FileRepository.kt
│   └── FileOperations.kt
├── ui/                 # UI Components (Screens & Widgets)
│   ├── components/
│   ├── theme/
│   ├── FileManagerScreen.kt
│   ├── AnalyzeStorageScreen.kt
│   └── ...
├── util/               # Utility classes
│   ├── FileIconProvider.kt
│   └── FileSizeCalculator.kt
├── viewmodel/          # ViewModels
│   ├── FileManagerViewModel.kt
│   ├── StorageViewModel.kt
│   └── ...
└── MainActivity.kt
```

## Permissions

The app requires the following permissions to function correctly:
- `READ_EXTERNAL_STORAGE` (Android 10 and below)
- `MANAGE_EXTERNAL_STORAGE` (Android 11+) - Required for full file access.

## Building the Project

1. Open the project in Android Studio.
2. Sync Gradle files.
3. Build and run on an emulator or physical device (Android 8.0+ recommended).

```bash
./gradlew build
```

## License

[MIT License](LICENSE)
