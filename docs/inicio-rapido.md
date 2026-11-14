# Inicio rápido

Recom3Template es una app Java para el visor **Recon Snow2**. Muestra velocidad o precisión horizontal del GPS. El mando cambia de métrica y permite pausar las actualizaciones.

1. En GitHub, pulsa **Use this template** y clona tu nuevo repositorio.
2. Instala **JDK 8**, **Android SDK Platform 27**, **Build Tools 27.0.3** y **Platform-Tools**. Configura `JAVA_HOME` y `ANDROID_HOME` con las rutas de tu equipo. La primera compilación descarga Gradle 4.1 y sus dependencias.
3. En PowerShell, desde la raíz del proyecto, ejecuta lo siguiente con el HUD conectado por USB y accesible mediante ADB.

   ```powershell
   .\gradlew.bat :app:assembleDebug
   $adb = Join-Path $env:ANDROID_HOME 'platform-tools/adb.exe'
   & $adb devices
   & $adb install -r app/build/outputs/apk/debug/app-debug.apk
   & $adb shell am start -n com.recom3.template/.TemplateActivity
   ```

4. Sal al exterior y espera a que aparezca **GPS READY**. Usa **← / →** para cambiar de métrica, **OK** para pausar o reanudar el GPS y **Atrás** para salir.
5. Cambia `applicationId` en [app/build.gradle](../app/build.gradle), el nombre en [strings.xml](../app/src/main/res/values/strings.xml) y el icono en [drawable](../app/src/main/res/drawable/). La [actividad](../app/src/main/java/com/recom3/template/TemplateActivity.java) recibe los eventos y decide qué mostrar. La [vista del HUD](../app/src/main/java/com/recom3/template/TemplateHud.java) dibuja los datos sobre un lienzo de 428 × 240.

El ejemplo se compila con el SDK de Android y funciona con las API de Android 16. El módulo `reconUI` carga las fuentes Open Sans. Si tu app necesita los servicios de orientación o conectividad de ReconOS, la guía del SDK explica cómo añadir el JAR del add-on y declarar la biblioteca del dispositivo.

[Guía del SDK](sdk.md) · [Diseño del HUD y mando](ui.md) · [Firma y distribución con Uplink](publishing.md)
