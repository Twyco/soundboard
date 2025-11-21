package de.twyco.soundboard.util.config;

import de.twyco.soundboard.enums.GlobalKeybind;
import de.twyco.soundboard.util.config.entries.GlobalStateEntry;
import de.twyco.soundboard.util.config.entries.SoundEntry;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SoundboardConfigData {

    public GlobalStateEntry globalState = GlobalStateEntry.fromDefaults();

    public boolean defaultLoop = false;
    public int defaultAmplifier = 100;
    public Map<String, SoundEntry> sounds = new LinkedHashMap<>();
    public Map<String, Set<Integer>> globalKeyCombos = new LinkedHashMap<>();

    private SoundboardConfigData() {}

    public static SoundboardConfigData createDefault() {
        SoundboardConfigData configData = new SoundboardConfigData();
        configData.globalState = GlobalStateEntry.fromDefaults();

        configData.defaultLoop = false;
        configData.defaultAmplifier = 100;
        configData.sounds = new LinkedHashMap<>();
        configData.globalKeyCombos = new LinkedHashMap<>();
        KeyCombo defaultOpenMenuKeyCombo = KeyCombo.of(
                GlobalKeybind.OPEN_CONFIG.getId(),
                GLFW.GLFW_KEY_LEFT_CONTROL,
                GLFW.GLFW_KEY_LEFT_SHIFT,
                GLFW.GLFW_KEY_T
        );
        configData.globalKeyCombos.put(defaultOpenMenuKeyCombo.getId(), defaultOpenMenuKeyCombo.getKeyCodes());
        return configData;
    }

}
