# Contributing to Lipi Keyboard

Thanks for helping! This project is a privacy-first keyboard, so a few rules are non-negotiable.

## Ground rules
- **Never log, store or transmit typed text.** No exceptions, including in debug logs and crash reports.
- Do not add the `INTERNET` permission to the base build.
- Disable suggestions and learning in password fields.
- Only add fonts, word lists or code whose license allows redistribution, and keep their notices.

## Getting set up
1. Install the latest stable Android Studio.
2. Fork and clone the repo, then run `./gradlew assembleDebug`.
3. Install on a **real device**. Emulators handle keyboards poorly.

## Making a change
1. Open an issue first for anything bigger than a small fix.
2. Create a branch: `feature/short-name` or `fix/short-name`.
3. Keep changes focused. One feature or fix per pull request.
4. Add or update unit tests (layout counts, transliteration rules, suggestion logic).
5. Run `./gradlew lint test` before opening the pull request.

## Commit messages
Use short prefixes: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.

## Layout and language contributions
Native typists are the most valuable contributors. When proposing a layout change, include:
- the language and script,
- which keys move and why (frequency, ergonomics, convention),
- a screenshot from a real device.

## Code style
Kotlin official style. No allocations inside `onDraw` or touch handlers. Keep UI work on the main thread and heavy work (suggestions) on background coroutines.

## Reporting security or privacy issues
Please do not open a public issue. Contact the maintainer privately through the email on their GitHub profile.
