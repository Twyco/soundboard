package de.twyco.soundboard.util.sound;

import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.gui.config.ConfigScreenFactory;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatApi;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.util.client.FocusActionScheduler;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.config.entries.SoundEntry;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class SoundManager {
    private static final Logger LOG = Soundboard.LOGGER;

    private static final Map<String, Sound> soundsById = new HashMap<>();

    private SoundManager() {
    }

    public static void init() {
        LOG.info("[SoundManager/init] Initializing Sound Manager");
        reload();
    }

    public static void reload() {
        soundsById.clear();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        Path runDir = client.runDirectory.toPath();
        Path soundsDir = runDir.resolve("sounds");
        if (!Files.exists(soundsDir)) {
            LOG.info("[SoundManager/init] Creating sounds directory");
            try {
                Files.createDirectory(soundsDir);
            } catch (IOException e) {
                LOG.error("[SoundManager/init] Error creating sounds directory: {}", e.getMessage());
                return;
            }
        }

        LOG.info("[SoundManager/init] Loading sounds from '{}'", soundsDir.toAbsolutePath());

        try (Stream<Path> stream = Files.list(soundsDir)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(SoundManager::isSupportedSoundFile)
                    .forEach(SoundManager::registerFileAsSound);
        } catch (IOException e) {
            LOG.error("[SoundManager/init] Error reading sounds from '{}': {}", soundsDir.toAbsolutePath(),  e.getMessage());
        }

        applyConfigToSounds(SoundboardConfig.get());

        LOG.info("[SoundManager/init] Successfully loaded {} sounds", soundsById.size());
    }

    private static boolean isSupportedSoundFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".mp3");
// TODO not implemented yet
//                || fileName.endsWith(".wav");
    }

    private static void registerFileAsSound(Path file) {
        String fileName = file.getFileName().toString();
        String nameWithoutExtension = stripExtension(fileName);

        Sound sound = new Sound(fileName, fileName, file, KeyCombo.empty("soundboard.play." + fileName));

        soundsById.put(fileName, sound);

        LOG.info("[SoundManager/registerFileAsSound] Registered sound id='{}', name='{}', file='{}'", fileName, nameWithoutExtension, file);
    }

    public static void applyConfigToSounds(SoundboardConfigData config) {
        for (Sound sound : getAllSounds()) {
            SoundEntry entry = config.sounds.get(sound.getId());
            if (entry == null) {
                entry = SoundEntry.fromDefaults(config);
                config.sounds.put(sound.getId(), entry);
                SoundboardConfig.save();
            }

            sound.setAmplifier(entry.amplifier);
            sound.setLoop(entry.loop);

            KeyCombo combo;

            if (entry.keyCombo != null && !entry.keyCombo.isEmpty()) {
                combo = KeyCombo.of(
                        "soundboard.play." + sound.getId(),
                        entry.keyCombo.stream().mapToInt(Integer::intValue).toArray()
                );
            } else {
                combo = KeyCombo.empty("soundboard.play." + sound.getId());
            }
            updateSoundKeyCombo(sound, combo);
        }
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    public static Collection<Sound> getAllSounds() {
        return Collections.unmodifiableCollection(soundsById.values());
    }

    public static void updateSoundKeyCombo(Sound sound, @NotNull KeyCombo newCombo) {
        sound.getKeyCombo().unregister();

        sound.setKeyCombo(newCombo);
        newCombo.onPress(c -> sound.play());
    }

    public static void playSound(@NotNull Sound sound) {
        LOG.info("[SoundManager/playSound] Start playing sound [name={}, amplifier={}, loop={}]", sound.getName(), sound.getAmplifier(), sound.isLoop());
        if(SimpleVoicechatApi.isAvailable()) {
            SimpleVoicechatService.playSound(sound);
        } else {
            LOG.warn("[SoundManager/playSound] Simple Voice Chat not available, skipping voice playback");
        }
    }

    public static void openSoundsFolder() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        Path runDir = client.runDirectory.toPath();
        Path soundsDir = runDir.resolve("sounds");

        try {
            Files.createDirectories(soundsDir);
        } catch (IOException e) {
            LOG.error("[SoundManager/openSoundsFolder] Failed to create sounds directory: {}", e.getMessage());
            return;
        }

        try {
            Util.getOperatingSystem().open(soundsDir.toFile());
            FocusActionScheduler.addActionNextFocus(() -> {
                SoundboardConfig.load();
                SoundManager.reload();
                ConfigScreenFactory.reloadConfigScreen();
            });
            LOG.info("[SoundManager/openSoundsFolder] Opened sounds directory '{}'", soundsDir.toAbsolutePath());
        } catch (Exception e) {
            LOG.error("[SoundManager/openSoundsFolder] Failed to open sounds directory '{}': {}",
                    soundsDir.toAbsolutePath(), e.getMessage());
        }
    }

    public static void stopAllSounds() {
        LOG.info("[SoundManager/stopAllSounds] Stopping all sounds");
        if(SimpleVoicechatApi.isAvailable()) {
            SimpleVoicechatService.stopAllSounds();
        } else {
            LOG.warn("[SoundManager/stopAllSounds] Simple Voice Chat not available, skipping voice playback");
        }
    }
}
