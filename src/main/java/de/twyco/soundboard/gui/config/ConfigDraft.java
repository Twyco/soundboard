package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.config.entries.SoundEntry;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.Sound;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ConfigDraft {

    private boolean playWhileMuted;
    private boolean showPlayingSoundsHud;
    private boolean toggleSoundWheel;
    private boolean closeSoundWheelOnPlay;
    private final Map<String, Set<Integer>> globalKeyCombos = new LinkedHashMap<>();
    private final Map<String, SoundDraft> sounds = new LinkedHashMap<>();

    private ConfigDraft() {
    }

    public static ConfigDraft from(SoundboardConfigData config) {
        ConfigDraft draft = new ConfigDraft();
        draft.playWhileMuted = config.globalState.playWhileMuted;
        draft.showPlayingSoundsHud = config.globalState.showPlayingSoundsHud;
        draft.toggleSoundWheel = config.globalState.toggleSoundWheel;
        draft.closeSoundWheelOnPlay = config.globalState.closeSoundWheelOnPlay;

        config.globalKeyCombos.forEach((id, keyCodes) ->
                draft.globalKeyCombos.put(id, copyKeyCodes(keyCodes))
        );
        config.sounds.forEach((id, entry) ->
                draft.sounds.put(id, SoundDraft.from(entry))
        );
        return draft;
    }

    public void applyTo(SoundboardConfigData config) {
        config.globalState.playWhileMuted = playWhileMuted;
        config.globalState.showPlayingSoundsHud = showPlayingSoundsHud;
        config.globalState.toggleSoundWheel = toggleSoundWheel;
        config.globalState.closeSoundWheelOnPlay = closeSoundWheelOnPlay;

        config.globalKeyCombos.clear();
        globalKeyCombos.forEach((id, keyCodes) ->
                config.globalKeyCombos.put(id, copyKeyCodes(keyCodes))
        );

        sounds.forEach((id, draft) -> {
            SoundEntry entry = config.sounds.computeIfAbsent(id, ignored -> SoundEntry.fromDefaults(config));
            draft.applyTo(entry);
        });
    }

    public boolean isPlayWhileMuted() {
        return playWhileMuted;
    }

    public void setPlayWhileMuted(boolean playWhileMuted) {
        this.playWhileMuted = playWhileMuted;
    }

    public boolean isShowPlayingSoundsHud() {
        return showPlayingSoundsHud;
    }

    public void setShowPlayingSoundsHud(boolean showPlayingSoundsHud) {
        this.showPlayingSoundsHud = showPlayingSoundsHud;
    }

    public boolean isToggleSoundWheel() {
        return toggleSoundWheel;
    }

    public void setToggleSoundWheel(boolean toggleSoundWheel) {
        this.toggleSoundWheel = toggleSoundWheel;
    }

    public boolean isCloseSoundWheelOnPlay() {
        return closeSoundWheelOnPlay;
    }

    public void setCloseSoundWheelOnPlay(boolean closeSoundWheelOnPlay) {
        this.closeSoundWheelOnPlay = closeSoundWheelOnPlay;
    }

    public KeyCombo getGlobalKeyCombo(String id) {
        return toKeyCombo(id, globalKeyCombos.get(id));
    }

    public void setGlobalKeyCombo(KeyCombo combo) {
        globalKeyCombos.put(combo.getId(), copyKeyCodes(combo.getKeyCodes()));
    }

    public SoundDraft getSound(Sound sound, SoundboardConfigData config) {
        return sounds.computeIfAbsent(
                sound.getId(),
                ignored -> SoundDraft.from(SoundEntry.fromDefaults(config))
        );
    }

    private static KeyCombo toKeyCombo(String id, Set<Integer> keyCodes) {
        if (keyCodes == null || keyCodes.isEmpty()) {
            return KeyCombo.empty(id);
        }
        return KeyCombo.of(id, keyCodes.stream().mapToInt(Integer::intValue).toArray());
    }

    private static Set<Integer> copyKeyCodes(Set<Integer> keyCodes) {
        return keyCodes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(keyCodes);
    }

    public static final class SoundDraft {

        private int amplifier;
        private boolean loop;
        private Set<Integer> keyCombo;

        private SoundDraft(int amplifier, boolean loop, Set<Integer> keyCombo) {
            this.amplifier = Math.max(0, Math.min(amplifier, 300));
            this.loop = loop;
            this.keyCombo = copyKeyCodes(keyCombo);
        }

        static SoundDraft from(SoundEntry entry) {
            return new SoundDraft(entry.amplifier, entry.loop, entry.keyCombo);
        }

        void applyTo(SoundEntry entry) {
            entry.amplifier = amplifier;
            entry.loop = loop;
            entry.keyCombo = copyKeyCodes(keyCombo);
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

        public KeyCombo getKeyCombo(String soundId) {
            return toKeyCombo("soundboard.play." + soundId, keyCombo);
        }

        public void setKeyCombo(KeyCombo combo) {
            keyCombo = copyKeyCodes(combo.getKeyCodes());
        }
    }
}
