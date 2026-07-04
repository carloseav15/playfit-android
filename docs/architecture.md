# Architecture

## Intent

Playfit for Android should be a native Android product showcase, not a web view and not a direct clone of React components. It borrows the product contract from `/play`, then presents it through Android-native Compose, Material 3, navigation, and adaptive layout patterns.

## Current Shape

The first pass is intentionally monomodule:

```text
app/
  data/
  model/
  ui/
    components/
    screens/
    theme/
```

That keeps the project easy to open, compile, and inspect. When API/cache work begins, split into modules only when there is real complexity:

```text
core:model
core:design
core:data
feature:playnext
feature:gamedetail
feature:picks
feature:taste
feature:settings
```

## Next Data Direction

Phase 1 uses mock data only.

Phase 2 should introduce a repository interface:

```text
PlayfitRepository
  getTodayRecommendations()
  getPicks()
  getTasteProfile()
  saveGameState(gameId, state)
```

Phase 3 should add:

- API client for Playfit product endpoints.
- Room for cached recommendations and game state.
- DataStore for user preferences.
- WorkManager for deferred sync.
- ViewModels exposing `StateFlow`.

## Design Direction

Use Material 3 and Compose idioms first:

- `Scaffold`
- `NavigationBar`
- `Card`
- `AssistChip`
- `LazyColumn`
- responsive/adaptive layouts

Material 3 Expressive should be used as a product-quality layer, not decoration. The first priority is clarity: one recommendation, why it fits, and fast feedback.
