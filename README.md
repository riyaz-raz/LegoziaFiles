# File Manager - Android App

A simple and elegant file manager application for Android built with Kotlin and Jetpack Compose.

## Features

- 📁 Browse files and folders on your device
- 🔍 View file details (name, size, modified date)
- 📂 Navigate through directory structure
- 🎨 Material Design 3 UI with dynamic theming
- 🌙 Dark mode support
- 📱 Modern Jetpack Compose UI
- 🔐 Proper storage permission handling

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Material Design**: Material 3

## Project Structure

```
FileManager/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/filemanager/
│   │   │   ├── data/
│   │   │   │   └── FileRepository.kt
│   │   │   ├── model/
│   │   │   │   └── FileItem.kt
│   │   │   ├── ui/
│   │   │   │   ├── components/
│   │   │   │   │   └── FileItemRow.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   └── FileManagerScreen.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── FileManagerViewModel.kt
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Key Components

### Data Layer
- **FileItem**: Data class representing files and folders with metadata
- **FileRepository**: Handles file system operations and directory navigation

### ViewModel Layer
- **FileManagerViewModel**: Manages UI state and business logic
- Handles navigation stack for back button functionality
- Manages loading and error states

### UI Layer
- **FileManagerScreen**: Main screen with file list and navigation
- **FileItemRow**: Individual file/folder item with icon and metadata
- **Material Design 3 Theme**: Modern theming with dynamic colors

## Permissions

The app requires storage permissions to access files:
- `READ_EXTERNAL_STORAGE` (Android 10 and below)
- `MANAGE_EXTERNAL_STORAGE` (Android 11+)

## Building the Project

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on an emulator or physical device

```bash
./gradlew build
```

## Running the App

1. Grant storage permissions when prompted
2. Browse through your device's file system
3. Tap on folders to navigate into them
4. Use the back button to navigate to parent directories

## File Type Support

The app recognizes and displays appropriate icons for:
- 📁 Folders
- 🖼️ Images (jpg, png, gif, etc.)
- 🎥 Videos (mp4, avi, mkv, etc.)
- 🎵 Audio (mp3, wav, flac, etc.)
- 📄 Documents (pdf, doc, txt, etc.)
- 📊 Spreadsheets (xls, csv, etc.)
- 📽️ Presentations (ppt, etc.)
- 📦 Archives (zip, rar, etc.)
- 🤖 APK files
- 💻 Code files
- 📋 Other files

## Future Enhancements

Potential features for future versions:
- File operations (copy, move, delete, rename)
- Search functionality
- File sorting options
- Multiple view modes (list, grid)
- File preview
- Share files
- Favorites/bookmarks

## License

This project is created for educational purposes.
