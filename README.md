# playfit-android

**Public portfolio project.** Native Android showcase for the [Playfit](https://github.com/carloseav15/playfit) product experience.

Playfit recommends games based on what you actually like, not what's popular. This repo is the
native Android showcase of that product; full product context lives at
[github.com/carloseav15/playfit](https://github.com/carloseav15/playfit).

This repo is a subordinate/portfolio implementation of the Playfit mobile product vision.

The app is Kotlin + Jetpack Compose with a fully wired local-first data layer (Room, Hilt, WorkManager) backed by the real Playfit API and Supabase Auth/PostgREST — no mock data or placeholder repository left.

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
- Real repository (`PlayfitRepositoryImpl`) wired against the Playfit API and Supabase.
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
- Screenshots and release artifacts for portfolio.

## Folder Layout

```
android-compose/
  app/
    src/
      main/java/com/carlosarancibia/playfit/
        di/             — Hilt modules (AppModule, NetworkModule, RepositoryModule, IdentityModule)
        model/          — Domain models + product rules (transitions, taste, onboarding)
        data/           — Repository interface + impl (Supabase/Retrofit-backed), local cache
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

Real API integration, Supabase Auth (anonymous → authenticated), Room/WorkManager offline sync,
and Compose UI test coverage are all done. What's left before Play Store distribution — signing
config, launcher icon, store listing assets — is tracked in
[tasks/android.md](../tasks/android.md).
