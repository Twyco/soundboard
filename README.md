# Simple Voice Chat Soundboard
A soundboard mod for the [Simple Voicechat Mod](https://modrinth.com/plugin/simple-voice-chat). It lets you play sounds through your microphone, so anyone using Simple Voice Chat can hear them.

> This is not an official addon. Please don't go to the Simple Voice Chat discord server for support! Instead, please use [GitHub issues](https://github.com/Twyco/soundboard/issues) for support. I'll try to provide support as soon as possible.

## Features
- Play any `.mp3` file
- Start/stop sounds with keybinds
- Multi-key keybinds (like ctrl + shift + 1)
- Bind unlimited amount of sounds
- Stop all currently playing sounds with a keybind
- HUD element in the bottom-right corner showing all active sounds and whether they are looping
- Per-sound settings:
    - Loop on/off
    - Sound amplifier (per-sound volume sent to others)
- Play sounds others can hear while your microphone is muted
- Adjust your local playback volume without affecting the sound amplifier or how loud others hear the sound

## Dependencies
- [Simple Voicechat](https://modrinth.com/plugin/simple-voice-chat)

## Quick Start
1. Put your `.mp3` files into the `sounds` folder in your Minecraft instance.
2. Open the settings (default `O`) and bind your sounds to keys.
3. Press your keybinds in-game (singleplayer or server) to play sounds.

# GUI (full usage guide)
### Currnetly Playing Sounds
All currnetly playing sounds are shown in a small HUD at the bottom-right of the screen.  
You can disable this indicator in the settings.
![Currently Playing GUI](https://cdn.modrinth.com/data/cached_images/728858bc2a3c116844b23284c6af170a79cf3f16.png)

### Local soundboard volume
You can adjust how loud the soundboard sounds are for **you** by changing the `Soundboard` category in the [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) **Adjust volumes** menu.  
This only affects your local volume and does **not** change how loud other players hear the sounds.
![Adjust volume](https://cdn.modrinth.com/data/cached_images/26a8381811f056c6ce0311a0307c2ec99fa3901f_0.webp)


## Settings
Open the settings menu with the default keybind `O`.
Can be changed in the default Controls Menu.
### General
The **General** tab contains all global settings, including global keybinds (e.g. stop all sounds, toggle HUD, play while muted, open config).
![Settings Menu](https://cdn.modrinth.com/data/cached_images/b8595a56fae5234a9891dedd2effbbb4f0feb3ef.png)

### Sound
The **Sound** tab lists all sounds detected in your `sounds` folder.

The `sounds` folder is automatically created after the first game launch. You can open it from the GUI, or by navigating to your Minecraft instance folder and opening the `sounds` folder.

Each sound can be customized individually:
- Keybind (multi-key combos supported)
- Amplifier (per-sound volume sent to others)
- Loop on/off

![Sound Settings GUI](https://cdn.modrinth.com/data/cached_images/f5c4acab46c0309fec10be58484884875353d2a1.png)