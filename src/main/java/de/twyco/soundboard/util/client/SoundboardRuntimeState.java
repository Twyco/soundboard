package de.twyco.soundboard.util.client;

public class SoundboardRuntimeState {

    private static boolean playWhileMuted = false;
    private static boolean showPlayingSoundsHud = true;

    private SoundboardRuntimeState() {
    }

    public static boolean isPlayWhileMuted() {
        return playWhileMuted;
    }

    public static void setPlayWhileMuted(boolean playWhileMuted) {
        SoundboardRuntimeState.playWhileMuted = playWhileMuted;
    }

    public static boolean isShowPlayingSoundsHud() {
        return showPlayingSoundsHud;
    }

    public static void setShowPlayingSoundsHud(boolean showPlayingSoundsHud) {
        SoundboardRuntimeState.showPlayingSoundsHud = showPlayingSoundsHud;
    }

}
