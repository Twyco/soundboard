# Simple Voice Chat Soundboard

A client-side soundboard mod for [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat). Play your own sounds through voice chat so everyone using Simple Voice Chat can hear them.

> This is not an official Simple Voice Chat addon. Please do not ask for support in the Simple Voice Chat Discord server. Instead, use the [GitHub issue tracker](https://github.com/Twyco/soundboard/issues); I will try to help as soon as possible.

## Features

- Play any `.mp3` file through Simple Voice Chat
- Start and stop sounds with keybinds
- Start and stop sounds directly from a customizable sound wheel
- Multi-key keybinds (like `Ctrl + Shift + 1`)
- Bind an unlimited number of sounds
- See active playing sounds in an optional HUD element
- Configure each sound individually:
  - Loop on or off
  - Amplifier (the volume sent to other players)
- Set one global amplifier that is multiplied with every sound's individual amplifier
- Mark favorite sounds and show only those favorites in the sound wheel
- Play sounds while your microphone is muted
- Adjust your local playback volume without changing how loud other players hear the sounds
- Search, sort, and filter large sound collections
- English and German interface

## Dependencies

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat)

## Quick Start

1. Launch the game once so the `sounds` folder is created in your Minecraft instance.
2. Put your `.mp3` files into that folder.
3. Open the Soundboard Config screen with `O` by default.
4. Assign keybinds to your sounds and mark the sounds you want in the sound wheel as favorites.
5. Use the keybinds in singleplayer or multiplayer to start a sound; use the same keybind again to stop it.

## GUI and Usage Guide

### Currently Playing Sounds

All currently playing sounds are shown in a small HUD at the bottom-right of the screen. Looping sounds are marked accordingly. You can disable this indicator in the settings.

![Currently Playing Sounds HUD](https://cdn.modrinth.com/data/cached_images/728858bc2a3c116844b23284c6af170a79cf3f16.png)

### Sound Wheel

Assign a keybind to **Open Sound Wheel** in the General tab. By default, the wheel stays open while you hold the keybind. You can instead enable toggle mode and optionally close the wheel after playing a sound.

The wheel shows up to six alphabetically sorted favorite sounds per page. Change pages with the mouse wheel, the left and right arrow keys, or the clickable page arrows. Hover over a sound and left-click it to start or stop it. Indicators show which sounds are currently playing or set to loop, and you can keep walking, jumping, sneaking, and sprinting while the wheel is open.

![Sound Wheel](https://cdn.modrinth.com/data/cached_images/29fc7790d6455f699ab0c9c847b8192558280507.png)

### Local Soundboard Volume

Change how loud the soundboard is for **you** with the `Soundboard` category in Simple Voice Chat's **Adjust Volumes** menu. This only affects local playback and does not change the amplifier or how loud other players hear the sounds.

![Adjust Soundboard Volume](https://cdn.modrinth.com/data/cached_images/26a8381811f056c6ce0311a0307c2ec99fa3901f_0.webp)

## Settings

Open the custom Minecraft-style config screen with `O` by default. You can change this key in Minecraft's Controls menu. Use **Apply** to save without closing the screen, or **Done** to save and close it.

### General

The **General** tab contains the global options and keybinds. Its persistent, collapsible sections let you:

- Bind **Stop all currently playing sounds**
- Set the global sound amplifier applied to every sound
- Allow sounds to play while your microphone is muted
- Show or hide the currently playing sounds HUD
- Open the sound folder or reload its files
- Bind the sound wheel and configure its toggle and close-on-play behavior

![General Settings](https://cdn.modrinth.com/data/cached_images/2d73ee956c29f67aa6c347c7d100a4227e865f9d.png)

### Sound Settings

The **Sound Settings** tab lists all `.mp3` files detected in your `sounds` folder. Search by name, sort from A-Z or Z-A, and filter sounds by favorite status, keybind, or looping.

Each sound can be customized individually:

- Keybind (multiple keys supported)
- Favorite star for inclusion in the sound wheel
- Loop on or off
- Amplifier (the volume sent to other players)

![Sound Settings](https://cdn.modrinth.com/data/cached_images/05178304cde87770a5d89998015d88d6c289d3ca.png)
