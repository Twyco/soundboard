package de.twyco.soundboard.util.config.entries;

public class GlobalStateEntry {
    public boolean playWhileMuted;
    public boolean showPlayingSoundsHud;
    public boolean toggleSoundWheel;
    public boolean closeSoundWheelOnPlay;
    public Boolean generalCategoryExpanded;
    public Boolean soundWheelCategoryExpanded;

    public GlobalStateEntry(
            boolean playWhileMuted,
            boolean showPlayingSoundsHud,
            boolean toggleSoundWheel,
            boolean closeSoundWheelOnPlay,
            Boolean generalCategoryExpanded,
            Boolean soundWheelCategoryExpanded
    ) {
        this.playWhileMuted = playWhileMuted;
        this.showPlayingSoundsHud = showPlayingSoundsHud;
        this.toggleSoundWheel = toggleSoundWheel;
        this.closeSoundWheelOnPlay = closeSoundWheelOnPlay;
        this.generalCategoryExpanded = generalCategoryExpanded;
        this.soundWheelCategoryExpanded = soundWheelCategoryExpanded;
    }

    public static GlobalStateEntry fromDefaults()
    {
        return new GlobalStateEntry(
                false,
                true,
                false,
                false,
                true,
                true
        );
    }
}
