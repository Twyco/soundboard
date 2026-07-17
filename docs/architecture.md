# Architecture and runtime flows

## Component overview

```text
KeyboardMixin -> KeyComboManager -> Sound.play() -> SoundManager
Config Screen -> ConfigDraft -> SoundManager
SoundManager -> SimpleVoicechatService
                               |                  |
                               | mergeAudio()     | ClientStaticAudioChannel
                               v                  v
                         voice-chat output     local playback

SoundboardConfig <---- GUI / SoundManager / GlobalKeybinds
SimpleVoicechatService ----> SoundboardHudRenderer
```

Most components are static services. The project has no dependency-injection
container and no custom event bus.

## Initialization

Fabric calls `Soundboard.onInitialize()`. Initialization order matters:

1. `SoundboardConfig.init()` loads or creates `config/soundboard.json`.
2. `KeyComboManager.init()` registers combo tick processing.
3. `KeyBindingManager.init()` registers Minecraft key-binding processing.
4. `SoundManager.init()` scans `sounds/` and applies the configuration.
5. `GlobalKeybinds.init()` registers global actions and persists defaults.
6. `HudService.init()` registers the HUD element.
7. `FocusWatcher.init()` observes window focus.

Separately, Fabric creates `SimpleVoicechatApi` through the `voicechat`
entrypoint. Simple Voice Chat calls its `initialize()` and `registerEvents()`
methods.

## Simple Voice Chat connection

After a successful client connection:

1. The `VoicechatClientApi` is stored in `SimpleVoicechatService`.
2. A volume category named `Soundboard` with ID `soundboardsounds` is registered.
3. A `ClientStaticAudioChannel` with a random UUID is created.
4. The channel is assigned to the Soundboard volume category.

Disconnecting unregisters the category, clears the client API reference, and
stops every active sound.

## Loading sound files

`SoundManager.reload()` clears the runtime list and reads regular files directly
below `<gameDirectory>/sounds`. Subdirectories are not scanned recursively. File
names are accepted when they end in `.mp3`, ignoring case.

The sound ID and display name are both the complete file name including its
extension. The file name is therefore also the stable JSON configuration key.
When a new file is found, the mod creates and saves a `SoundEntry` using the
global defaults.

## Starting, toggling, and stopping sounds

A sound combo registers a `PRESS` callback on `Sound.play()`.
`SimpleVoicechatService` decodes the entire MP3 into a `short[]` and normalizes it
to:

- 48,000 samples per second
- mono
- signed 16-bit PCM samples

Stereo input is converted to mono by averaging both channels. Other channel
counts are rejected. Sample rates other than 48 kHz are resampled linearly.

If the same sound ID is already active, triggering it removes the existing
playback. Otherwise, a new `PlayingSound` is added. Other sound IDs remain active
and are mixed in parallel. The global stop combo clears the complete playback
list.

`PlayingSound` copies loop and gain values when playback starts. Later config
changes do not retroactively affect an already running instance.

## Mixing and playback

`MergeClientSoundEvent` continuously requests blocks of 960 samples, equal to
20 ms at 48 kHz. For each active sound, the service:

1. reads samples from the current position,
2. multiplies each sample by `amplifier / 100`,
3. adds it to the other active sounds,
4. clamps the result to the Java `short` range,
5. advances the playback position, and
6. removes a finished non-looping sound on the next pass.

When `playWhileMuted` is enabled or the voice-chat microphone is not muted, the
mixed block is sent to both destinations:

- `event.mergeAudio(mixed)` adds it to the Simple Voice Chat client stream.
- `ClientStaticAudioChannel.play(mixed)` plays it locally in the Soundboard
  volume category.

When the microphone is muted and `playWhileMuted` is disabled, positions still
advance, but no audio block is output.

## Keyboard input

The project uses two input mechanisms:

| Mechanism | Usage | Processing |
| --- | --- | --- |
| Minecraft `KeyMapping` | Open the config screen | `consumeClick()` at tick end |
| Custom `KeyCombo` | Sounds, sound wheel, and global stop | Raw keyboard mixin event |

A `KeyCombo` contains an ID and an unordered set of GLFW key codes. It is held
when every configured key is currently pressed. Empty combos never trigger.

`KeyComboManager` tracks whether each combo was previously held and derives
`PRESS`, `HOLD`, and `RELEASE` events. Registration changes are queued until the
end of the client tick. Combo callbacks are not triggered while a normal
Minecraft screen is open.

When a combo event triggers, `KeyboardMixin` consumes the raw keyboard event.
This is intentional, but it can conflict with Minecraft or mod key bindings that
use the same keys.

## Sound wheel

The global combo `soundboard.sounds.open_wheel` opens a non-pausing
`SoundWheelScreen` on `PRESS`. It only opens when no other Minecraft screen is
active. By default the combo must remain held; toggle mode instead keeps the
wheel open until the combo is pressed again or Escape is pressed. The screen
releases the mouse cursor while voice chat and game simulation continue.
Configured movement mappings for walking, jumping, sneaking, and sprinting are
forwarded to game controls in either mode. Other gameplay and screen shortcuts
remain blocked.

The wheel and config screen share the Minecraft-inspired palette and rendering
helpers in `SoundboardUi`. Wheel page controls use Vanilla Minecraft sprites.

Sounds are sorted by file name without case sensitivity and split into pages of
six. Sectors start at the top and continue clockwise. The mouse wheel and left or
right arrow keys cycle through pages. Clickable page arrows appear beside the
wheel when a page exists in that direction. A mouse-wheel icon indicates scroll
navigation when multiple pages exist.

The mouse angle relative to the screen center selects a sector. The inner dead
zone has no selection. Primary click starts or stops the selected sound. By
default the wheel stays open so several sounds can be controlled in one session;
the close-on-play option closes it after the click instead.
Playing sounds are green and show a play icon; looping sounds show a loop icon.
Releasing a key from the opening combo or pressing Escape closes the wheel
without triggering additional playback.

## Config screen and reload flow

The custom Vanilla-style screen has two main tabs:

- **General:** collapsible General and Sound Wheel Settings sections. Their
  expansion state is persisted with the other global options. General contains
  the stop combo, play-while-muted behavior, HUD visibility, and folder actions;
  Sound Wheel Settings contains the wheel combo and wheel behavior.
- **Sound Settings:** search, ascending or descending name sorting, bound and
  loop filters, and per-file combo, loop, and amplification settings

Widgets edit a local `ConfigDraft`. `Apply` writes the draft to the active config,
saves JSON, and reloads sound and global combo state. `Done` performs the same
steps and closes the screen. `Cancel` and Escape discard the draft.

Opening the external sound folder schedules an action for the next focus regain.
After Minecraft regains focus, the mod reloads the configuration, file list, and
an open config screen.

## HUD

The HUD element is registered before Vanilla chat. When the HUD option is active,
Simple Voice Chat is connected, and sounds are playing, it shows `Currently
Playing`, every active file name, and a `Looping` suffix where applicable in the
bottom-right corner.
