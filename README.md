# Lipi Keyboard

A fast, private Android keyboard for Indic scripts, starting with **Warang Chiti** (Warang Citi, the script of the Ho language) and **Odia**. It works fully offline and never collects what you type.

## Features
- Warang Chiti layout: 32 letters, small and capital forms (Shift), Warang Chiti digits, Om symbol
- Odia and English layouts
- Language switch key, emoji panel, settings
- Dark theme and touch-friendly key sizing
- No ads, no tracking, no network permission

> Planned: Odia transliteration, suggestions and autocorrect, clipboard history, themes, voice typing.

## Screenshots
| Warang Chiti | Symbols |
|---|---|
| ![Warang Chiti layout](docs/screenshots/warang-chiti.png) | ![Symbols layout](docs/screenshots/symbols.png) |

## Getting started
1. Clone the repo and open it in Android Studio.
2. Build and install on a device (Android 7.0+, API 24).
3. Open the app, tap **Enable keyboard**, then **Select keyboard**.
4. Use the "Type here to test" field to try it.

```bash
git clone https://github.com/<your-username>/lipi-keyboard.git
cd lipi-keyboard
./gradlew assembleDebug
```

## Privacy
The keyboard does not log, store remotely or transmit typed text. The base build has no `INTERNET` permission. Suggestions and learning are disabled in password fields.

## Tech stack
Kotlin, `InputMethodService`, custom `View` rendering, Jetpack Compose (settings), Room, DataStore.

## Project structure
```
app/src/main/java/.../
  ime/        IME service, InputController
  keyboard/   KeyboardView, key rendering, touch handling
  layout/     WarangChitiLayout and other layouts
  settings/   Settings and onboarding screens
```

## Roadmap
- [x] Warang Chiti layout
- [ ] Odia transliteration engine
- [ ] Suggestions and personal dictionary
- [ ] Clipboard history and themes
- [ ] More Indic languages (Santali Ol Chiki, Hindi, Bengali)

## Contributing
Issues and pull requests are welcome, especially layout feedback from native Warang Chiti and Odia typists. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Acknowledgements
Noto Sans Warang Citi (SIL Open Font License) and the Unicode Warang Citi block.

## License
[MIT](LICENSE)
