package de.twyco.soundboard.util.config.entries;

public class GlobalStateEntry {
    public boolean playWhileMuted;
    public boolean showPlayingSoundsHud;

    public GlobalStateEntry(
            boolean playWhileMuted,
            boolean showPlayingSoundsHud
    ) {
        this.playWhileMuted = playWhileMuted;
        this.showPlayingSoundsHud = showPlayingSoundsHud;
    }

    public static GlobalStateEntry fromDefaults()
    {
        return new GlobalStateEntry(
                false,
                true
        );
    }
}
