# playfit-android

**Public portfolio project.** Native Android showcase for the [Playfit](https://github.com/carloseav15/playfit) product experience.

This repo is a subordinate/portfolio implementation of the Playfit mobile product vision. The main project ecosystem lives at [github.com/carloseav15/playfit](https://github.com/carloseav15/playfit).

The project starts as a Kotlin + Jetpack Compose app with mock data and a fully wired local-first data layer (Room, Hilt, WorkManager). The first goal is to match the iOS SwiftUI scaffold, then prove the native Android flow works before adding auth and full API sync.

### Route mapping

| Web (product) | Android (Compose NavHost) |
|---|---|
| `/` (Play Next) | `play-next` |
| `/game/[gameId]` | `game/{gameId}` |
| `/picks` | `picks` |
| `/taste` | `taste` |
| `/settings` | `settings` |

## Scope

The Android app should reinterpret the mobile Playfit product flow from the web app:

- `Play Next`: one strong recommendation plus a short queue.
- `Game Detail`: cover, metadata, match score, reasons, and action buttons.
- `Picks`: saved recommendations.
- `Taste`: taste profile and signal confidence.
- `Settings`: auth, sync, and preference controls.

This is not a full web port. The target is a native Android showcase that feels current on Android platforms.

## Current Stack

- Kotlin.
- Jetpack Compose with Material 3.
- Hilt for dependency injection.
- Room for local cache.
- Navigation Compose.
- ViewModel + StateFlow architecture.
- DataStore for preferences.
- WorkManager for deferred sync.
- Mock repository for the first UI pass (ready for API swap).
- Supabase Kotlin SDK wired for auth and PostgREST.
- Ktor/OkHttp + Retrofit for network.
- Kotlinx Serialization.
- Coil for async image loading.
- Unit tests with MockK, Turbine, and Coroutines Test.

Build pinned to versions cached on this machine for offline compilation:
- Android SDK 36, AGP 9.0.0, Kotlin 2.2.20.

## Intended 2026 Direction

- Material 3 Expressive polish where it improves clarity.
- Adaptive UI for phones, tablets, and foldables.
- Upgrade to the current 2026 AndroidX/Compose stack once dependency download is intentionally allowed.
- Real API client replacing mock repository.
- SwiftData-style sync via WorkManager + Room.
- Full Supabase Auth integration (email, OAuth, anonymous → authenticated migration).
- Screenshots and release artifacts for portfolio.

## Folder Layout

```
android-compose/
  app/
    src/
      main/java/com/carlosarancibia/playfit/
        di/             — Hilt modules (AppModule, NetworkModule, RepositoryModule, IdentityModule)
        model/          — Domain models + product rules (transitions, taste, onboarding)
        data/           — Repository interface + impl, mock data
        ui/
          theme/        — Material 3 theme
          components/   — Reusable composables (GlassCard, ScoreBadge, Shimmer, etc.)
          screens/      — Feature screens (PlayNext, GameDossier, Picks, Taste, Settings, etc.)
          viewmodel/    — ViewModels + ProductUtils
    src/test/           — Unit tests (repository, model, ViewModel, auth, screens)
  docs/
    architecture.md
    play-route-mapping.md
```

## Terminal Validation

```sh
./gradlew :app:assembleDebug
```

If dependencies are not already cached, Android Studio can resolve them when the project opens.

## Open in Android Studio

Open:

```
/Users/carancibia/Projects/playfit/android-compose
```

Package:

```
com.carlosarancibia.playfit.android
```

The current launcher icon is a placeholder vector. Replace it before portfolio screenshots or Play Store distribution.

## Next Milestones

1. Connect mock repository to real Playfit API endpoints.
2. Add Supabase Auth with anonymous → authenticated migration flow.
3. Validate Room cache + WorkManager sync for offline availability.
4. Write Compose UI tests for core screens.
5. Replace placeholder icon, add portfolio screenshots, and publish release APK.
