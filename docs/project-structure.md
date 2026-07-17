# Projektstruktur

## Wurzelverzeichnis

```text
.
|-- .github/
|   |-- ISSUE_TEMPLATE/       Vorlagen fuer Fehler und Feature-Wuensche
|   `-- workflows/build.yml   Build und Artefakt-Upload fuer Push und PR
|-- docs/                     Technische Projektdokumentation
|-- gradle/wrapper/           Festgelegte Gradle-Wrapper-Version
|-- src/main/java/            Java-Quellcode
|-- src/main/resources/       Fabric-Metadaten, Mixin und Assets
|-- build.gradle              Plugins, Abhaengigkeiten und Build-Konfiguration
|-- gradle.properties         Minecraft-, Mod- und Dependency-Versionen
|-- settings.gradle           Gradle-Plugin-Repositories
|-- gradlew / gradlew.bat     Plattformabhaengige Gradle-Wrapper
`-- LICENSE                   MIT-Lizenz
```

Generierte Verzeichnisse wie `.gradle/`, `build/`, `out/` und `run/` werden nicht
versioniert.

## Java-Pakete

Alle Klassen liegen unter `de.twyco.soundboard`.

### Einstiegspunkt

`Soundboard` implementiert Fabrics `ModInitializer` und startet alle
clientseitigen Dienste in einer festen Reihenfolge. `MOD_ID` und der gemeinsame
SLF4J-Logger werden ebenfalls hier definiert.

### `client`

- `GlobalKeybinds` registriert globale Aktionen. Das Konfigurationsmenue ist ein
  Minecraft-Key-Binding; "alle Sounds stoppen" ist eine frei konfigurierbare
  Key Combo.
- `client.hud.HudService` bindet den Renderer vor dem Vanilla-Chat-HUD ein.
- `client.hud.SoundboardHudRenderer` zeigt aktive Sounds rechts unten an.

### `enums`

- `GlobalKeyBindings` ist der Katalog normaler Minecraft-Tastenbelegungen.
- `GlobalKeyCombos` ist der Katalog der Mod-eigenen Tastenkombinationen.
- `KeyComboEventType` unterscheidet `PRESS`, `HOLD` und `RELEASE`.

### `gui.config`

- `ConfigScreenFactory` erzeugt den eigenen Vanilla-Minecraft-Config-Screen und
  reicht Reloads an eine geoeffnete Instanz weiter.
- `SoundboardConfigScreen` verwaltet Tabs, Footer-Aktionen, den lokalen Entwurf
  und die Aufnahme von Tastenkombinationen.
- `ConfigDraft` trennt noch nicht gespeicherte UI-Werte von der aktiven
  Laufzeitkonfiguration.
- `widget.SoundboardConfigList` stellt globale Optionen und Sound-Einstellungen
  als scrollbare, responsive Vanilla-Liste dar.
- `widget.KeyComboButton` kapselt Aufnahmezustand und Aenderungs-Callback einer
  Tastenkombination.
- `widget.AmplifierSlider` bildet den Wertebereich von 0 bis 300 Prozent auf
  einen Minecraft-Slider ab.

### `gui.soundwheel`

`SoundWheelScreen` zeigt bis zu sechs alphabetisch sortierte Sounds radial an.
Er verwaltet Mausauswahl, Seitenwechsel und Wiedergabe per primaerem Mausklick.
Laufende Sounds und Loop-Eigenschaften werden direkt in den Sektoren angezeigt;
das Loslassen der konfigurierten Oeffnen-Combo schliesst nur den Screen.

### `interfaces`

Die funktionalen Interfaces `KeyBindingCallback`, `KeyComboCallback` und
`VoicechatListener` geben den Callback-Signaturen sprechende Typen. Die
Simple-Voice-Chat-Unterpakete verfeinern `VoicechatListener` fuer Client- und
Server-Events. Der Server-Typ wird aktuell nicht verwendet, da der Mod
clientseitig ist.

### `mixin`

`KeyboardMixin` injiziert am Anfang von `KeyboardHandler.keyPress`. Es reicht das
rohe GLFW-Ereignis an den `KeyComboManager` weiter und bricht die weitere
Minecraft-Verarbeitung ab, sobald eine Combo ein Ereignis ausgeloest hat.

### `modImplementations`

- `modMenu.ModMenuApi` stellt Mod Menu die `ConfigScreenFactory` bereit.
- `simpleVoicechatApi.SimpleVoicechatApi` ist das Voicechat-Plugin, registriert
  Events und haelt Voicechat-API, Lautstaerkekategorie und lokalen Audiokanal.
- `simpleVoicechatApi.SimpleVoicechatService` dekodiert, verwaltet und mischt
  Sounds.
- `listener.ClientVoicechatConnectionListener` richtet beim Verbinden die
  clientseitigen Voicechat-Objekte ein und raeumt sie beim Trennen auf.
- `listener.MergeClientSoundListener` delegiert Audiobloecke an den Service.
- `util.PlayingSound` ist der veraenderliche Wiedergabezustand eines Sounds.

### `util.client`

`FocusWatcher` erkennt, wenn Minecraft nach einem Fokusverlust wieder aktiv wird.
`FocusActionScheduler` fuehrt dann vorgemerkte Aktionen aus. Dies wird verwendet,
um nach dem Schliessen des extern geoeffneten Sound-Ordners Konfiguration,
Dateiliste und Config-Screen neu zu laden.

### `util.config`

- `SoundboardConfig` liest und schreibt JSON mit Gson.
- `SoundboardConfigData` ist das Wurzelobjekt der Konfiguration.
- `entries.GlobalStateEntry` enthaelt globale Laufzeitoptionen.
- `entries.SoundEntry` enthaelt persistierte Einstellungen pro Audiodatei.

### `util.keybinding`

- `KeyBindingManager` verarbeitet normale Minecraft-`KeyMapping`-Objekte am Ende
  eines Client-Ticks.
- `KeyCombo` repraesentiert ID und Tastenmenge einer Kombination.
- `KeyComboManager` verwaltet Combo-Zustaende und Callbacks.
- `KeyHelper` fragt den aktuellen Tastenzustand am Minecraft-Fenster ab.

### `util.sound`

- `SoundManager` scannt den Sound-Ordner, verknuepft Dateien mit der
  Konfiguration und delegiert Start und Stopp an Simple Voice Chat.
- `Sound` ist das Laufzeitmodell einer Datei mit ID, Pfad, Verstaerkung, Loop und
  Key Combo.

## Ressourcen

| Datei | Aufgabe |
| --- | --- |
| `fabric.mod.json` | Mod-Metadaten, Entrypoints und zwingende Abhaengigkeiten |
| `soundboard.mixins.json` | Registrierung von `KeyboardMixin`, Java-Level 25 |
| `assets/soundboard/lang/en_us.json` | Englische Texte und Translation Keys |
| `assets/soundboard/lang/de_de.json` | Deutsche Uebersetzung derselben Keys |
| `assets/soundboard/icon.png` | Mod-Icon |
