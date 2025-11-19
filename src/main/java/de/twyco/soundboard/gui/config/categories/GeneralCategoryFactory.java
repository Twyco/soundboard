package de.twyco.soundboard.gui.config.categories;

import de.twyco.soundboard.client.GlobalKeybinds;
import de.twyco.soundboard.enums.GlobalKeybind;
import de.twyco.soundboard.gui.config.ConfigScreenFactory;
import de.twyco.soundboard.gui.config.entries.ActionButtonEntry;
import de.twyco.soundboard.gui.config.entries.ActionButtonGridEntry;
import de.twyco.soundboard.gui.config.entries.KeyComboEntry;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.config.entries.GlobalStateEntry;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GeneralCategoryFactory {

    public static void create(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        SoundboardConfigData configData = SoundboardConfig.get();
        GlobalStateEntry defaultState = GlobalStateEntry.fromDefaults();
        GlobalStateEntry state = configData.globalState;
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("gui.soundboard.config.categories.general.title"));

        category.addEntry(
                entryBuilder
                        .startTextDescription(Text.translatable("gui.soundboard.config.categories.general.description"))
                        .build()
        );

        category.addEntry(getKeyComboEntry(GlobalKeybind.OPEN_CONFIG));
        category.addEntry(getKeyComboEntry(GlobalKeybind.SOUND_STOP_ALL));
        category.addEntry(
                entryBuilder.startBooleanToggle(
                                Text.translatable("gui.soundboard.config.state.global.play_while_muted").formatted(Formatting.WHITE),
                                state.playWhileMuted
                        )
                        .setDefaultValue(defaultState.playWhileMuted)
                        .setSaveConsumer(newValue -> state.playWhileMuted = newValue)
                        .setTooltip(Text.translatable("gui.soundboard.config.state.global.play_while_muted.description"))
                        .build()
        );
        category.addEntry(
                entryBuilder.startBooleanToggle(
                                Text.translatable("gui.soundboard.config.state.global.show_sounds_in_hud").formatted(Formatting.WHITE),
                                state.showPlayingSoundsHud
                        )
                        .setDefaultValue(defaultState.showPlayingSoundsHud)
                        .setSaveConsumer(newValue -> state.showPlayingSoundsHud = newValue)
                        .build()
        );
        category.addEntry(
                new ActionButtonGridEntry(
                        new ActionButtonEntry(
                                Text.translatable("gui.soundboard.config.action.open_sounds_folder"),
                                Text.translatable("gui.soundboard.config.action.open_sounds_folder"),
                                SoundManager::openSoundsFolder
                        ),
                        new ActionButtonEntry(
                                Text.translatable("gui.soundboard.config.action.reload"),
                                Text.translatable("gui.soundboard.config.action.reload"),
                                () -> {
                                    SoundboardConfig.load();
                                    SoundManager.reload();
                                    ConfigScreenFactory.reloadConfigScreen();
                                }
                        )
                )
        );
    }

    private static KeyComboEntry getKeyComboEntry(GlobalKeybind keybind) {
        SoundboardConfigData configData = SoundboardConfig.get();
        KeyCombo combo = GlobalKeybinds.getKeyCombos().computeIfAbsent(
                keybind.getId(),
                id -> KeyCombo.empty(keybind.getId())
        );

        return new KeyComboEntry(
                Text.translatable(keybind.getTranslationKey()),
                combo,
                newCombo -> {
                    configData.globalKeyCombos.remove(keybind.getId());
                    configData.globalKeyCombos.put(newCombo.getId(), newCombo.getKeyCodes());
                    SoundboardConfig.save();
                    GlobalKeybinds.reload(keybind);
                },
                0xAAAAAA
        );
    }
}
