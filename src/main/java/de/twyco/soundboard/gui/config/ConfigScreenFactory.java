package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.sound.Sound;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static de.twyco.soundboard.gui.config.categories.SoundSubCategoryFactory.createSoundCategory;

public class ConfigScreenFactory {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("gui.soundboard.config.title"));

        builder.setSavingRunnable(() -> {
            SoundboardConfig.save();
            SoundManager.applyConfigToSounds(SoundboardConfig.get());
        });

        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        ConfigCategory soundsCat = builder.getOrCreateCategory(Text.translatable("gui.soundboard.config.category.sounds"));

        for (Sound sound : SoundManager.getAllSounds()) {
            soundsCat.addEntry(createSoundCategory(sound, entryBuilder));
        }

        return builder.build();
    }

}

