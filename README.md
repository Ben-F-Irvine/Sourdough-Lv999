# Sourdough-Lv999

Sourdough companion app that simplifies the whole process. A personal Android app that guides each bake, builds the timeline from your own schedule templates, and stretches bulk fermentation to match your room temperature.

The full plan is in [docs/PLAN.md](docs/PLAN.md).

## Stack

- Kotlin + Jetpack Compose (Material 3), min Android 12 (API 31), target API 35
- Room (SQLite) for all data, on the phone only
- AlarmManager exact alarms for every bake timer, WorkManager for temperature readings during bulk
- Vico for charts
- No server, no Play Store: sideload from Android Studio

## Layout

```
engine/   Plain Kotlin (no Android). Timeline maths, temperature curve, TemperatureSource interface.
          Unit-tested on the JVM: ./gradlew :engine:test
app/      The Android app.
  data/db/        Room entities and database (the 9 tables from the plan)
  alarms/         AlarmManager receivers (step alarms, re-register after reboot)
  temperature/    TemperatureSource implementations (manual now; Home Assistant / BLE later)
  ui/             Compose screens and navigation
```

Rule of thumb: anything that is maths or a decision goes in `engine`; anything that touches Android goes in `app`.

## Getting started

1. Install [Android Studio](https://developer.android.com/studio) (it brings the Android SDK and a JDK).
2. Open this folder in Android Studio and let Gradle sync.
3. On the phone: enable Developer options and USB (or wireless) debugging, then Run ▶.
4. Samsung only: Settings → Battery → Background usage limits → **Never sleeping apps** → add Sourdough, and allow **Alarms and reminders**, or One UI may delay alarms.

Engine tests run without the Android SDK:

```bash
./gradlew :engine:test
```
