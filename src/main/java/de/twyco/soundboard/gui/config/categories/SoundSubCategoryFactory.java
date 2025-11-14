package de.twyco.soundboard.gui.config.categories;

import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.config.entries.SoundEntry;
import de.twyco.soundboard.util.sound.Sound;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SoundSubCategoryFactory {

    public static @NotNull SubCategoryListEntry createSoundCategory(Sound sound, ConfigEntryBuilder entryBuilder) {
        SoundboardConfigData configData = SoundboardConfig.get();
        SoundEntry entry = configData.sounds.computeIfAbsent(
                sound.getId(),
                id -> SoundEntry.fromDefaults(configData)
        );
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Text.literal(sound.getName()));

        builder.add(
                entryBuilder
                        .startBooleanToggle(
                                Text.translatable("gui.soundboard.config.sound.loop"),
                                entry.loop
                        )
                        .setDefaultValue(configData.defaultLoop)
                        .setSaveConsumer(newValue -> entry.loop = newValue)
                        .build()
        );

        builder.add(
                entryBuilder
                        .startIntSlider(
                                Text.translatable("gui.soundboard.config.sound.amplifier"),
                                entry.amplifier,
                                0,
                                300
                        )
                        .setDefaultValue(configData.defaultAmplifier)
                        .setSaveConsumer(newValue -> entry.amplifier = newValue)
                        .build()
        );

        return builder.setExpanded(true).build();
    }
}
