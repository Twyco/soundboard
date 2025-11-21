package de.twyco.soundboard.client;

import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.enums.GlobalKeyBindings;
import de.twyco.soundboard.enums.GlobalKeyCombos;
import de.twyco.soundboard.gui.config.ConfigScreenFactory;
import de.twyco.soundboard.interfaces.KeyBindingCallback;
import de.twyco.soundboard.interfaces.KeyComboCallback;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.keybinding.KeyBindingManager;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.SoundManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GlobalKeybinds {

    private static final Map<String, KeyCombo> keyCombos = new HashMap<>();
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(Soundboard.MOD_ID, "general"));

    private GlobalKeybinds() {
    }

    public static void init() {
        SoundboardConfigData configData = SoundboardConfig.get();
        for (GlobalKeyCombos keybind : GlobalKeyCombos.values()) {
            Set<Integer> keyComboEntry = configData.globalKeyCombos.computeIfAbsent(keybind.getId(), k -> Set.of());

            KeyCombo combo;

            if (!keyComboEntry.isEmpty()) {
                combo = KeyCombo.of(keybind.getId(), keyComboEntry.stream().mapToInt(Integer::intValue).toArray());
            } else {
                combo = KeyCombo.empty(keybind.getId());
            }

            keyCombos.put(combo.getId(), combo);
        }
        for (GlobalKeyBindings keybind : GlobalKeyBindings.values()) {
            KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    keybind.getTranslationKey(),
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_O,
                    CATEGORY
            ));
            KeyBindingManager.register(keyBinding, getKeyBindingAction(keybind));
        }
        SoundboardConfig.save();
        reloadAll();
    }

    public static void reloadAll() {
        for (GlobalKeyCombos keybind : GlobalKeyCombos.values()) {
            reloadKeyCombo(keybind);
        }
    }

    public static void reloadKeyCombo(@NotNull GlobalKeyCombos bind) {
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
        combo.onPress(getKeyComboAction(bind));
    }

    public static Map<String, KeyCombo> getKeyCombos() {
        return keyCombos;
    }

    private static KeyComboCallback getKeyComboAction(@NotNull GlobalKeyCombos keybind) {
        return switch (keybind) {
            case GlobalKeyCombos.SOUND_STOP_ALL -> combo -> SoundManager.stopAllSounds();
        };
    }

    private static KeyBindingCallback getKeyBindingAction(@NotNull GlobalKeyBindings keybind) {
        return switch (keybind) {
            case GlobalKeyBindings.OPEN_CONFIG -> combo -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null) return;
                client.setScreen(ConfigScreenFactory.create(client.currentScreen));
            };
        };
    }
}
