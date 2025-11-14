package de.twyco.soundboard.gui.config.categories;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class GeneralCategoryFactory {

    public static void create(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("gui.soundboard.config.categories.general.title"));

        category.addEntry(
                entryBuilder
                        .startTextDescription(Text.translatable("gui.soundboard.config.categories.general.description"))
                        .build()
        );

        category.addEntry(
                entryBuilder.startTextDescription(Text.literal("TODO Open Menu Keybind")).build()
        );
        category.addEntry(
                entryBuilder.startTextDescription(Text.literal("TODO Open Sounds Folder Button")).build()
        );
        category.addEntry(
                entryBuilder.startTextDescription(Text.literal("(TODO Reload Sounds)")).build()
        );
    }

}
