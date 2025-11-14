package de.twyco.soundboard.util.config;

public class SoundboardConfigData {

    public boolean enabled = true;
    public int defaultAmplifier = 100;

    private SoundboardConfigData() {}

    public static SoundboardConfigData createDefault() {
        SoundboardConfigData configData = new SoundboardConfigData();
        configData.enabled = true;
        configData.defaultAmplifier = 100;
        return configData;
    }

}
