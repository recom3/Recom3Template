# Sign and distribute an APK

For local development, the debug APK from `:app:assembleDebug` is signed with your Android debug key. To distribute updates, choose a unique `applicationId`, increase `versionCode` for each release, and keep the same signing key. Debug and release builds signed with different keys require a fresh installation or different application IDs. Uninstalling an existing build also deletes its app data.

## Build and sign a release

Create a keystore with Android Studio or with JDK 8's `keytool`. Keep it outside the repository and back it up. This example prompts for passwords.

```powershell
& "$env:JAVA_HOME\bin\keytool.exe" -genkeypair -v -keystore 'C:\keys\my-app.jks' -alias my-app -keyalg RSA -keysize 2048 -validity 10000
.\build-apk.ps1 -Keystore 'C:\keys\my-app.jks' -KeyAlias my-app
```

The script uses the `JAVA_HOME` and `ANDROID_HOME` setup from the README. It runs `:app:assembleRelease`, checks ZIP alignment, signs the APK and checks that its signature is valid on API 16. It also checks the final manifest for `android:testOnly` and `android:debuggable`. The output is `app/build/outputs/apk/uplink/app-release.apk`.

On other platforms, run `sh ./gradlew :app:assembleRelease`. The unsigned APK is `app/build/outputs/apk/release/app-release-unsigned.apk`. Use `zipalign -c 4` to check alignment, then `apksigner sign --ks <keystore> --ks-key-alias <alias> --out app-release.apk <unsigned-apk>` and `apksigner verify --min-sdk-version 16 --print-certs app-release.apk`. Use the tools from Build Tools 27.0.3. [Android signing documentation](https://developer.android.com/tools/apksigner)

## Test the release APK

Install the signed APK on a Snow2 with `adb install -r`. Check that it opens, the remote controls work, and an earlier build signed with the same key can be upgraded.

## Publish the template repository

Push the source to GitHub. Under **Settings → General**, enable **Template repository** to show **Use this template** on the repo. The ignore file excludes local SDK settings, private keystores and build outputs. Add a signed APK as a GitHub Release asset if you want to share a binary. [GitHub template repository documentation](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-template-repository)
