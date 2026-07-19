# Project structure

## Repository root

```text
.
|-- .github/
|   |-- ISSUE_TEMPLATE/       Issue and feature-request templates
|   `-- workflows/build.yml   Build workflow for pushes and pull requests
|-- docs/                     Technical documentation and release notes
|   `-- changelog/            Changelog policy and one file per mod version
|-- gradle/wrapper/           Branch-specific Gradle wrapper
|-- src/main/java/            Java source code
|-- src/main/resources/       Fabric metadata, mixins, and assets
|-- build.gradle              Plugins, dependencies, and build configuration
|-- gradle.properties         Minecraft, mod, and dependency versions
|-- settings.gradle           Gradle plugin repositories
|-- gradlew / gradlew.bat     Platform-specific Gradle wrappers
`-- LICENSE                   MIT license
```

Generated directories such as `.gradle/`, `build/`, `out/`, and `run/` are not
versioned.

## Java packages

All classes are below `de.twyco.soundboard`.

### Entrypoint

`Soundboard` implements Fabric's `ModInitializer` and starts client services in a
fixed order. It also defines `MOD_ID` and the shared SLF4J logger.

### `client`

- `GlobalKeybinds` registers global actions. Opening config is a Minecraft key
  binding; opening the wheel and stopping sounds are configurable key combos.
- `client.hud.HudService` registers the renderer before Vanilla chat.
- `client.hud.SoundboardHudRenderer` displays active sounds in the bottom-right.

### `enums`

- `GlobalKeyBindings` lists normal Minecraft key bindings.
- `GlobalKeyCombos` lists custom mod key combos.
- `KeyComboEventType` distinguishes `PRESS`, `HOLD`, and `RELEASE`.

### `gui.config`

- `ConfigScreenFactory` creates the custom config screen and forwards reloads to
  an open instance.
- `SoundboardConfigScreen` owns tabs, footer actions, the local draft, and combo
  recording.
- `ConfigDraft` separates unapplied UI values from active runtime configuration.
- `widget.SoundboardConfigList` renders global and per-sound settings as a
  scrollable responsive list.
- `widget.FavoriteButton` renders the interactive empty or filled favorite star.
- `widget.KeyComboButton` owns combo recording state and its change callback.
- `widget.AmplifierSlider` maps 0 through 300 percent to a Minecraft slider.

### `gui.component`

`SoundboardUi` contains the shared Minecraft-inspired palette and drawing helpers
for the config screen and wheel. It owns panel and row rendering, text clipping,
circle and mouse-wheel drawing, and Vanilla page-arrow sprites.

### `gui.soundwheel`

`SoundWheelScreen` renders up to six alphabetically sorted favorite sounds per page. It
owns mouse selection, paging, click playback, movement forwarding, and playing or
loop indicators. Releasing the opening combo closes only the screen.

### `interfaces`

`KeyBindingCallback`, `KeyComboCallback`, and `VoicechatListener` provide named
functional callback types. Voice-chat subpackages specialize listeners for
client and server events. The server listener is currently unused because the
mod is client-side.

### `mixin`

`KeyboardMixin` injects at the start of `KeyboardHandler.keyPress`, forwards the
raw GLFW event to `KeyComboManager`, and cancels further Minecraft processing
when a combo triggers.

### `modImplementations`

- `modMenu.ModMenuApi` exposes `ConfigScreenFactory` to Mod Menu.
- `simpleVoicechatApi.SimpleVoicechatApi` is the voice-chat plugin and registers
  events.
- `simpleVoicechatApi.SimpleVoicechatService` decodes, tracks, and mixes sounds.
- `listener.ClientVoicechatConnectionListener` creates and clears client
  voice-chat state on connect and disconnect.
- `listener.MergeClientSoundListener` delegates audio blocks to the service.
- `util.PlayingSound` stores mutable playback state.

### `util.client`

`FocusWatcher` detects when Minecraft regains focus. `FocusActionScheduler` then
runs pending actions, such as reloading config, sound files, and the open config
screen after the external sound folder closes.

### `util.config`

- `SoundboardConfig` reads and writes JSON with Gson.
- `SoundboardConfigData` is the root config object.
- `entries.GlobalStateEntry` contains global runtime options.
- `entries.SoundEntry` contains persisted settings per file.

### `util.keybinding`

- `KeyBindingManager` processes Minecraft `KeyMapping` objects at tick end.
- `KeyCombo` represents a combo ID and key set.
- `KeyComboManager` tracks combo state and callbacks.
- `KeyHelper` reads current key state from the Minecraft window.

### `util.sound`

- `SoundManager` scans the sound folder, links files to config, and delegates
  playback to Simple Voice Chat.
- `Sound` is the runtime model containing ID, path, amplification, favorite
  status, loop, and key combo.

## Resources

| File | Responsibility |
| --- | --- |
| `fabric.mod.json` | Mod metadata, entrypoints, and required dependencies |
| `soundboard.mixins.json` | Mixin registration and branch-specific Java level |
| `assets/soundboard/lang/en_us.json` | Base English UI text and translation keys |
| `assets/soundboard/lang/de_de.json` | German translation of the same UI keys |
| `assets/soundboard/icon.png` | Mod icon |
