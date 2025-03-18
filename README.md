# MorseLens

![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.5.10-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.0.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

MorseLens is an Android application that allows users to translate, transmit, and detect Morse code using multiple methods including flashlight, vibration, and sound.

## Features

- **Morse Code Translation**: Convert text to Morse code and vice versa
- **Multi-language Support**: Translate between various languages including Hindi, Urdu, and English
- **Flashlight Detection**: Detect and decode Morse code signals from other devices
- **Multiple Transmission Methods**:
  - Flashlight: Use your device camera flash to transmit Morse code
  - Vibration: Feel the Morse code through device vibration
  - Sound: Hear Morse code through audio tones
- **Visual Speech Recognition**: Recognize speech with a waveform visualizer
- **Morse Code Dictionary**: Reference all Morse code characters

## Screenshots

[Place screenshots here]

## Technology Stack

- **Kotlin** and **Jetpack Compose** for modern Android UI development
- **MVVM Architecture** with state management
- **OpenCV** for image processing in flash detection
- **Google ML Kit** for language translation
- **Android Speech Recognition** for voice input
- **Hilt** for dependency injection
- **DataStore Preferences** for persistent settings

## Setup

### Prerequisites
- Android Studio Meerkat | 2024.3.1 or higher
- Minimum SDK 26 (Android 8.0 Oreo)
- Target SDK 35

### Installation
1. Clone this repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run on an emulator or physical device

## Project Structure

- **ui/morse_code_translator**: Main translator functionality
- **ui/flashlight**: Flashlight detection components
- **ui/dictionary**: Morse code dictionary
- **ui/components**: Reusable UI components
- **ui/speech**: Speech recognition and language translation
- **ui/settings**: User preferences
- **data**: Data management components

## Permissions

- Camera: Required for flash detection and transmission
- Microphone: Required for speech recognition
- Vibration: Used for tactile Morse code output

## License

[Add license information]

## Credits

Developed by [Anant Jaiswal](https://github.com/Razorquake), [Aryan Sharma](https://github.com/idAryan)
