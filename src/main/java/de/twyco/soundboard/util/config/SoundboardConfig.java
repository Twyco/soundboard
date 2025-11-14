package de.twyco.soundboard.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.twyco.soundboard.Soundboard;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SoundboardConfig {
    private static Logger LOG = Soundboard.LOGGER;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static SoundboardConfigData configData;

    public static SoundboardConfigData get() {
        return configData;
    }

    public static void init() {
        LOG.info("[SoundboardConfig/init] Initializing Config");
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        Path configDir = client.runDirectory.toPath().resolve("config").resolve(Soundboard.MOD_ID);
        Path configFile = configDir.resolve("config.json");

        if(!Files.exists(configFile)) {
            LOG.info("[SoundboardConfig/init] Creating default config file");
            configData = SoundboardConfigData.createDefault();
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                LOG.error("[SoundboardConfig/init] Failed to create config dir: {}", e.getMessage());
                return;
            }

            try (Writer writer = Files.newBufferedWriter(configFile)) {
                gson.toJson(configData, writer);
            } catch (IOException e) {
                LOG.error("[SoundboardConfig/init] Failed to save config: {}", e.getMessage());
            }
            return;
        }
        load();
    }

    public static void load() {
        LOG.info("[SoundboardConfig/load] Loading Config");
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        Path configDir = client.runDirectory.toPath().resolve("config").resolve(Soundboard.MOD_ID);
        Path configFile = configDir.resolve("config.json");

        if(!Files.exists(configFile)) {
            LOG.error("[SoundboardConfig/load] Unable to find config file: {}", configFile);
            return;
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            SoundboardConfigData data = gson.fromJson(reader, SoundboardConfigData.class);
            if(data != null) {
                configData = data;
            }else {
                configData = SoundboardConfigData.createDefault();
            }
        }catch (IOException e) {
            LOG.error("[SoundboardConfig/load] Unable to read config file: {}; {}", configFile, e.getMessage());
            LOG.warn("[SoundboardConfig/load] Continuing with default config");
            configData = SoundboardConfigData.createDefault();
        }
    }

    public static void save() {
        LOG.info("[SoundboardConfig/save] Saving Config");
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        Path configDir = client.runDirectory.toPath().resolve("config").resolve(Soundboard.MOD_ID);
        Path configFile = configDir.resolve("config.json");

        if(!Files.exists(configDir)) {
            LOG.warn("[SoundboardConfig/save] Unable to find config dir: {}", configDir);
            try {
                LOG.warn("[SoundboardConfig/save] Creating new config dir: {}", configDir);
                Files.createDirectories(configDir);
            } catch (IOException e) {
                LOG.error("[SoundboardConfig/save] Failed to create config dir: {}", e.getMessage());
                return;
            }
        }

        try (Writer writer = Files.newBufferedWriter(configFile)) {
            gson.toJson(configData, writer);
        }catch (IOException e) {
            LOG.error("[SoundboardConfig/save] Failed to save config: {}; {}", configFile, e.getMessage());
        }
    }
}
