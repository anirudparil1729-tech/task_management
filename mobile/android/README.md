# Task Management (Android)

This directory contains the Android app scaffold (Jetpack Compose + Hilt + DataStore) plus shared core modules.

## Build / run

From the repo root:

```bash
cd mobile/android
./gradlew :app:assembleDebug
```

To create a Play Store–ready app bundle:

```bash
cd mobile/android
./gradlew :app:bundleRelease
```

## Authentication (scaffold)

- The app shows a splash screen, then a login screen.
- Login validates the static password: `Tikur@12345`.
- A successful login is persisted via `androidx.datastore` so subsequent launches go straight to the main scaffold.

## Design tokens sync (frontend → Android)

The frontend design tokens live at:

- `frontend/src/design-system/tokens/index.ts`

To export Android resources, run:

```bash
pnpm --filter frontend export-tokens:android
```

This prints XML snippets for:

- `colors.xml`
- `dimens.xml`
- `TextAppearance` styles

Update the Android resources under:

- `mobile/android/core/designsystem/src/main/res/values/colors.xml`
- `mobile/android/core/designsystem/src/main/res/values-night/colors.xml`
- `mobile/android/core/designsystem/src/main/res/values/dimens.xml`
- `mobile/android/core/designsystem/src/main/res/values/styles.xml` (TextAppearance.*)

## Module layout

- `:app` — the Android application module (Compose navigation + scaffold + bottom sheet)
- `:core:common` — shared utilities (DataStore-backed session)
- `:core:designsystem` — theme + design-token resources + Geist fonts
