# Runtime configuration

## Files and directories

All paths are relative to the Minecraft game directory of the current instance.

| Path | Contents |
| --- | --- |
| `sounds/` | User-provided MP3 files |
| `config/soundboard.json` | Global options, key combos, and sound settings |

The mod creates both directories when needed. The sound folder is deliberately
not part of the repository and is not scanned recursively.

## Example configuration

```json
{
  "globalState": {
    "playWhileMuted": false,
    "showPlayingSoundsHud": true,
    "toggleSoundWheel": false,
    "closeSoundWheelOnPlay": false,
    "generalCategoryExpanded": true,
    "soundWheelCategoryExpanded": true
  },
  "defaultLoop": false,
  "defaultAmplifier": 100,
  "sounds": {
    "example.mp3": {
      "amplifier": 100,
      "loop": false,
      "keyCombo": [341, 75]
    }
  },
  "globalKeyCombos": {
    "soundboard.sounds.open_wheel": [341, 82],
    "soundboard.sounds.stop_all": [341, 88]
  }
}
```

Values in `keyCombo` and `globalKeyCombos` are GLFW key codes. Their order has no
semantic meaning because they are loaded as a set.

## Fields

### `globalState`

| Field | Default | Meaning |
| --- | --- | --- |
| `playWhileMuted` | `false` | Output sounds while the voice-chat microphone is muted |
| `showPlayingSoundsHud` | `true` | Display active sounds in the HUD |
| `toggleSoundWheel` | `false` | Open and close the sound wheel by pressing its combo instead of holding it |
| `closeSoundWheelOnPlay` | `false` | In toggle mode, close the sound wheel after starting or stopping a sound |
| `generalCategoryExpanded` | `true` | Keep the General section expanded in the config screen |
| `soundWheelCategoryExpanded` | `true` | Keep the Sound Wheel Settings section expanded in the config screen |

Older configuration files do not contain the two expansion fields. Missing
values are treated as `true` so updating the mod preserves the previous layout.

### Global sound defaults

| Field | Default | Meaning |
| --- | --- | --- |
| `defaultLoop` | `false` | Loop default for newly discovered files |
| `defaultAmplifier` | `100` | Amplification default in percent |

These values are templates for new `SoundEntry` objects. They do not
automatically update existing entries.

### `sounds`

Each map key is the exact file name including `.mp3`. Every entry contains:

| Field | Range | Meaning |
| --- | --- | --- |
| `amplifier` | `0` to `300` | Gain in percent; 100 equals a factor of 1.0 |
| `loop` | Boolean | Restart playback after reaching the end |
| `keyCombo` | Set of key codes | Combo that starts or stops the sound |

The runtime `Sound` class also clamps amplification to 0 through 300. Entries for
removed files remain in JSON and are ignored until that file exists again.

### `globalKeyCombos`

| ID | Action |
| --- | --- |
| `soundboard.sounds.open_wheel` | Open and hold the sound wheel |
| `soundboard.sounds.stop_all` | Stop all active sounds |

## Recording a key combo

1. Click the combo button in the config screen.
2. Press every key that should belong to the combo.
3. Release a key to store the keys captured so far.

Backspace, Delete, or Escape clears a combo while it is being recorded. Only one
combo can be recorded at a time.

## Loading and saving

- The first launch creates a default configuration.
- Every launch loads the existing JSON through Gson.
- Newly discovered sound files receive entries automatically.
- `Apply` saves without closing the config screen.
- `Done` saves and returns to the previous screen.
- `Cancel` and Escape discard unapplied changes.
- Combo changes remain in the local draft until `Apply` or `Done`.
- `Reload sound files` reloads both the config and sound folder.

There is currently no explicit schema version or migration layer. New config
fields must therefore use backward-compatible defaults.
