package de.twyco.soundboard.modImplementations.simpleVoicechatApi.util;

import de.twyco.soundboard.util.sound.Sound;

public class PlayingSound {
    public final String soundId;
    public final String displayName;
    public final short[] samples;
    public int index = 0;

    public final boolean loop;
    public final float gain;

    public PlayingSound(Sound sound, short[] samples) {
        this.soundId = sound.getId();
        this.displayName = sound.getName();
        this.samples = samples;
        this.loop = sound.isLoop();
        this.gain = Math.max(0f, Math.min(sound.getAmplifier() / 100.0f, 3f));
    }

    public boolean isFinished() {
        return !loop && index >= samples.length;
    }

}
