# Designing for the Snow2 HUD

The example uses a full-screen Android `View` in landscape. [TemplateHud.java](../app/src/main/java/com/recom3/template/TemplateHud.java) draws in a **428 × 240** coordinate system borrowed from Recom3Ghost. The view scales both axes equally to fit and centers the result. Text coordinates in `drawText()` refer to the baseline.

| HUD region | What it draws |
| --- | --- |
| Header | App name at left and GPS state at right |
| Main panel | Selected metric, large value and unit |
| Footer | Controls available in the current state |

The title, status labels, metrics and footer hints live in [strings.xml](../app/src/main/res/values/strings.xml). The background and full-screen theme live in [styles.xml](../app/src/main/res/values/styles.xml). The colors in `TemplateHud` use light text on a dark panel, green for a current fix and amber for other states. Every status has a text label. [reconUI](../reconUI/README.md) loads the included Open Sans Regular and Bold fonts on API 16.

## Add a field or a screen

1. Choose the state needed to draw your screen. The activity owns the selected metric, current fix and paused state. Add your new state there and decide what to show when a value is absent.
2. Format the label, value and unit in the activity. Pass them into the view through `show()` and call `invalidate()` after changing the view's fields. Keep long work and service calls out of `onDraw()`.
3. Use `TemplateHud.onDraw()` to place a new element. Keep the main number large, allow room for long values and check that the header labels fit beside each other. If your app has menus or focusable controls, Android XML layouts with explicit D-pad focus order are another option. The historical Recon UI examples include lists, dialogs and carousels. [ReconUIExamples](https://github.com/gstarnet/sdk/tree/master/Samples/ReconUIExamples)

## Controls

| Remote | Android event | Example |
| --- | --- | --- |
| Left / Right | `KEYCODE_DPAD_LEFT` / `KEYCODE_DPAD_RIGHT` | Switch between speed and GPS accuracy |
| OK | `KEYCODE_DPAD_CENTER`, `KEYCODE_ENTER` or `KEYCODE_BUTTON_A` | Pause or resume GPS |
| Back | `KEYCODE_BACK` | Use Android's default back navigation |

`TemplateActivity.onKeyUp()` acts once when the button is released and skips canceled key events. Its default case lets Android handle keys the sample doesn't use. Show the current actions in the footer as you add screens. If a device maps a button differently, log the key code during development and test the mapping on that HUD.

## Check the interaction

With the sample running, try these commands from a host with `adb` on its PATH.

```text
adb shell input keyevent 22
adb shell input keyevent 23
adb shell input keyevent 23
adb shell input keyevent 4
```

Check that the metric switches, **GPS PAUSED** appears and clears, and Back exits. With a fix, check that pausing, disabling GPS, or losing the fix clears the metric. With the real HUD, also check the paired remote and readability in daylight.
