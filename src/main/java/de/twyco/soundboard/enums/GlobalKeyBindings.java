package de.twyco.soundboard.enums;

public enum GlobalKeyBindings {

    OPEN_CONFIG("soundboard.config.open", "key.soundboard.open_config");

    private final String id;
    private final String translationKey;

    GlobalKeyBindings(String id, String translationKey) {
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
