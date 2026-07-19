package de.twyco.soundboard.util.config.entries;

import de.twyco.soundboard.util.config.SoundboardConfigData;

import java.util.Set;

public class SoundEntry {
    public int amplifier;
    public boolean loop;
    public boolean favorite;
    public Set<Integer> keyCombo;

    public SoundEntry(int amplifier, boolean loop, boolean favorite, Set<Integer> keyCombo) {
        this.amplifier = amplifier;
        this.loop = loop;
        this.favorite = favorite;
        this.keyCombo = keyCombo;
    }

    public static SoundEntry fromDefaults(SoundboardConfigData config) {
        return new SoundEntry(
                config.defaultAmplifier,
                config.defaultLoop,
                false,
                Set.of()
        );
    }
}
