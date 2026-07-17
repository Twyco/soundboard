# Entwicklung und Konventionen

## Voraussetzungen

- JDK 25 fuer Quell- und Zielkompatibilitaet
- Git
- Keine globale Gradle-Installation erforderlich; der Wrapper ist enthalten

Die Gradle-Wrapper-Version ist versionsabhaengig und wird pro Minecraft-Branch
gepflegt. `build.gradle` setzt Java-Toolchain, `sourceCompatibility`,
`targetCompatibility` und Compiler-Release auf 25.

## Wichtige Befehle

```bash
./gradlew build
./gradlew runClient
./gradlew clean
```

Das remappte Mod-JAR entsteht unter `build/libs/`. Fuer lokale Spielstarts legt
Fabric Loom die Instanz unter `run/` an. Beide Verzeichnisse werden ignoriert.

## Versionspflege und Branches

Die Minecraft-Versionen werden getrennt gepflegt:

- `26.2` ist der aktuelle Default- und primaere Entwicklungsbranch.
- `26.1.x` wird parallel fuer Minecraft 26.1 gepflegt.
- `1.21.11` wird als aelterer, bereits unobfuskierter Branch gepflegt.
- Weitere aeltere Branches bleiben versionsbezogen getrennt.

Neue Aufgaben werden zuerst auf dem aktuellen 26.2-Branch umgesetzt. Eine
Uebertragung auf 26.1.x ist ein eigener Arbeitsschritt und soll nicht unbemerkt
zusammen mit der 26.2-Aenderung erfolgen.

### Backport-Strategie

Features werden grundsaetzlich per Cherry-pick rueckportiert. Dateien manuell zu
kopieren ist nur ein Ausweg, wenn sich ein Commit auch nach sinnvoller Aufteilung
nicht uebertragen laesst. Cherry-picks erhalten die Herkunft einzelner Features,
vermeiden versehentlich ausgelassene Dateien und machen spaetere Backports
nachvollziehbar.

Fuer einen Backport gilt:

1. Der Zielbranch wird von seinem dauerhaften Minecraft-Versionsbranch erstellt,
   beispielsweise `26.1.x-ui-rework` von `26.1.x`.
2. Auf dem Quellbranch werden nur die Feature-Commits nach dessen fertiger
   Versionsbasis ermittelt, beispielsweise mit
   `git log --first-parent --reverse 26.2..26.2-ui-rework`.
3. Diese Commits werden in derselben Reihenfolge cherry-gepickt. Ein Merge-Commit,
   der selbst Teil des Feature-Stacks ist, wird mit seiner First-Parent-Seite als
   Basis uebernommen, beispielsweise `git cherry-pick -m 1 <commit>`.
4. Minecraft-Port-Commits vor der Quellbasis werden nicht uebernommen. Der
   Release-Commit fuer `mod_version` wird dagegen uebernommen, wenn der Backport
   denselben Mod-Release bereitstellt.
5. Bei Konflikten bleiben `minecraft_version`, Loader, Loom, Fabric API, Mod Menu,
   Simple Voice Chat und die Minecraft-Constraints des Zielbranches erhalten.
   Nur fachlich beabsichtigte Abhaengigkeitsaenderungen wie das Entfernen von
   Cloth Config werden uebertragen.
6. Unterschiede in Minecraft-, Fabric- oder GUI-APIs werden zielversionsspezifisch
   angepasst. Unobfuskierte Namen bedeuten nicht, dass Signaturen und
   Render-Lebenszyklus zwischen 1.21.11, 26.1 und 26.2 identisch sind.
7. Zielversionsspezifische Anpassungen und Dokumentation kommen in einen eigenen
   Commit oberhalb des gemeinsamen Feature-Stacks. Der PR zielt immer auf den
   zugehoerigen dauerhaften Versionsbranch.
8. Vor dem Push werden mindestens `./gradlew build`, die Werte in
   `gradle.properties` und die Constraints in `fabric.mod.json` geprueft.

Neue Features werden weiterhin zuerst auf dem aktuellen Hauptversionsbranch
entwickelt. Fuer jede noch unterstuetzte Minecraft-Version entsteht danach ein
eigener Backport-Branch nach demselben Ablauf.

Die zentralen Versionswerte stehen in `gradle.properties`:

- `minecraft_version`
- `loader_version`
- `loom_version`
- `fabric_version`
- `modmenu_version`
- `voicechat_api_version`
- `voicechat_version`
- `mod_version`

Bei einem Versionsupdate muessen ausserdem die Constraints in `fabric.mod.json`
und gegebenenfalls das Java-Level in `soundboard.mixins.json` geprueft werden.

## Abhaengigkeiten

| Abhaengigkeit | Rolle |
| --- | --- |
| Fabric Loader | Mod-Lader und Entrypoints |
| Fabric API | Client-Ticks, Key Mappings und HUD-Registrierung |
| Simple Voice Chat API | MP3-Dekoder, Events und Audiokanaele |
| Simple Voice Chat | Laufzeit-Mod, fuer die lokale Entwicklungsinstanz |
| Mod Menu | Optionaler Einstieg in den Config-Screen |
| Gson | JSON-Persistenz, ueber die vorhandene Laufzeit bereitgestellt |

Simple Voice Chat ist fachlich und laut `fabric.mod.json` eine zwingende
Abhaengigkeit. Mod Menu ist nur vorgeschlagen; das Menue kann auch ueber das
Minecraft-Key-Binding geoeffnet werden.

## Codekonventionen

### Sprache und Benennung

- Paketnamen bleiben unter `de.twyco.soundboard`.
- Klassen und Enums verwenden `UpperCamelCase`, Methoden und Felder
  `lowerCamelCase`, Konstanten `UPPER_SNAKE_CASE`.
- Technische IDs beginnen mit `soundboard.`; Ressourcen verwenden den Namespace
  `soundboard`.
- Translation Keys werden nach Funktionsbereich gruppiert, beispielsweise
  `gui.soundboard.config.*` und `key.soundboard.*`.
- Logmeldungen beginnen im bestehenden Code meist mit `[Klasse/Methode]`.
- Quellcode, Bezeichner und UI-Translations bleiben Englisch. Die technische
  Projektdokumentation liegt auf Deutsch vor.

### Struktur und Zustaendigkeiten

- Fabric-, Mod-Menu- und Voicechat-spezifische Adapter bleiben in den jeweiligen
  `modImplementations`-Paketen.
- Persistenzlogik bleibt in `util.config`, Dateierkennung und Sound-Laufzeitmodell
  in `util.sound`.
- Der eigene Config-Screen arbeitet auf `ConfigDraft`; Widgets veraendern nicht
  direkt die persistierte Laufzeitkonfiguration.
- GUI-Widgets enthalten keine Audioverarbeitung und delegieren Aktionen an Screen
  oder Manager.
- Gemeinsam verwendete UI-Farben und Zeichenprimitive liegen in
  `gui.component.SoundboardUi`. Fachliche Rad-Geometrie und Screen-Interaktion
  bleiben im jeweiligen Screen.
- Neue globale Standardaktionen werden im passenden Enum deklariert und zentral
  in `GlobalKeybinds` auf einen Callback abgebildet.
- Nicht instanziierbare Service- und Utility-Klassen erhalten einen privaten
  Konstruktor.

### Nullbarkeit und Daten

- Pflichtparameter werden im bestehenden Code mit JetBrains `@NotNull`
  gekennzeichnet; Minecraft-/GUI-Overrides verwenden teilweise JSpecify.
- Getter auf Collections sollen keine frei veraenderbaren internen Datenstrukturen
  herausgeben. `KeyCombo.getKeyCodes()` und `SoundManager.getAllSounds()` folgen
  diesem Muster bereits.
- Konfigurationsdaten muessen von Gson ohne Spezialadapter lesbar und schreibbar
  bleiben.
- Sound-IDs sind Dateinamen. Eine Aenderung dieses Schemas benoetigt eine
  Config-Migration.

### Threads und Lebenszyklus

- Minecraft-Zustand und GUI werden auf dem Client-Kontext veraendert.
- Aenderungen an Key-Combo-Registrierungen werden bis zum Tick-Ende gepuffert,
  damit Event-Verarbeitung und Registrierung nicht gleichzeitig dieselbe Map
  veraendern.
- Zugriff auf aktive Sounds muss mit der Synchronisationsstrategie des
  `SimpleVoicechatService` konsistent bleiben, da Voicechat-Event und HUD nicht
  zwingend denselben Aufrufkontext haben.
- Beim Trennen von Simple Voice Chat muessen lokale Referenzen und aktive Sounds
  aufgeraeumt werden.

## Erweiterungsmuster

### Neue globale Key Combo

1. Wert mit stabiler ID und Translation Key zu `GlobalKeyCombos` hinzufuegen.
2. Aktion im Switch von `GlobalKeybinds.getKeyComboAction()` implementieren.
3. Einen passenden Eintrag in `SoundboardConfigList` anzeigen.
4. Englischen Translation Key in `en_us.json` ergaenzen.

### Neues Minecraft-Key-Binding

1. Wert zu `GlobalKeyBindings` hinzufuegen.
2. Callback in `GlobalKeybinds.getKeyBindingAction()` implementieren.
3. Translation Key ergaenzen und einen sinnvollen Default-Key festlegen.

### Neues Audioformat

1. Endung in `SoundManager.isSupportedSoundFile()` zulassen.
2. Dekodierung in `SimpleVoicechatService.decodeSoundToPcm()` ergaenzen.
3. Ausgabe weiterhin auf 48 kHz, Mono und 16-Bit-PCM normalisieren.
4. Fehlerfaelle und ungueltige Kanal-/Sampleraten sauber loggen.
5. UI-Text und diese Dokumentation aktualisieren.

## Ressourcen und Lokalisierung

Neue sichtbare Texte gehoeren nach
`src/main/resources/assets/soundboard/lang/<locale>.json`. Im Java-Code werden
dafuer `Component.translatable(...)` und stabile Translation Keys verwendet.
Dateinamen aus dem Benutzerverzeichnis und dynamische Werte bleiben Literale.

Neue Mixins muessen in `soundboard.mixins.json` eingetragen werden. Mixins sollen
klein bleiben und moeglichst sofort an eine testbare Manager- oder Serviceklasse
delegieren.

## Build-Workflow

`.github/workflows/build.yml` startet bei jedem Push und Pull Request einen
Gradle-Build und laedt `build/libs/` als Artefakt hoch. Der Workflow richtet
aktuell JDK 21 ein, waehrend der Build eine Java-25-Toolchain verlangt. Vor einer
aktiven Nutzung des Workflows sollte diese Versionsabweichung geprueft werden.

Automatisierte Projekttests sind derzeit nicht vorhanden. Bei Aenderungen sind
mindestens `./gradlew build` und ein manueller Start mit `./gradlew runClient`
vorgesehen. Audioaenderungen sollten mit Mono- und Stereo-MP3s, verschiedenen
Sampleraten, Mute an/aus, Loop sowie paralleler Wiedergabe geprueft werden.

## Checkliste fuer Aenderungen

1. Auf dem 26.2-Branch arbeiten und bestehende lokale Aenderungen respektieren.
2. Aenderung in der fachlich zustaendigen Klasse umsetzen.
3. Translation Keys und Config-Kompatibilitaet pruefen.
4. Bei Audiocode Mute, Loop, parallele Sounds und Stop-Verhalten pruefen.
5. `./gradlew build` ausfuehren.
6. Betroffene Dokumentation aktualisieren.
7. Portierung auf 26.1.x und 1.21.11 bei Bedarf nach der dokumentierten
   Backport-Strategie separat vornehmen.
