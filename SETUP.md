# Quick Setup Guide

## Prerequisites

Before you begin, ensure you have:
- **Android Studio** (Hedgehog 2023.1.1 or newer)
- **JDK 17** or higher
- **Android SDK** with API 34 installed

## Setup Instructions

### 1. Open the Project

1. Launch Android Studio
2. Click "Open" or "File" → "Open"
3. Navigate to: `/Users/datacube/Documents/Files/Experiments/Android/FileManager`
4. Click "OK"

### 2. Sync Gradle

Android Studio will automatically detect the Gradle files and prompt you to sync. If not:
1. Click "File" → "Sync Project with Gradle Files"
2. Wait for the sync to complete (this may take a few minutes on first run)

### 3. Install Gradle Wrapper (if needed)

If you see Gradle wrapper errors, run this in the terminal:

```bash
cd /Users/datacube/Documents/Files/Experiments/Android/FileManager
gradle wrapper --gradle-version 8.2
```

Or if you don't have Gradle installed globally, Android Studio will handle it automatically.

### 4. Build the Project

**Option A: Using Android Studio**
- Click "Build" → "Make Project" (or press `Cmd+F9`)

**Option B: Using Terminal**
```bash
cd /Users/datacube/Documents/Files/Experiments/Android/FileManager
./gradlew build
```

### 5. Run the App

**On an Emulator:**
1. Click "Tools" → "Device Manager"
2. Create a new virtual device (if you don't have one)
   - Recommended: Pixel 6 with API 34
3. Click the "Run" button (green play icon) or press `Ctrl+R`
4. Select your emulator

**On a Physical Device:**
1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Go to Settings → Developer Options
   - Turn on "USB Debugging"
3. Connect your device via USB
4. Click the "Run" button and select your device

### 6. Grant Permissions

When the app launches for the first time:

**Android 10 and below:**
- Tap "Grant Permission" when prompted
- Allow storage access

**Android 11 and above:**
- Tap "Grant Permission"
- You'll be taken to system settings
- Find "File Manager" in the list
- Enable "Allow access to manage all files"
- Return to the app

### 7. Start Browsing

- The app will load your device's external storage directory
- Tap on folders to navigate into them
- Use the back arrow in the top bar to go to parent directories
- View file information (size, date) for each item

## Troubleshooting

### Gradle Sync Failed
- Ensure you have a stable internet connection
- Check that JDK 17 is installed and configured in Android Studio
- Try "File" → "Invalidate Caches" → "Invalidate and Restart"

### Build Errors
- Clean the project: "Build" → "Clean Project"
- Rebuild: "Build" → "Rebuild Project"
- Check that all dependencies downloaded successfully

### App Crashes on Launch
- Check logcat for error messages
- Ensure your device/emulator is running Android 7.0 (API 24) or higher
- Verify storage permissions are granted

### No Files Showing
- Ensure storage permissions are granted
- Check that your device has files in the external storage directory
- Try navigating to a different folder

## Project Structure Overview

```
FileManager/
├── app/
│   ├── build.gradle.kts          # App dependencies
│   └── src/main/
│       ├── AndroidManifest.xml   # App configuration
│       ├── java/                 # Kotlin source code
│       └── res/                  # Resources (strings, themes, icons)
├── build.gradle.kts              # Project-level config
├── settings.gradle.kts           # Gradle settings
└── README.md                     # Documentation
```

## Next Steps

Once the app is running successfully:

1. **Explore the Code**
   - Check out the MVVM architecture
   - Review the Jetpack Compose UI components
   - Understand the permission handling flow

2. **Customize**
   - Change the app name in `res/values/strings.xml`
   - Modify the theme colors in `ui/theme/Color.kt`
   - Add new features (see README.md for ideas)

3. **Test**
   - Try on different Android versions
   - Test with various file types
   - Verify dark mode works correctly

## Additional Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Android Permissions Guide](https://developer.android.com/training/permissions)
- [MVVM Architecture](https://developer.android.com/topic/architecture)

## Support

If you encounter any issues:
1. Check the logcat output in Android Studio
2. Review the walkthrough.md for detailed implementation info
3. Verify all prerequisites are met
4. Try cleaning and rebuilding the project

Happy coding! 🚀
