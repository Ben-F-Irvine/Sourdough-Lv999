# Sourdough Companion

Personal Android app (Kotlin + Compose). Plan and requirements: `docs/PLAN.md`. Build phases there are the roadmap; finish one before starting the next.

## Architecture rules

- `engine/` is pure Kotlin with no Android imports. Scheduling, temperature curve and bulk progress maths live there and are unit-tested (`./gradlew :engine:test`).
- Step times are offsets from the previous step, never stored clock times. `planned_at` on `bake_steps` is a derived cache that gets recomputed on every re-plan.
- Timers are AlarmManager exact alarms (`setAlarmClock`, which Doze never delays), never in-app countdowns. Re-planning = cancel affected alarms, set new ones; only `BakeRepository` does this. Temperature re-plans ignore changes under 10 minutes.
- Fixed steps finish on their own when their time is up; wait-for-user steps block the chain, so no alarms are set past the first unfinished one.
- A repeating step's `durationMin` is the whole step; repeats fire at start + k × interval.
- A bake copies its template's steps into `bake_steps` at start; never read the template mid-bake.
- Sensors go behind `TemperatureSource`; scheduling code must not know which source is in use.
- Manual DI via `AppContainer`; no Hilt/Koin.
- Room schema is exported to `app/schemas/`; bump the DB version with a migration once real bakes are stored.

## Local build notes (this machine)

- Android SDK is at `Z:\AndroidSDK` (in `local.properties`), Android Studio at `Z:\AndroidStudio`.
- Run Gradle with the system JDK 23. Studio's bundled JBR is Java 25, which Gradle 8.12 can't run on.
- Build and check: `./gradlew :app:assembleDebug :app:lintDebug :engine:test`.
- If Gradle fails with "Unable to establish loopback connection", set `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=<short path>`.
