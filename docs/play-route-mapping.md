# Web to Android Route Mapping

Source web routes (all at root level — `(play)` route group, no `/play` prefix):

```
/
/game/[gameId]
/picks
/taste
/settings
```

Source files:

```
/Users/carancibia/Projects/playfit/product/apps/web/src/app/(play)
/Users/carancibia/Projects/playfit/product/apps/web/src/components/playfit-mvp
```

Native Android destinations:

| Web route | Android screen | Kotlin file |
| --- | --- | --- |
| `/` | Play Next | `PlayNextScreen.kt` |
| `/game/[gameId]` | Game Dossier | `GameDossierScreen.kt` |
| `/picks` | Picks | `PicksScreen.kt` |
| `/taste` | Taste | `TasteScreen.kt` |
| `/settings` | Settings | `SettingsScreen.kt` |

Android nav routes (internal Compose NavHost, not URLs):

```
play-next
game/{gameId}
picks
taste
taste-map
settings
```

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
