package de.twyco.soundboard.util.config;

import de.twyco.soundboard.util.config.entries.SoundEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SoundboardConfigData {

    public boolean defaultLoop = false;
    public int defaultAmplifier = 100;
    public Map<String, SoundEntry> sounds = new LinkedHashMap<>();
    public Map<String, Set<Integer>> globalKeyCombos = new LinkedHashMap<>();

    private SoundboardConfigData() {}

    public static SoundboardConfigData createDefault() {
        SoundboardConfigData configData = new SoundboardConfigData();
        configData.defaultLoop = false;
        configData.defaultAmplifier = 100;
        configData.sounds = new LinkedHashMap<>();
        configData.globalKeyCombos = new LinkedHashMap<>();
        return configData;
    }

}
