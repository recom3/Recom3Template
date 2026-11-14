# Android and the Recon SDK

Recom3Template targets the Snow2 API 16 environment. The Android compile SDK controls which symbols the compiler can see. `minSdkVersion 16` is the install floor and `targetSdkVersion 16` selects the legacy Android behavior. When adding an Android call introduced after API 16, check `Build.VERSION.SDK_INT` before calling it and provide fallback behavior.

| What you need | API or module |
| --- | --- |
| GPS, device sensors, the Activity lifecycle, storage and key events | Standard Android framework. Start with `LocationManager`, `SensorManager`, `Activity`, `getFilesDir()` or `KeyEvent`. |
| UI like the one in Recom3Ghost | Android `View` and `Canvas`, plus the included [reconUI font loader](../reconUI/README.md). |
| Heading from ReconOS and HTTP through a paired phone | The historical Recon Instruments SDK Add-On and the matching ReconOS system library on the device. |

## Follow the GPS example

1. The [manifest](../app/src/main/AndroidManifest.xml) declares `ACCESS_FINE_LOCATION`. On Snow2 API 16, Android handles this permission at install time. The feature is optional so the HUD can open and display a fallback on a device without GPS.
2. [TemplateActivity](../app/src/main/java/com/recom3/template/TemplateActivity.java) gets `LocationManager` from `Context.LOCATION_SERVICE`. It registers for `GPS_PROVIDER` updates in `onResume()` and removes the listener in `onPause()`. The remote's pause action uses the same registration and cleanup methods.
3. Every accepted fix is delivered to the main looper. The example clears the fix when the provider is disabled or the sample reaches five seconds of age. It checks `hasSpeed()` and `hasAccuracy()` before reading the corresponding values. Speed comes in m/s and is multiplied by 3.6 for km/h.
4. The activity formats the values and calls `TemplateHud.show()`. The view invalidates itself and draws on the next frame. Follow this sequence for a new data source and release its listener when the activity pauses.

[Android LocationManager reference](https://developer.android.com/reference/android/location/LocationManager) · [Android Location reference](https://developer.android.com/reference/android/location/Location)

## Add a ReconOS service

Use this section if your app imports `com.reconinstruments.os.*`. The base template builds with Android SDK Platform 27 alone.

1. With a local copy of the **Recon Instruments SDK Add-On for API 16**, locate `libs/com.reconinstruments.os.jar` inside the add-on folder. Under the Android SDK, look in `add-ons/` for a folder whose `manifest.ini` has `vendor=Recon Instruments` and `api=16`. The original SDK README describes a legacy SDK Manager repository. The examples and `docs/reference/index.html` in the installed add-on are useful offline.
2. Keep the add-on JAR outside the project. Set the following shell variable to its absolute path.

   ```powershell
   $env:RECON_SDK_JAR = 'C:\path\to\com.reconinstruments.os.jar'
   ```

3. Add the JAR as a compile-only dependency in `app/build.gradle`. The firmware supplies the classes at runtime.

   ```groovy
   def reconSdkJar = System.getenv('RECON_SDK_JAR')
   if (!reconSdkJar || !file(reconSdkJar).isFile()) {
       throw new GradleException('Set RECON_SDK_JAR to the Recon SDK add-on JAR.')
   }

   dependencies {
       compileOnly files(reconSdkJar)
   }
   ```

4. If your app requires ReconOS to start, declare the shared library inside the manifest's `<application>` element. Android rejects installation on devices where this library is missing. To check the connected HUD, run `adb shell pm list libraries` and look for `library:com.reconinstruments.os`.

   ```xml
   <uses-library android:name="com.reconinstruments.os" android:required="true" />
   ```

   For an optional integration, set `android:required="false"`, check `getPackageManager().getSystemSharedLibraryNames()`, and load your Recon-specific implementation class only when the library is present. Keep the rest of the app free of Recon imports so it can load on stock Android.

5. For heading, get the service with `HUDOS.getHUDService(HUDOS.HUD_HEADING_SERVICE)` and cast it to `HUDHeadingManager`. If the service is available, register a `HeadLocationListener` in `onResume()` and unregister it in `onPause()`. The listener receives yaw, pitch and roll through `onHeadLocation(float yaw, float pitch, float roll)`. Handle missing or invalid samples and send UI updates to the main thread. For paired-phone HTTP, start with `HUDConnectivityManager` and handle disconnects.

The historical add-on target string is `Recon Instruments:Recon Instruments SDK Add-On:16`. The steps above retain the template's compile SDK 27 setup while making the optional service types available to the compiler. Test Recon services on the intended firmware.

[Legacy SDK setup](https://github.com/gstarnet/sdk) · [DemoCompass](https://github.com/gstarnet/sdk/tree/master/Samples/DemoCompass) · [HUDConnectivityDemo](https://github.com/gstarnet/sdk/tree/master/Samples/HUDConnectivityDemo)
