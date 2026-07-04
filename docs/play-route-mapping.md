# `/play` to Android Mapping

Source web routes:

```text
/play
/play/game/[gameId]
/play/picks
/play/taste
/play/settings
```

Source files:

```text
/Users/carancibia/Projects/playfit/product/apps/web/src/app/play
/Users/carancibia/Projects/playfit/product/apps/web/src/components/playfit-mvp
```

Native Android destinations:

| Web route | Android screen | Kotlin file |
| --- | --- | --- |
| `/play` | Play Next | `TodayScreen.kt` |
| `/play/game/[gameId]` | Game Detail | `GameDetailScreen.kt` |
| `/play/picks` | Picks | `PicksScreen.kt` |
| `/play/taste` | Taste | `TasteScreen.kt` |
| `/play/settings` | Settings | `SettingsScreen.kt` |

## Product Contract

The Android app should prove:

- A user can see one primary recommendation.
- A user can inspect why it fits.
- A user can save, skip, or mark a game as played.
- A user can open saved picks.
- A user can understand their taste profile.
- The app is useful with cached data.

## Non-goals

- Do not port the full web app.
- Do not use a web view.
- Do not duplicate backend rules that belong in `playfit/product`.
- Do not start with auth complexity before the native flow feels strong.
