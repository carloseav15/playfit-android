# Manual Production Device Validation

This checklist is the safe handoff for the paused task: connect Android end-to-end to the real production API and validate auth on a physical device.

Do not run this casually. Use it only when the user explicitly approves production/device validation.

## Preconditions

- Confirm the production Playfit API and Supabase project to use.
- Confirm the test account and whether it may create/delete Playfit profile data.
- Use a physical Android device with a clean install.
- Build from the intended release or debug variant with explicit production values in `local.properties`.
- Do not use a personal account unless the user explicitly approves it.

## Build Sanity

- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- `./gradlew assembleDebug`
- Optional local device smoke: `./gradlew connectedDebugAndroidTest`

## Device Flow

- Install the APK on the physical device.
- Launch from a clean app data state.
- Confirm anonymous/guest session starts without blocking the intro.
- Complete onboarding with platforms, three loved games, and one disliked game.
- Confirm Play Next loads a primary recommendation from the real API.
- Use Add Pick, Skip, Not for me, and Already Played with all four reactions.
- Confirm Picks reflects saved and removed games.
- Open a Game Dossier and confirm the bottom action bar remains usable after scrolling.
- Open Taste, Taste Map, and Decisions Activity; confirm changed/deleted signals refresh without app restart.
- Open Settings and validate Appearance, Platforms, Account, and Privacy states.
- Sign in or link the approved auth account.
- Sign out and relaunch; confirm the expected session/profile behavior.

## Safety Boundaries

- Do not run destructive account deletion unless a dedicated disposable test account is approved.
- Do not modify Supabase production schema, auth config, or storage.
- If a write fails, capture logs and stop; do not keep retrying production writes blindly.
- If the app shows stale/pending-sync state, document the exact screen, action, and network condition.

## Evidence To Capture

- Device model and Android version.
- Build variant and API base URL host.
- Timestamped pass/fail notes for each flow.
- Relevant `adb logcat` excerpts for failures only.
- Screenshots only if they do not expose tokens, email addresses, or private profile data.

## Closeout Criteria

Only mark the paused TASKS.md item complete after:

- The approved physical-device flow passes end to end.
- Auth state survives relaunch as expected.
- No production-only API/auth errors appear in logs.
- Unit tests, lint, assemble, and connected UI tests pass.
