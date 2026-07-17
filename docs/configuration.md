# Laufzeitkonfiguration

## Dateien und Verzeichnisse

Alle Pfade beziehen sich auf das Minecraft-Spielverzeichnis der jeweiligen
Instanz.

| Pfad | Inhalt |
| --- | --- |
| `sounds/` | Vom Benutzer bereitgestellte MP3-Dateien |
| `config/soundboard.json` | Globale Optionen, Key Combos und Sound-Einstellungen |

Der Mod erzeugt beide Verzeichnisse bei Bedarf. Der Sound-Ordner ist absichtlich
nicht Teil des Repositorys und wird nicht rekursiv durchsucht.

## Beispielkonfiguration

```json
{
  "globalState": {
    "playWhileMuted": false,
    "showPlayingSoundsHud": true
  },
  "defaultLoop": false,
  "defaultAmplifier": 100,
  "sounds": {
    "example.mp3": {
      "amplifier": 100,
      "loop": false,
      "keyCombo": [
        341,
        75
      ]
    }
  },
  "globalKeyCombos": {
    "soundboard.sounds.stop_all": [
      341,
      88
    ]
  }
}
```

Die Ganzzahlen in `keyCombo` und `globalKeyCombos` sind GLFW-Keycodes. Die
Reihenfolge ist semantisch irrelevant, da sie beim Laden als Menge behandelt
werden.

## Felder

### `globalState`

| Feld | Standard | Bedeutung |
| --- | --- | --- |
| `playWhileMuted` | `false` | Sound auch ausgeben, wenn das Voicechat-Mikrofon stumm ist |
| `showPlayingSoundsHud` | `true` | Aktive Sounds im HUD anzeigen |

### Globale Sound-Defaults

| Feld | Standard | Bedeutung |
| --- | --- | --- |
| `defaultLoop` | `false` | Loop-Standard fuer neu erkannte Dateien |
| `defaultAmplifier` | `100` | Verstaerkungsstandard in Prozent |

Diese Werte dienen beim Anlegen eines bisher unbekannten `SoundEntry` als
Vorlage. Sie veraendern bestehende Eintraege nicht automatisch.

### `sounds`

Der Map-Schluessel ist exakt der Dateiname inklusive `.mp3`. Jeder Eintrag
enthaelt:

| Feld | Bereich | Bedeutung |
| --- | --- | --- |
| `amplifier` | `0` bis `300` | Gain in Prozent; 100 entspricht Faktor 1,0 |
| `loop` | Boolean | Nach dem Ende wieder von vorne beginnen |
| `keyCombo` | Menge von Keycodes | Kombination zum Starten und Stoppen |

Die Laufzeitklasse `Sound` begrenzt die Verstaerkung zusaetzlich auf 0 bis 300.
Konfigurationseintraege entfernter Dateien bleiben im JSON erhalten und werden
ignoriert, solange die Datei fehlt.

### `globalKeyCombos`

Die Map speichert Kombinationen fuer globale Aktionen. Derzeit ist genau eine ID
definiert:

| ID | Aktion |
| --- | --- |
| `soundboard.sounds.stop_all` | Alle laufenden Sounds stoppen |

## Bedienung der Key-Combo-Eingabe

1. Im Config-Screen die Schaltflaeche der gewuenschten Kombination anklicken.
2. Alle gewuenschten Tasten druecken.
3. Beim Loslassen einer Taste wird die bis dahin erfasste Kombination gespeichert.

Backspace, Delete oder Escape waehrend der Aufnahme loeschen die Belegung. Es
kann immer nur eine Kombination gleichzeitig aufgenommen werden.

## Laden und Speichern

- Beim ersten Start wird eine Standardkonfiguration erzeugt.
- Bei jedem Start wird die vorhandene JSON-Datei mit Gson geladen.
- Neu gefundene Sounddateien erhalten automatisch einen Eintrag.
- `Apply` speichert, ohne den Config-Screen zu schliessen.
- `Done` speichert und kehrt zum vorherigen Screen zurueck.
- `Cancel` und Escape verwerfen alle noch nicht angewendeten Aenderungen.
- Key-Combo-Aenderungen bleiben bis `Apply` oder `Done` im lokalen Entwurf.
- "Reload sound files" liest Config und Sound-Ordner erneut ein.

Es gibt derzeit keine explizite Schema-Version oder Migrationsschicht. Neue
Konfigurationsfelder sollten deshalb mit rueckwaertskompatiblen Defaults
eingefuehrt werden.
