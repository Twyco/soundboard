package de.twyco.soundboard.gui.config.categories;

import de.twyco.soundboard.util.sound.Sound;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

import static de.twyco.soundboard.gui.config.subcategories.SoundSubCategoryFactory.createSoundCategory;

public class SoundsCategoryFactory {


    public static void create(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("gui.soundboard.config.categories.sounds.title"));

        for (Sound sound : SoundManager.getAllSounds()) {
            category.addEntry(createSoundCategory(sound, entryBuilder));
        }
    }
}
