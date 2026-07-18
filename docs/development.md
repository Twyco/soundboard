# Development and conventions

## Prerequisites

- Git
- The JDK required by the target Minecraft branch
- No global Gradle installation; use the included wrapper

| Minecraft branch | Java | Gradle wrapper | Mappings |
| --- | --- | --- | --- |
| `26.2` | 25 | 9.6.1 | Mojang names provided by the unobfuscated game |
| `26.1.x` | 25 | 9.4.1 | Mojang names provided by the unobfuscated game |
| `1.21.11` | 21 | 9.3.0 | Official Mojang mappings through Loom 1.14 |

The branch-specific `build.gradle`, wrapper, and `soundboard.mixins.json` are the
source of truth if this table needs to be updated.

## Common commands

```bash
./gradlew build
./gradlew runClient
./gradlew clean
```

The remapped mod JAR is created under `build/libs/`. Fabric Loom uses `run/` for
local client instances. Both directories are ignored by Git.

## Version and branch maintenance

Minecraft versions are maintained separately:

- `26.2` is the default and primary development branch.
- `26.1.x` is maintained for Minecraft 26.1.
- `1.21.11` is maintained as an older branch using official Mojang names.
- Other older branches remain version-specific.

New work starts on the current 26.2 branch. Porting it to another Minecraft
version is an explicit follow-up step and must not be mixed invisibly into the
primary implementation.

### Backport strategy

Features should be backported with cherry-picks. Manual file copying is a last
resort when a commit cannot be transferred after reasonable separation.
Cherry-picks preserve feature provenance, reduce omitted files, and make later
backports traceable.

Use this process:

1. Create the target feature branch from its permanent Minecraft version branch,
   for example `26.1.x-ui-rework` from `26.1.x`.
2. Identify only feature commits after the completed source-version base, for
   example with `git log --first-parent --reverse 26.2..26.2-ui-rework`.
3. Cherry-pick those commits in the same order. For a merge commit that is itself
   part of the feature stack, use its first-parent side as the mainline, for
   example `git cherry-pick -m 1 <commit>`.
4. Do not copy Minecraft port commits that precede the source base. Do copy the
   `mod_version` release commit when the target provides the same mod release.
5. During conflicts, retain the target branch's `minecraft_version`, Loader,
   Loom, Fabric API, Mod Menu, Simple Voice Chat, Java, and Minecraft constraints.
   Transfer intentional dependency changes, such as removing Cloth Config.
6. Adapt Minecraft, Fabric, and GUI APIs for the target version. Shared names do
   not imply identical signatures or rendering lifecycles across 1.21.11, 26.1,
   and 26.2.
7. Put target-specific adaptations in a separate commit above the shared feature
   stack. Pull requests target the matching permanent version branch.
8. Before pushing, run `./gradlew build` and verify `gradle.properties` and
   `fabric.mod.json`.

The central version values are in `gradle.properties`:

- `minecraft_version`
- `loader_version`
- `loom_version`
- `fabric_version`
- `modmenu_version`
- `voicechat_api_version`
- `voicechat_version`
- `mod_version`
- `mod_release_type` (`alpha`, `beta`, or empty for a stable release)

Also verify `fabric.mod.json`, the Gradle wrapper, Java settings, and the
compatibility level in `soundboard.mixins.json` during a version update.

## Changelog workflow

The changelog policy is documented in [changelog/README.md](changelog/README.md).
Every user-visible change must update the draft file for the active mod release
in the same feature commit. The draft is finalized when the release is published;
the next release then starts with a new version file. Changelogs are organized by
mod version, not Minecraft version, because supported Minecraft builds share the
same user-facing release.

## Dependencies

| Dependency | Role |
| --- | --- |
| Fabric Loader | Mod loading and entrypoints |
| Fabric API | Client ticks, key mappings, and HUD registration |
| Simple Voice Chat API | MP3 decoding, events, and audio channels |
| Simple Voice Chat | Required runtime mod for local development |
| Mod Menu | Optional entry point to the config screen |
| Gson | JSON persistence supplied by the runtime |

Simple Voice Chat is a required dependency in both behavior and
`fabric.mod.json`. Mod Menu is only suggested; the config screen can also be
opened through the Minecraft key binding.

## Code conventions

### Language and naming

- Packages remain below `de.twyco.soundboard`.
- Classes and enums use `UpperCamelCase`; methods and fields use `lowerCamelCase`;
  constants use `UPPER_SNAKE_CASE`.
- Technical IDs start with `soundboard.` and resources use the `soundboard`
  namespace.
- Translation keys are grouped by feature, such as `gui.soundboard.config.*` and
  `key.soundboard.*`.
- Source code, identifiers, comments, documentation, and base translations use
  English. Other UI languages live in their locale JSON files.

### Structure and ownership

- Fabric-, Mod Menu-, and voice-chat-specific adapters remain in their matching
  `modImplementations` packages.
- Persistence stays in `util.config`; file discovery and runtime sound models stay
  in `util.sound`.
- The config screen works on `ConfigDraft`; widgets do not mutate persisted
  runtime configuration directly.
- GUI widgets contain no audio processing and delegate actions to screens or
  managers.
- Shared colors and drawing primitives live in `gui.component.SoundboardUi`.
  Wheel geometry and screen interaction remain in the relevant screen.
- New global actions are declared in the appropriate enum and mapped centrally
  in `GlobalKeybinds`.
- Non-instantiable service and utility classes have private constructors.

### Nullability and data

- Required parameters use JetBrains `@NotNull` where the surrounding code follows
  that convention; some Minecraft overrides use JSpecify.
- Collection getters must not expose freely mutable internal state.
- Configuration data must remain readable and writable by Gson without custom
  adapters.
- Sound IDs are file names. Changing this scheme requires a config migration.

### Threads and lifecycle

- Minecraft state and GUI changes occur on the client context.
- Combo registration changes are queued until tick end so event processing and
  registration do not mutate the same map concurrently.
- Access to active sounds follows the synchronization strategy of
  `SimpleVoicechatService`, because voice-chat events and the HUD may run in
  different call contexts.
- Disconnecting Simple Voice Chat clears local references and active sounds.

## Extension patterns

### Add a global key combo

1. Add a value with a stable ID and translation key to `GlobalKeyCombos`.
2. Implement its action in `GlobalKeybinds.getKeyComboAction()`.
3. Display it in `SoundboardConfigList`.
4. Add the English translation to `en_us.json` and other supported locales.
5. Update the active release changelog when the change is user-visible.

### Add a Minecraft key binding

1. Add a value to `GlobalKeyBindings`.
2. Implement its callback in `GlobalKeybinds.getKeyBindingAction()`.
3. Add its translation and a sensible default key.
4. Update the active release changelog.

### Add an audio format

1. Accept the extension in `SoundManager.isSupportedSoundFile()`.
2. Add decoding in `SimpleVoicechatService.decodeSoundToPcm()`.
3. Normalize output to 48 kHz, mono, signed 16-bit PCM.
4. Handle invalid channel counts and sample rates clearly.
5. Update UI text, technical documentation, and the active changelog.

## Resources and localization

Visible text belongs in
`src/main/resources/assets/soundboard/lang/<locale>.json`. Java code uses
`Component.translatable(...)` and stable translation keys. User file names and
dynamic values remain literals.

New mixins must be registered in `soundboard.mixins.json`. Keep mixins small and
delegate quickly to a manager or service that can be tested separately.

## Verification

There are currently no project-specific automated tests. Every change requires at
least `./gradlew build` and relevant in-game validation with `./gradlew runClient`.
Audio changes should cover mono and stereo MP3s, different sample rates, muted and
unmuted behavior, loops, and parallel playback.

## Change checklist

1. Work on the current 26.2 feature branch and preserve unrelated local changes.
2. Implement the change in the owning component.
3. Verify translations and config compatibility.
4. Update the active mod-version changelog for user-visible changes.
5. For audio code, verify mute, loop, parallel playback, and stop behavior.
6. Run `./gradlew build`.
7. Update affected technical documentation.
8. Backport to 26.1.x and 1.21.11 with the documented strategy when required.
