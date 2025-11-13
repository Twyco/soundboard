package de.twyco.soundboard.util.sound;

import de.twyco.soundboard.Soundboard;
import net.minecraft.client.MinecraftClient;
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
        LOG.info("[SoundManager/init] Initializing Soundboard Manager");
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

        LOG.info("[SoundManager/init] Successfully loaded {} sounds", soundsById.size());
    }

    public static boolean isSupportedSoundFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".mp3")
                || fileName.endsWith(".wav")
                || fileName.endsWith(".ogg");
    }

    public static void registerFileAsSound(Path file) {
        String fileName = file.getFileName().toString();
        String nameWithoutExtension = stripExtension(fileName);

        String id = fileName;

        Sound sound = new Sound(nameWithoutExtension, file);

        soundsById.put(id, sound);

        LOG.info("[SoundManager/registerFileAsSound] Registered sound id='{}', name='{}', file='{}'", id, nameWithoutExtension, file);
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

    public static Sound getSoundById(String id) {
        return soundsById.get(id);
    }

    public static void playSound(Sound sound) {
        LOG.info("[SoundManager/playSound] Start playing sound {}", sound.getPath());
    }
}
