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

## Firebase Setup (Required for Push Notifications)

The app integrates Firebase Cloud Messaging (FCM) for push notifications, including task reminders, daily summaries, and focus-mode nudges.

### Prerequisites

1. **Create a Firebase Project:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use an existing one
   - Add an Android app with package name: `com.ctonew.taskmanagement`

2. **Download google-services.json:**
   - In the Firebase Console, go to Project Settings → Your Apps → Android App
   - Download the `google-services.json` file
   - Place it at: `mobile/android/app/google-services.json`
   - **Important:** This file is git-ignored and must be added manually

3. **Configure Server Key:**
   - In Firebase Console, go to Project Settings → Cloud Messaging
   - Copy the "Server key" or generate a new one
   - Provide this key to your backend server for sending push notifications
   - The backend will use this key to send FCM messages to devices

### Notification Types

The app handles the following FCM message types (specified in the `type` data field):

- **`reminder`** — Task reminders with quick actions (complete, snooze)
  - Data fields: `task_id`, `task_title`, `task_description`
- **`daily_summary`** — Daily productivity summaries
  - Data fields: `completed_count`, `pending_count`, `overdue_count`
- **`focus_nudge`** — Focus mode nudges
  - Data fields: `message`
- **`overdue`** — Overdue task alerts
  - Data fields: `task_id`, `task_title`, `days_overdue`

### Local Notification Scheduling

The app also schedules local notifications using WorkManager and AlarmManager:

- **Periodic Reminder Sync:** Runs every 15 minutes to check upcoming tasks and schedule reminders
- **Daily Summary:** Runs once per day to aggregate productivity stats
- **Repeated Reminders:** Continues notifying for incomplete tasks past due time
- **Focus Timer Completion:** Foreground service notification when focus session completes

### Testing Without Firebase

If you don't have `google-services.json`, the app will still compile and run, but FCM functionality will be disabled. Local notifications via WorkManager will continue to work.

To build without Firebase:
```bash
# Comment out the google-services plugin in app/build.gradle.kts
# plugins {
#   alias(libs.plugins.google.services)
# }
```

### Backend API Integration

The app polls the backend endpoint `/api/notifications/next-reminder` periodically to sync server-side reminder schedules. The backend should implement this endpoint to return upcoming reminders for the authenticated user.

Example response format:
```json
{
  "reminders": [
    {
      "task_id": "123",
      "task_title": "Complete report",
      "reminder_time": "2024-01-15T14:30:00Z"
    }
  ]
}
```
