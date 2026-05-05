# Stoic Quote Android — Build Spec

## Goal

Build an Android app (Kotlin + Jetpack Compose preferred) that shows one curated Stoic quote per day. This is a port of the [Stoic Quote Cinnamon desklet](https://github.com/WanderingBread0/stoic-quote-desklet). **Do not literally port the desklet code** — the JS uses `imports.ui.desklet`, `St.Label`, `Mainloop`, `Settings.DeskletSettings`, all GJS/Cinnamon-specific. Treat `reference/desklet.js` as a behavior spec, not source to translate.

## Data

`data/quotes.json` — array of 63 entries:

```json
{ "text": "...", "author": "...", "source": "..." }
```

Bundle this as an `assets/` resource (or convert to a Kotlin `List<Quote>` at build time). Authors: Marcus Aurelius, Epictetus, Seneca, Musonius Rufus, Chrysippus, Cleanthes, Zeno of Citium. Sources: *Meditations*, *Discourses*, *Enchiridion*, *Letters from a Stoic*, *Lectures*, *Hymn to Zeus*, *Lives of the Eminent Philosophers*, etc.

## Behavior

### Daily rotation (deterministic)

The same quote is shown to every user on a given calendar date. Compute today's index as:

```
index = djb2(YYYY-MM-DD) mod 63
```

djb2 hash (from desklet.js):
```
h = 5381
for each char c in str: h = ((h << 5) + h + c) & 0xFFFFFFFF
return h
```

Quote does **not** reshuffle on app restart — it's a pure function of the date.

### Manual advance

Tapping the quote (or a refresh button) increments a `manualOffset` counter. The displayed index becomes:

```
index = (djb2(YYYY-MM-DD) + manualOffset) mod 63
```

`manualOffset` resets to 0 at local midnight (when the date changes).

### Refresh modes (settings)

- **Daily** — auto-rerender at local midnight (the date-hash will produce a new index)
- **Hourly** — re-evaluate every hour (no behavior change unless midnight crosses)
- **Manual only** — no auto-refresh; only tap-to-advance

## UI

Match the spirit of `reference/stylesheet.css`:

- Centered quote text wrapped in typographic curly quotes: `"…"`
- Below the quote: em-dash + author (`— Marcus Aurelius`)
- Below the author: smaller, muted source line (`Meditations, Book 5`) — toggleable in settings
- Calm dark background, generous padding, serif-ish quote font
- A small refresh button (or use the whole-screen tap) — toggleable

### Settings screen

| Setting | Default | Description |
|---|---|---|
| Show source | On | Show book/work name under the author |
| Show refresh button | On | Show a visible refresh button (vs. tap-anywhere) |
| Refresh frequency | Daily | Daily / Hourly / Manual only |

## i18n

`reference/i18n/strings.pot` lists all 132 translatable strings from the desklet (every quote, author, source, plus UI labels). Convert these to Android `res/values/strings.xml` resources. Ship English to start; other locales drop into `res/values-<lang>/strings.xml` later.

Note: in the desklet, every quote text/author/source is wrapped in `_()` so translators can localize the entire dataset. Do the same on Android — every displayed string should come from a string resource, not a hardcoded literal.

## Constraints

- **No network access** — everything runs offline from bundled data
- **No analytics, no telemetry**
- **No permissions requested**
- **Single-activity app** — keep it minimal
- **Min SDK**: target current Android (API 33+ is fine)

## App icon

Use `reference/icon.svg` as the source. Generate the standard adaptive-icon set (foreground/background layers) and the legacy `mipmap-*` PNG sizes.

## Out of scope (don't add)

- Account/login
- Cloud sync of `manualOffset`
- Push notifications
- Widget on home screen (could be a v2)
- Quote sharing / social features
- In-app purchases
