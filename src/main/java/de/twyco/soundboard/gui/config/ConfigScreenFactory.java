package de.twyco.soundboard.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ConfigScreenFactory {

    private ConfigScreenFactory() {
    }

    public static Screen create(Screen parent) {
        return new SoundboardConfigScreen(parent);
    }

    public static void reloadConfigScreen() {
        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof SoundboardConfigScreen screen) {
            screen.reloadFromConfig();
        }
    }
}

