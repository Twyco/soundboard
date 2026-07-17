# Entwicklung und Konventionen

## Voraussetzungen

- JDK 25 fuer Quell- und Zielkompatibilitaet
- Git
- Keine globale Gradle-Installation erforderlich; der Wrapper ist enthalten

Der Gradle-Wrapper verwendet Gradle 9.6.1. `build.gradle` setzt Java-Toolchain,
`sourceCompatibility`, `targetCompatibility` und Compiler-Release auf 25.

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
- Aeltere Branches bleiben versionsbezogen getrennt.

Neue Aufgaben werden zuerst auf dem aktuellen 26.2-Branch umgesetzt. Eine
Uebertragung auf 26.1.x ist ein eigener Arbeitsschritt und soll nicht unbemerkt
zusammen mit der 26.2-Aenderung erfolgen.

Die zentralen Versionswerte stehen in `gradle.properties`:

- `minecraft_version`
- `loader_version`
- `loom_version`
- `fabric_version`
- `modmenu_version`
- `cloth_config_version`
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
| Cloth Config | Config-Screen und Eintraege |
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
- GUI-Factorys bauen Eintraege, enthalten aber moeglichst keine Audioverarbeitung.
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
3. `KeyComboEntry` in der passenden Config-Kategorie anzeigen.
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
7. Portierung auf 26.1.x bei Bedarf separat vornehmen.
