package de.twyco.soundboard.gui.config.categories;

import de.twyco.soundboard.gui.config.ConfigScreenFactory;
import de.twyco.soundboard.gui.config.entries.ActionButtonEntry;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class GeneralCategoryFactory {

    public static void create(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, Screen parent) {
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
                new ActionButtonEntry(
                        Text.translatable("gui.soundboard.config.action.open_sounds_folder"),
                        Text.translatable("gui.soundboard.config.action.open_sounds_folder"),
                        SoundManager::openSoundsFolder
                )
        );
        category.addEntry(
                new ActionButtonEntry(
                        Text.translatable("gui.soundboard.config.action.reload"),
                        Text.translatable("gui.soundboard.config.action.reload"),
                        () -> {
                            SoundboardConfig.load();
                            SoundManager.reload();
                            MinecraftClient client = MinecraftClient.getInstance();
                            client.setScreen(ConfigScreenFactory.create(parent));
                        }
                )
        );
    }

}
