package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SoundboardConfigScreenFactory {

    public static Screen create(Screen parent) {
        SoundboardConfigData configData = SoundboardConfig.get();
        SoundboardConfigData defaultData = SoundboardConfigData.createDefault();

        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("gui.soundboard.config.title"));

        builder.setSavingRunnable(SoundboardConfig::save);

        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("gui.soundboard.config.category.general.title"));

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.translatable("gui.soundboard.config.category.general.enabled"), configData.enabled)
                .setDefaultValue(defaultData.enabled)
                .setSaveConsumer(newValue -> configData.enabled = newValue)
                .build()
        );

        general.addEntry(entryBuilder
                .startIntSlider(Text.translatable("gui.soundboard.config.category.general.defaultAmplifier"), configData.defaultAmplifier, 0, 300)
                .setDefaultValue(defaultData.defaultAmplifier)
                .setSaveConsumer(newValue -> configData.defaultAmplifier = newValue)
                .build()
        );

        return builder.build();
    }

}

