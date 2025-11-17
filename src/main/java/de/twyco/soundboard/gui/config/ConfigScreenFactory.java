package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.gui.config.categories.GeneralCategoryFactory;
import de.twyco.soundboard.gui.config.categories.SoundsCategoryFactory;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreenFactory {

    private static Screen parent = null;

    public static Screen create(Screen parent) {
        ConfigScreenFactory.parent = parent;
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("gui.soundboard.config.title"));

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
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;
        if (currentScreen == null || currentScreen.getTitle() == null || !currentScreen.getTitle().equals(Text.translatable("gui.soundboard.config.title"))) {
            return;
        }
        client.setScreen(create(parent));
    }

}

