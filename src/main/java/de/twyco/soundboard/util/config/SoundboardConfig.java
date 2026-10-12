package de.twyco.soundboard.util.config;

import de.twyco.soundboard.Soundboard;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public class SoundboardConfig {
    private static final Logger LOG = Soundboard.LOGGER;
    private static SoundboardConfigStore store;

    public static SoundboardConfigData get() {
        return store.get();
    }

    public static void init() {
        Path configFile = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(Soundboard.MOD_ID + ".json");
        store = new SoundboardConfigStore(configFile, SoundboardConfigData.CURRENT_CONFIG_VERSION, LOG::warn);
        try {
            store.load();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Could not safely load Soundboard configuration " + configFile
                    + ". The original file has been preserved. Fix it before starting Soundboard.", exception);
        }
    }

    public static void load() {
        try {
            store.load();
        } catch (IOException | RuntimeException exception) {
            LOG.error("Could not reload Soundboard config; retaining active settings and blocking saves", exception);
        }
    }

    public static void save() {
        try {
            store.save();
        } catch (IOException exception) {
            LOG.error("Could not safely save Soundboard config; original file preserved", exception);
        }
    }
}
