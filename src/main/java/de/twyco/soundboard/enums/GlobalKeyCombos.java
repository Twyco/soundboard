package de.twyco.soundboard.enums;

public enum GlobalKeyCombos {

    SOUND_STOP_ALL("soundboard.sounds.stop_all", "key.soundboard.stop_all");

    private final String id;
    private final String translationKey;

    GlobalKeyCombos(String id, String translationKey) {
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
