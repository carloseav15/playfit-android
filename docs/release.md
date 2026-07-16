# Android release

## Local validation

From `android-compose/`:

```sh
./gradlew testDebugUnitTest lintRelease bundleRelease
```

`bundleRelease` produces an unsigned bundle unless the signing variables below are
present. This keeps local builds safe and avoids requiring a private key in the repo.

## Release signing

The release build reads signing values only from environment variables:

```sh
export PLAYFIT_UPLOAD_STORE_FILE=/absolute/path/playfit-upload.jks
export PLAYFIT_UPLOAD_STORE_PASSWORD='...'
export PLAYFIT_UPLOAD_KEY_ALIAS='playfit-upload'
export PLAYFIT_UPLOAD_KEY_PASSWORD='...'
./gradlew bundleRelease
```

Never commit the keystore, passwords, or generated signed artifacts. In CI, store the
keystore as a base64-encoded secret, decode it into a temporary workspace file, and set
the four `PLAYFIT_UPLOAD_*` variables as masked secrets.

The repository workflow supports both modes: it creates an unsigned bundle when
`ANDROID_KEYSTORE_BASE64` is absent and a signed bundle when that secret plus the four
upload-signing secrets are present.

## Play Console checklist

- Keep the upload key separate from the Play App Signing key.
- Back up the keystore in an approved password manager.
- Increment `versionCode` for every upload.
- Upload the generated `.aab`, not the unsigned APK.
- Complete the Data Safety form and privacy-policy URL before production rollout.
