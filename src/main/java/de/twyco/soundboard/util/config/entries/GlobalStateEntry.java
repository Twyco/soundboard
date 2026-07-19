package de.twyco.soundboard.util.config.entries;

public class GlobalStateEntry {
    private static final int DEFAULT_SOUND_AMPLIFIER = 100;
    private static final int MAX_SOUND_AMPLIFIER = 300;

    public boolean playWhileMuted;
    public boolean showPlayingSoundsHud;
    public boolean toggleSoundWheel;
    public boolean closeSoundWheelOnPlay;
    public Integer soundAmplifier;
    public Boolean generalCategoryExpanded;
    public Boolean soundWheelCategoryExpanded;

    public GlobalStateEntry(
            boolean playWhileMuted,
            boolean showPlayingSoundsHud,
            boolean toggleSoundWheel,
            boolean closeSoundWheelOnPlay,
            Integer soundAmplifier,
            Boolean generalCategoryExpanded,
            Boolean soundWheelCategoryExpanded
    ) {
        this.playWhileMuted = playWhileMuted;
        this.showPlayingSoundsHud = showPlayingSoundsHud;
        this.toggleSoundWheel = toggleSoundWheel;
        this.closeSoundWheelOnPlay = closeSoundWheelOnPlay;
        setSoundAmplifier(soundAmplifier == null ? DEFAULT_SOUND_AMPLIFIER : soundAmplifier);
        this.generalCategoryExpanded = generalCategoryExpanded;
        this.soundWheelCategoryExpanded = soundWheelCategoryExpanded;
    }

    public int getSoundAmplifier() {
        if (soundAmplifier == null) {
            return DEFAULT_SOUND_AMPLIFIER;
        }
        return Math.max(0, Math.min(soundAmplifier, MAX_SOUND_AMPLIFIER));
    }

    public void setSoundAmplifier(int soundAmplifier) {
        this.soundAmplifier = Math.max(0, Math.min(soundAmplifier, MAX_SOUND_AMPLIFIER));
    }

    public static GlobalStateEntry fromDefaults()
    {
        return new GlobalStateEntry(
                false,
                true,
                false,
                false,
                DEFAULT_SOUND_AMPLIFIER,
                true,
                true
        );
    }
}
