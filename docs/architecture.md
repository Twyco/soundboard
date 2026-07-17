# Architektur und Ablaeufe

## Komponentenuebersicht

```text
KeyboardMixin -> KeyComboManager -> Sound.play() -> SoundManager
Config Screen -> ConfigDraft -> SoundManager
SoundManager -> SimpleVoicechatService
                               |                  |
                               | mergeAudio()     | ClientStaticAudioChannel
                               v                  v
                         Voice-Chat-Ausgabe   lokale Wiedergabe

SoundboardConfig <---- GUI / SoundManager / GlobalKeybinds
SimpleVoicechatService ----> SoundboardHudRenderer
```

Die Klassen sind ueberwiegend statische Services. Es gibt keinen Dependency-
Injection-Container und keinen eigenen Event-Bus.

## Initialisierung

Fabric ruft `Soundboard.onInitialize()` auf. Die Reihenfolge ist relevant:

1. `SoundboardConfig.init()` laedt oder erzeugt `config/soundboard.json`.
2. `KeyComboManager.init()` registriert seine Tick-Verarbeitung.
3. `KeyBindingManager.init()` registriert Minecraft-Key-Binding-Verarbeitung.
4. `SoundManager.init()` scannt den Ordner `sounds/` und wendet die Config an.
5. `GlobalKeybinds.init()` registriert globale Aktionen und speichert Defaults.
6. `HudService.init()` registriert das HUD-Element.
7. `FocusWatcher.init()` beobachtet den Fensterfokus.

Unabhaengig davon erzeugt Fabric ueber den `voicechat`-Entrypoint eine Instanz von
`SimpleVoicechatApi`. Simple Voice Chat ruft dort `initialize()` und
`registerEvents()` auf.

## Verbindung mit Simple Voice Chat

Bei einer erfolgreichen Client-Verbindung:

1. Der `VoicechatClientApi` wird im `SimpleVoicechatService` gespeichert.
2. Eine Lautstaerkekategorie mit der ID `soundboardsounds` und dem Namen
   `Soundboard` wird registriert.
3. Ein `ClientStaticAudioChannel` mit zufaelliger UUID wird erstellt.
4. Der Kanal wird der Soundboard-Lautstaerkekategorie zugeordnet.

Beim Trennen wird die Kategorie abgemeldet, die Client-API entfernt und jede
aktive Wiedergabe beendet.

## Laden der Sounddateien

`SoundManager.reload()` leert die Laufzeitliste und liest alle regulaeren Dateien
direkt unterhalb von `<gameDirectory>/sounds`. Unterordner werden nicht rekursiv
durchsucht. Aktuell werden nur Dateinamen akzeptiert, die ohne Beachtung der
Gross-/Kleinschreibung auf `.mp3` enden.

Die Sound-ID und der sichtbare Name sind jeweils der vollstaendige Dateiname
inklusive Erweiterung. Dadurch ist der Dateiname zugleich der stabile Schluessel
in der JSON-Konfiguration. Fuer jede neue Datei wird bei Bedarf sofort ein
`SoundEntry` mit den globalen Standardwerten erzeugt und gespeichert.

## Start, Umschalten und Stopp

Eine Sound-Combo registriert einen `PRESS`-Callback auf `Sound.play()`. Der
`SimpleVoicechatService` dekodiert die MP3 vollstaendig in ein `short[]` und
normalisiert sie auf:

- 48.000 Samples pro Sekunde
- Mono
- vorzeichenbehaftete 16-Bit-PCM-Samples

Stereo wird durch Mittelung beider Kanaele in Mono umgewandelt. Andere
Kanalzahlen werden abgewiesen. Abweichende Sampleraten werden linear auf 48 kHz
umgerechnet.

Ist dieselbe Sound-ID bereits aktiv, soll der erneute Start den vorhandenen
Eintrag entfernen. Andernfalls wird ein neues `PlayingSound` angelegt. Andere
Sound-IDs bleiben aktiv und werden parallel gemischt. Die globale Stop-Combo
leert die gesamte Liste.

`PlayingSound` uebernimmt Loop- und Gain-Werte beim Start. Aenderungen an der
Konfiguration beeinflussen daher bereits laufende Instanzen nicht rueckwirkend.

## Mischen und Wiedergabe

Das `MergeClientSoundEvent` fordert fortlaufend Audiobloecke an. Pro Event wird
ein Block mit 960 Samples erzeugt, entsprechend 20 ms bei 48 kHz.

Fuer jeden aktiven Sound wird:

1. ab der aktuellen Position gelesen,
2. jedes Sample mit `amplifier / 100` multipliziert,
3. mit den anderen aktiven Sounds addiert,
4. auf den Bereich eines Java-`short` begrenzt,
5. die Abspielposition fortgeschrieben und
6. ein beendeter Nicht-Loop beim folgenden Durchlauf entfernt.

Wenn `playWhileMuted` aktiviert oder das Voicechat-Mikrofon nicht stummgeschaltet
ist, geht derselbe gemischte Block an zwei Ziele:

- `event.mergeAudio(mixed)` mischt ihn in den von Simple Voice Chat verarbeiteten
  Client-Audiostream.
- `ClientStaticAudioChannel.play(mixed)` spielt ihn lokal in der eigenen
  Soundboard-Lautstaerkekategorie ab.

Ist das Mikrofon stumm und `playWhileMuted` deaktiviert, werden Positionen weiter
fortgeschrieben, der Block wird jedoch nicht ausgegeben.

## Tastatureingaben

Das Projekt unterscheidet zwei Mechanismen:

| Mechanismus | Verwendung | Verarbeitung |
| --- | --- | --- |
| Minecraft `KeyMapping` | Config-Menue oeffnen | `consumeClick()` am Tick-Ende |
| Eigene `KeyCombo` | Sounds, Sound-Rad und globaler Stopp | rohes Ereignis im Mixin |

Eine `KeyCombo` besteht aus einer ID und einer ungeordneten Menge von GLFW-
Keycodes. Sie gilt als gedrueckt, wenn alle enthaltenen Tasten aktuell gedrueckt
sind. Leere Kombinationen werden nie ausgeloest.

Der `KeyComboManager` merkt sich pro ID, ob die Kombination zuvor gedrueckt war,
und leitet daraus `PRESS`, `HOLD` oder `RELEASE` ab. Registrierungen und
Abmeldungen werden bis zum Ende des Client-Ticks gepuffert. Bei geoeffnetem
Minecraft-Screen werden keine Combo-Callbacks ausgeloest.

Sobald ein Combo-Ereignis ausgeloest wurde, konsumiert `KeyboardMixin` das rohe
Tastaturereignis. Dieses Verhalten ist beabsichtigt, kann aber mit anderen
Minecraft- oder Mod-Tastenbelegungen kollidieren.

## Sound-Rad

Die globale Combo `soundboard.sounds.open_wheel` oeffnet bei `PRESS` einen nicht
pausierenden `SoundWheelScreen`. Das Oeffnen ist nur ohne bereits aktiven
Minecraft-Screen moeglich. Der Screen gibt den Mauszeiger frei, waehrend die
Voicechat- und Spielsimulation weiterlaufen. Die konfigurierten KeyMappings fuer
Vorwaerts-, Rueckwaerts- und Seitwaertsbewegung sowie Springen, Schleichen und
Sprinten werden waehrenddessen an die Spielsteuerung weitergereicht. Andere
Gameplay- und Screen-Shortcuts bleiben blockiert.

Sound-Rad und Config-Screen verwenden dieselbe Minecraft-nahe UI-Palette aus
`SoundboardUi`: graue pixelige Bevel- und Inset-Flaechen, helle und dunkle
Rahmenkanten sowie Gruen fuer aktive Wiedergabe. Die Seitennavigation des Rads
nutzt Vanilla-Minecraft-Sprites.

Die Sounds werden beim Oeffnen ohne Beachtung der Gross-/Kleinschreibung nach
Dateiname sortiert und in Seiten mit jeweils sechs Eintraegen aufgeteilt. Die
Sektoren beginnen oben und laufen im Uhrzeigersinn. Mausrad sowie linke und rechte
Pfeiltaste wechseln zyklisch zwischen den Seiten. Zusaetzlich erscheinen an den
oberen Aussenseiten des Rads klickbare Pfeile, sofern in der jeweiligen Richtung
eine Seite vorhanden ist. Ein Mausrad-Icon in der Mitte weist bei mehreren Seiten
auf die Scroll-Navigation hin.

Die Mausposition bestimmt ueber ihren Winkel zum Bildschirmzentrum den aktiven
Sektor. Innerhalb der mittleren toten Zone bleibt die Auswahl leer. Ein primaerer
Mausklick startet oder stoppt den markierten Sound, ohne das Rad zu schliessen.
Dadurch koennen waehrend einer Rad-Sitzung mehrere Sounds gestartet werden.
Laufende Sounds sind gruen markiert und tragen ein Wiedergabe-Icon; Loop-Sounds
tragen ein Wiederholen-Icon. Das Loslassen einer Taste der Oeffnen-Combo oder
Escape schliesst das Rad ohne eine weitere Wiedergabe auszuloesen.

## Config-Screen und Nachladen

Der eigene Vanilla-Minecraft-Screen stellt zwei Hauptbereiche bereit:

- **General:** Sound-Rad- und Stop-Combo, Wiedergabe bei stummem Mikrofon, HUD
  sowie Aktionen zum Oeffnen und Nachladen des Sound-Ordners
- **Sound Settings:** Suche, auf- oder absteigende Namenssortierung, Filter fuer
  Keybind- und Loop-Zustand sowie je Datei Key Combo, Loop und Verstaerkung

Widgets aendern zunaechst nur einen lokalen `ConfigDraft`. `Apply` uebernimmt den
Entwurf in die aktive Konfiguration, speichert die JSON-Datei und laedt Sound-
sowie globale Key-Combo-Zustaende neu. `Done` fuehrt dieselben Schritte aus und
schliesst den Screen. `Cancel` und Escape verwerfen den Entwurf.

Beim Oeffnen des externen Sound-Ordners wird eine Aktion fuer den naechsten
Fensterfokus vorgemerkt. Nach der Rueckkehr zu Minecraft werden Konfiguration,
Dateiliste und ein gegebenenfalls noch offener Config-Screen neu aufgebaut.

## HUD

Das HUD-Element wird vor dem Vanilla-Chat registriert. Wenn HUD und Option aktiv
sind, Simple Voice Chat verbunden ist und Sounds laufen, zeigt es rechts unten
die Ueberschrift `Currently Playing`, alle Dateinamen und gegebenenfalls den
Zusatz `Looping` an.
