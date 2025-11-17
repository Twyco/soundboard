package de.twyco.soundboard.util.sound;

import de.twyco.soundboard.util.keybinding.KeyCombo;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Sound {

    private final String id;
    private final String name;
    private final Path path;

    private int amplifier = 100;
    private boolean loop = false;
    @NotNull
    private KeyCombo keyCombo;

    public Sound(String id, String name, Path path, @NotNull KeyCombo keyCombo) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.keyCombo = keyCombo;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = Math.max(0, Math.min(amplifier, 300));
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public @NotNull KeyCombo getKeyCombo() {
        return keyCombo;
    }

    public void setKeyCombo(@NotNull KeyCombo keyCombo) {
        this.keyCombo = keyCombo;
    }

    public void play() {
        SoundManager.playSound(this);
    }
}
