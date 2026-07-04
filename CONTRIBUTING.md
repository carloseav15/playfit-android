# Contributing

Playfit for Android is a portfolio-grade project. Contributions should keep the
repo easy to review, easy to build, and honest about operational limits.

## Local Setup

```sh
./gradlew :app:assembleDebug
```

Open the project in Android Studio and let it sync Gradle dependencies.

## Quality Gate

```sh
./gradlew :app:test
```

Keep unit tests passing. Add tests for new ViewModels, repositories, or model
logic.

## Standards

- Keep runtime secrets out of source control.
- Use Kotlin conventions: `StateFlow` over `LiveData`, sealed classes for UI
  state, `flow` builders for async data.
- Prefer Hilt for injection, Room for persistence, Coil for images.
- Keep composables focused: one file per screen, extract reusable components
  to `ui/components/`.

## Pull Requests

PRs should include the user-facing reason for the change, validation performed,
and any known limits. If a check is skipped, explain why.
