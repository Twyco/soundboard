package de.twyco.soundboard.gui.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class ConfigScreenFactory {

    private ConfigScreenFactory() {
    }

    public static Screen create(Screen parent) {
        return new SoundboardConfigScreen(parent);
    }

    public static void reloadConfigScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof SoundboardConfigScreen screen) {
            screen.reloadFromConfig();
        }
    }
}

