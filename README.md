# Stoic Quote — Android

Android port of the [Stoic Quote Cinnamon desklet](https://github.com/WanderingBread0/stoic-quote-desklet). Single-activity app that shows one Stoic quote per day from a 63-entry curated dataset.

## What's in this repo

```
data/
  quotes.json          — 63 quotes (text/author/source). Bundle this as an asset.
reference/
  desklet.js           — original Cinnamon desklet — DO NOT port literally
  stylesheet.css       — visual styling cues (colors, spacing, font sizes)
  icon.svg / icon.png  — source artwork for the app icon
  screenshot.png       — what the desklet looks like
  i18n/strings.pot     — gettext template with all 132 translatable strings
SPEC.md                — what the app should do (read this first)
```

## Status

Empty Android project — to be built. See `SPEC.md` for the full behavior spec.

## License

Public domain / CC0. Quote texts are historical works in the public domain.
