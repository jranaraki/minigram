# MiniGram

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://img.shields.io/badge/style-dark-brightgreen?style=for-the-badge">
  <source media="(prefers-color-scheme: light)" srcset="https://img.shields.io/badge/style-light-brightgreen?style=for-the-badge">
  <img alt="Style" src="https://img.shields.io/badge/style-dark-brightgreen?style=for-the-badge">
</picture>

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://img.shields.io/badge/platform-Android-success?style=for-the-badge">
  <source media="(prefers-color-scheme: light)" srcset="https://img.shields.io/badge/platform-Android-success?style=for-the-badge">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-success?style=for-the-badge">
</picture>

![MiniGram](https://img.shields.io/badge/version-1.0-blue)

A privacy-preserving Android app that performs real-time grammar correction across all apps using on-device LLM inference via ExecuTorch.

---

## Features

- **System-wide grammar correction** - Works in any app
- **On-device processing** - All processing happens locally, no data leaves the device
- **Floating overlay** - Quick access to grammar correction with a single tap
- **Privacy-first** - No internet connection required, no user data stored

---

## Architecture

```
User selects text
    ↓
AccessibilityService detects selection
    ↓
Overlay shows pen icon
    ↓
User taps icon
    ↓
KeyboardThread → GrammarService → ExecuTorch LLM
    ↓
Corrected text replaces selected portion
```

---

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** 30 (Android 11)
- **Target SDK:** 30
- **ML Framework:** ExecuTorch Android 1.1.0
- **Model:** Qwen3 0.6B (472 MB)
- **Integration:** Android AccessibilityService

---

## Installation

### Prerequisites

- Android Studio Artic Fox | Giraffe or later
- Android SDK 30+
- JDK 17
- 8GB+ RAM available on device (for model inference)
- Gradle 8+ (project uses 8.13)

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/[username]/minigram.git
cd minigram
```

2. Build the APK:
```bash
./gradlew assembleDebug
```

The APK will be available at `app/build/outputs/apk/debug/app-debug.apk` (~525 MB)

3. Install on your Android device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or transfer the APK to your device and install from file manager.

---

## Usage

### Setup (First Time Only)

1. **Enable Accessibility Service:**
   - Open Settings → Accessibility
   - Find "MiniGram"
   - Enable the service

2. **Grant Overlay Permission:**
   - Follow the on-screen prompts to grant SYSTEM_ALERT_WINDOW permission

3. **Select text in any app:**
   - Long-press to select text
   - MiniGram overlay icon appears

4. **Tap the overlay:**
   - Model processes text locally
   - Corrected text replaces selected text automatically

---

## Troubleshooting

### App crashes after selection

The app relies on accessibility events. Some apps may trigger edge cases with selection indices. If crashes occur, restart the app and try selecting text again.

### Grammar corrections not appearing

- Check that AccessibilityService is enabled in Settings → Accessibility
- Verify overlay permission is granted
- Check logs for "MiniGram" tag to see processing errors

### Model not responding

The first inference takes longer as Executors loads the model:
- Wait 30-60 seconds after first tap
- Subsequent taps will be faster (~3-5 seconds)

---

## Development

### Project Structure

```
minigram/
├── app/
│   └── src/main/
│       ├── java/com/minigram/
│       │   ├── GrammarAccessibilityService.kt
│       │   ├── GrammarService.kt
│       │   ├── OverlayView.kt
│       │   ├── TextReplacer.kt
│       │   └── MainActivity.kt
│       ├── res/
│       │   ├── assets/models/
│       │   │   ├── qwen3_0.6B_model.pte (472 MB)
│       │   │   ├── tokenizer.json (11 MB)
│       │   │   └── vocab.json (2.7 MB)
│       │   ├── mipmap-*/ic_launcher.png
│       │   └── xml/accessibility_service_config.xml
│       └── AndroidManifest.xml
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Key Components

- **GrammarAccessibilityService:** Monitors text selection events
- **GrammarService:** Manages LLM model and handles inference
- **OverlayView:** Floating UI with pen/rotate icon for busy state
- **TextReplacer:** Replaces selected text with corrected text

### Building

- Debug: `./gradlew assembleDebug`
- Clean build: `./gradlew clean && ./gradlew assembleDebug`

---

## Privacy & Data

- **All processing happens on device**
- No data leaves the device
- No analytics or telemetry
- No cloud APIs or services

---

## License

[MIT License](LICENSE)

---

## Contributing

Pull requests and issues are welcome!

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test thoroughly on Android 11+ devices
5. Submit a PR with clear description

### Code Style

- 100-character max line width
- Minimal, clean code
- No unnecessary comments

---

## Acknowledgments

- [ExecuTorch](https://github.com/pytorch/executorch) - On-device ML inference framework
- [Qwen3](https://github.com/Qwen/Qwen) - Lightweight language model for grammar correction