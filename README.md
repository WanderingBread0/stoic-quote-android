# Stoic Quote — Android

Android port of the [Stoic Quote Cinnamon desklet](https://github.com/WanderingBread0/stoic-quote-desklet). Daily curated Stoic quote, on your phone as a homescreen widget or a full-screen app.

## Install on your phone

1. Open the [Latest release](https://github.com/WanderingBread0/stoic-quote-android/releases/tag/latest) page on your phone's browser.
2. Download `app-debug.apk`.
3. Open the file. Android will ask permission to install from this source — allow it.
4. Add the widget: long-press your home screen → **Widgets** → find **Stoic Quote** → drag to where you want it.

You can also open the app from the launcher to see the same quote in full-screen — tap the quote to advance to the next one (resets at midnight).

## What it does

- 143 curated Stoic quotes from Marcus Aurelius, Epictetus, Seneca, Musonius Rufus, Chrysippus, Cleanthes, Zeno of Citium, and other Stoic and proto-Stoic thinkers.
- Same quote of the day for everyone — a deterministic per-cycle shuffle picks the quote, so it doesn't reshuffle on restart, cycles through every quote once before repeating, and matches the desklet on the same date.
- Tap the quote (in the app or the widget) to advance to another. The advance counter resets at midnight.
- Auto-updates the widget at midnight with the new day's quote.
- Offline. No accounts, no analytics, no permissions, no network access.

## Build

The CI workflow at `.github/workflows/build-apk.yml` builds the APK on every push to `main` and updates the `latest` release with the new file.

To build locally:
```bash
gradle wrapper --gradle-version 8.7
./gradlew assembleDebug
# APK will be at app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and Android SDK with platform 34.

## Project layout

```
app/                          Android module
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    assets/quotes.json        63 quotes, bundled
    kotlin/com/orion/stoicquote/
      MainActivity.kt         Compose UI for the app
      StoicQuoteWidget.kt     Homescreen widget (AppWidgetProvider)
      QuoteRepo.kt            djb2 hash, today's quote
      Prefs.kt                Manual offset, midnight reset, settings
    res/
      layout/widget.xml       Widget RemoteViews layout
      xml/widget_info.xml     Widget configuration
      drawable/               Backgrounds and adaptive launcher icon
      values/                 strings, colors, themes
data/quotes.json              Source data (also bundled into app/src/main/assets/)
reference/                    Original desklet files for reference
SPEC.md                       Behavior spec
```

## License

Public domain / CC0. Quote texts are historical works in the public domain.
