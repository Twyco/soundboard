package de.twyco.soundboard.client;

import de.twyco.soundboard.enums.GlobalKeybind;
import de.twyco.soundboard.gui.config.ConfigScreenFactory;
import de.twyco.soundboard.interfaces.KeyComboCallback;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.SoundManager;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GlobalKeybinds {

    private static final Map<String, KeyCombo> keyCombos = new HashMap<>();

    private GlobalKeybinds() {
    }

    public static void init() {
        SoundboardConfigData configData = SoundboardConfig.get();
        for (GlobalKeybind keybind : GlobalKeybind.values()) {
            Set<Integer> keyComboEntry = configData.globalKeyCombos.computeIfAbsent(keybind.getId(), k -> Set.of());

            KeyCombo combo;

            if (!keyComboEntry.isEmpty()) {
                combo = KeyCombo.of(keybind.getId(), keyComboEntry.stream().mapToInt(Integer::intValue).toArray());
            } else {
                combo = KeyCombo.empty(keybind.getId());
            }

            keyCombos.put(combo.getId(), combo);
        }
        SoundboardConfig.save();
        reloadAll();
    }

    public static void reloadAll() {
        for (GlobalKeybind keybind : GlobalKeybind.values()) {
            reload(keybind);
        }
    }

    public static void reload(@NotNull GlobalKeybind bind) {
        SoundboardConfigData config = SoundboardConfig.get();
        KeyCombo oldCombo = keyCombos.get(bind.getId());
        if (oldCombo != null) {
            oldCombo.unregister();
            keyCombos.remove(bind.getId());
        }

        Set<Integer> keyComboEntry = config.globalKeyCombos.get(bind.getId());
        KeyCombo combo;
        if (keyComboEntry == null || keyComboEntry.isEmpty()) {
            combo = KeyCombo.empty(bind.getId());
        } else {
            combo = KeyCombo.of(
                    bind.getId(),
                    keyComboEntry.stream().mapToInt(Integer::intValue).toArray()
            );
        }

        keyCombos.put(bind.getId(), combo);
        combo.onPress(getAction(bind));
    }

    public static Map<String, KeyCombo> getKeyCombos() {
        return keyCombos;
    }

    private static KeyComboCallback getAction(@NotNull GlobalKeybind keybind) {
        return switch (keybind) {
            case GlobalKeybind.OPEN_CONFIG -> combo -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null) return;
                client.setScreen(ConfigScreenFactory.create(client.currentScreen));
            };
            case GlobalKeybind.SOUND_STOP_ALL -> combo -> SoundManager.stopAllSounds();
        };
    }
}
