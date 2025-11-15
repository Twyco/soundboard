package de.twyco.soundboard.util.sound;

import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.config.entries.SoundEntry;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        return fileName.endsWith(".mp3")
                || fileName.endsWith(".wav")
                || fileName.endsWith(".ogg");
    }

    private static void registerFileAsSound(Path file) {
        String fileName = file.getFileName().toString();
        String nameWithoutExtension = stripExtension(fileName);

        Sound sound = new Sound(fileName, fileName, file);

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

            if (entry.keyCombo != null && !entry.keyCombo.isEmpty()) {
                KeyCombo combo = KeyCombo.of(
                        "soundboard.play." + sound.getId(),
                        entry.keyCombo.stream().mapToInt(Integer::intValue).toArray()
                );
                updateSoundKeyCombo(sound, combo);
            } else {
                updateSoundKeyCombo(sound, null);
            }
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

    public static void updateSoundKeyCombo(Sound sound, @Nullable KeyCombo newCombo) {
        KeyCombo oldCombo = sound.getKeyCombo();
        if(oldCombo != null) {
            oldCombo.unregister();
        }

        sound.setKeyCombo(newCombo);
        if(newCombo != null) {
            newCombo.onPress(c -> sound.play());
        }
    }

    public static void playSound(@NotNull Sound sound) {
        LOG.info("[SoundManager/playSound] Start playing sound [name={}, amplifier={}, loop={}]", sound.getName(), sound.getAmplifier(), sound.isLoop());
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
            LOG.info("[SoundManager/openSoundsFolder] Opened sounds directory '{}'", soundsDir.toAbsolutePath());
        } catch (Exception e) {
            LOG.error("[SoundManager/openSoundsFolder] Failed to open sounds directory '{}': {}",
                    soundsDir.toAbsolutePath(), e.getMessage());
        }
    }
}
