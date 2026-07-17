package de.twyco.soundboard.util.config.entries;

public class GlobalStateEntry {
    public boolean playWhileMuted;
    public boolean showPlayingSoundsHud;
    public boolean toggleSoundWheel;
    public boolean closeSoundWheelOnPlay;

    public GlobalStateEntry(
            boolean playWhileMuted,
            boolean showPlayingSoundsHud,
            boolean toggleSoundWheel,
            boolean closeSoundWheelOnPlay
    ) {
        this.playWhileMuted = playWhileMuted;
        this.showPlayingSoundsHud = showPlayingSoundsHud;
        this.toggleSoundWheel = toggleSoundWheel;
        this.closeSoundWheelOnPlay = closeSoundWheelOnPlay;
    }

    public static GlobalStateEntry fromDefaults()
    {
        return new GlobalStateEntry(
                false,
                true,
                false,
                false
        );
    }
}
