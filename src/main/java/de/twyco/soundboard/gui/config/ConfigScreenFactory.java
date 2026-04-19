package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.gui.config.categories.GeneralCategoryFactory;
import de.twyco.soundboard.gui.config.categories.SoundsCategoryFactory;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreenFactory {

    private static Screen parent = null;

    public static Screen create(Screen parent) {
        ConfigScreenFactory.parent = parent;
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.translatable("gui.soundboard.config.title"));

        builder.setSavingRunnable(() -> {
            SoundboardConfig.save();
            SoundManager.applyConfigToSounds(SoundboardConfig.get());
        });

        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        GeneralCategoryFactory.create(builder, entryBuilder);
        SoundsCategoryFactory.create(builder, entryBuilder);

        return builder.build();
    }

    public static void reloadConfigScreen() {
        Minecraft client = Minecraft.getInstance();
        Screen currentScreen = client.screen;
        if (currentScreen == null || !currentScreen.getTitle().equals(Component.translatable("gui.soundboard.config.title"))) {
            return;
        }
        client.setScreen(create(parent));
    }

}

