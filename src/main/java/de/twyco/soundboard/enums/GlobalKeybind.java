package de.twyco.soundboard.enums;

public enum GlobalKeybind {

    OPEN_CONFIG("soundboard.config.open", "gui.soundboard.config.keybind.global.open_config"),
    SOUND_STOP_ALL("soundboard.sounds.stop_all", "gui.soundboard.config.keybind.global.stop_all");

    private final String id;
    private final String translationKey;

    GlobalKeybind(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String getId() {
        return this.id;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }
}
