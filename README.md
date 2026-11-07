# HP News – Harry-Potter-Nachrichten für Android

Zeigt aktuelle Nachrichten rund um Harry Potter (inkl. der HBO-Serie).

- **Deutsche Artikel** werden so angezeigt, wie sie sind.
- **Alle anderen Artikel** werden per Claude (KI) in **einfaches Englisch** umgeschrieben,
  mit einer **Vokabelliste Englisch → Deutsch** für die schwierigeren Wörter.
  Die Vokabeln sind im Text golden hervorgehoben. Das Original ist per Reiter umschaltbar.
- Zu jedem Artikel steht die **Quelle** (Herausgeber, Datum, Link) dabei.
- Filter: Alle · HBO-Serie · Deutsch · Englisch.

## Quellen

| Quelle | Sprache | Art |
|---|---|---|
| Bing News Suche „Harry Potter“ | DE | Schlagzeilen aus vielen Medien |
| Bing News Suche „Harry Potter Serie HBO“ | DE | dto. |
| Bing News Suche „Harry Potter“ / „HBO series“ | EN | dto. |
| MuggleNet | EN | Fan-Seite, Feed mit Text |
| The Leaky Cauldron | EN | Fan-Seite, Feed mit Text |

Weitere Quellen lassen sich in `core/src/main/kotlin/de/hpnews/core/Sources.kt` eintragen.

Den Artikeltext holt die App wie der Lesemodus eines Browsers von der Originalseite.
Bei Bezahlschranken gibt es nur einen Auszug und einen Link zum Original.

## Urheberrecht

Die App ist für den **privaten Gebrauch** gedacht: Sie lädt die Artikel direkt auf deinem Handy,
so wie ein Browser. Bitte die App mit Volltexten **nicht** öffentlich (z. B. im Play Store)
verbreiten – dafür müsste man auf Überschrift, kurze Zusammenfassung und Link umstellen.

## KI-Übersetzung

- Braucht einen Claude-API-Schlüssel (console.anthropic.com), einzutragen in der App unter Einstellungen.
- Modell wählbar: Claude Opus 5.5 (beste Qualität, ca. 5 Cent pro Artikel) oder
  Claude Haiku 5.5 (ca. 0,2 Cent pro Artikel).
- Übersetzt wird erst beim Öffnen eines Artikels, und jedes Ergebnis wird gespeichert –
  derselbe Artikel kostet also nur einmal.

## Aufbau

- `core/` – reines Kotlin: Feeds laden (RSS/Atom), Text extrahieren, Spracherkennung, Claude-Aufruf, Cache.
  Ohne Android testbar: `./gradlew -PcoreOnly :core:test`
- `app/` – Android-Oberfläche mit Jetpack Compose (Liste, Artikel, Einstellungen).

## Bauen

- **Automatisch:** Jeder Push auf `main` baut per GitHub Actions die APK und stellt sie unter
  *Releases → latest* als `HP-News.apk` bereit. Auf dem Handy herunterladen und installieren
  (Installation aus unbekannten Quellen erlauben).
- **Lokal:** In Android Studio öffnen und starten, oder `./gradlew :app:assembleDebug`.
