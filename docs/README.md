# Soundboard documentation

This documentation describes the current state of **Simple Voice Chat
Soundboard**, a client-side Fabric mod that plays custom sounds through the
Simple Voice Chat API.

## Documents

- [Project structure](project-structure.md): directories, packages, and ownership
- [Architecture](architecture.md): initialization, audio flow, input, and GUI
- [Configuration](configuration.md): runtime files, JSON schema, and behavior
- [Development](development.md): builds, dependencies, and project conventions
- [Changelog](changelog/README.md): release-note policy and published releases

## Project scope

- **Platform:** Fabric, client-side only
- **Voice chat integration:** Simple Voice Chat is required.
- **Maintained Minecraft versions:** 26.2, 26.1.x, and 1.21.11 are maintained
  on separate branches.
- **Primary development branch:** `26.2`
- **Audio formats:** Only MP3 files are supported. More formats may follow later.
- **Playback:** Multiple sounds may play simultaneously. Triggering a running
  sound again stops that sound.
- **Sound wheel:** Holding a configurable global key combo opens a radial picker
  with six alphabetically sorted sounds per page. Primary click starts or stops
  sounds, and playing and looping sounds are marked in the wheel. Player movement
  remains available while the wheel is open.
- **Input:** Triggered sound combos consume their matching Minecraft keyboard
  event.
- **Quality assurance:** There are no project-specific automated tests. Changes
  are validated with Gradle builds and targeted in-game testing.

## Terms

| Term | Meaning |
| --- | --- |
| Key Binding | A normal single-key binding managed by Minecraft |
| Key Combo | A mod-defined combination of one or more GLFW keys |
| Sound Entry | Persisted settings for one audio file |
| Playing Sound | Decoded PCM data and its current playback position |
| Merge Event | Simple Voice Chat event into which the mod mixes audio blocks |
