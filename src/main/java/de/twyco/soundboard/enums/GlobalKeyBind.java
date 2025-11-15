package de.twyco.soundboard.enums;

import org.jetbrains.annotations.Nullable;

public enum GlobalKeyBind {

    OPEN_CONFIG("soundboard.config.open"),
    SOUND_STOP_ALL("soundboard.sounds.stop_all");

    private final String id;

    GlobalKeyBind(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
