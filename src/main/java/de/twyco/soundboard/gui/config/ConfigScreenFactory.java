package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.gui.config.categories.GeneralCategoryFactory;
import de.twyco.soundboard.gui.config.categories.SoundsCategoryFactory;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreenFactory {

    public static Screen create(Screen parent) {
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

}

