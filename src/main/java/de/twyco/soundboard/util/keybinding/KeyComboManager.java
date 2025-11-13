package de.twyco.soundboard.util.keybinding;

import de.twyco.soundboard.interfaces.KeyComboListener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyComboManager {

    private static final List<KeyCombo> combos = new ArrayList<KeyCombo>();
    private static final Map<KeyCombo, KeyComboListener> listeners = new HashMap<>();
    private static final Map<KeyCombo, Boolean> comboStates = new HashMap<>();

    private KeyComboManager() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.isPaused()) {
                return;
            }

            for(KeyCombo combo : combos) {
                boolean nowPressed = combo.allKeysPressed();
                boolean wasPressed = Boolean.TRUE.equals(comboStates.get(combo));

                if(nowPressed && !wasPressed) {
                    KeyComboListener listener = listeners.get(combo);
                    if(listener != null) {
                        listener.onComboPressed(combo);
                    }
                }

                comboStates.put(combo, nowPressed);
            }
        });
    }

    public static void register(KeyCombo combo, KeyComboListener listener) {
        combos.add(combo);
        listeners.put(combo, listener);
        comboStates.put(combo, false);
    }

    public static void unregister(KeyCombo combo) {
        combos.remove(combo);
        listeners.remove(combo);
        comboStates.remove(combo);
    }

}
