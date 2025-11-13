package de.twyco.soundboard.util.keybinding;

import de.twyco.soundboard.enums.KeyComboEventType;
import de.twyco.soundboard.interfaces.KeyComboCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.*;

public class KeyComboManager {

    private static final Map<KeyCombo, EnumMap<KeyComboEventType, List<KeyComboCallback>>> listeners = new HashMap<>();
    private static final Map<KeyCombo, Boolean> pressedStates = new HashMap<>();

    private static final List<Runnable> pendingActions = new ArrayList<>();

    private KeyComboManager() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.isPaused()) {
                return;
            }

            for (KeyCombo combo : listeners.keySet()) {
                boolean nowPressed = combo.allKeysPressed();
                boolean wasPressed = Boolean.TRUE.equals(pressedStates.get(combo));

                KeyComboEventType eventType = null;

                if (nowPressed && !wasPressed) {
                    eventType = KeyComboEventType.PRESS;
                } else if (nowPressed && wasPressed) {
                    eventType = KeyComboEventType.HOLD;
                } else if(!nowPressed && wasPressed) {
                    eventType = KeyComboEventType.RELEASE;
                }

                if(eventType != null) {
                    fireEvent(combo, eventType);
                }

                pressedStates.put(combo, nowPressed);
            }

            if(!pendingActions.isEmpty()) {
                pendingActions.forEach(Runnable::run);
                pendingActions.clear();
            }
        });
    }

    public static void register(KeyCombo combo, KeyComboEventType eventType, KeyComboCallback callback) {
        pendingActions.add(() -> {
            EnumMap<KeyComboEventType, List<KeyComboCallback>> byType =
                    listeners.computeIfAbsent(combo, c -> new EnumMap<>(KeyComboEventType.class));

            List<KeyComboCallback> callbacks =
                    byType.computeIfAbsent(eventType, t ->   new ArrayList<>());

            callbacks.add(callback);

            pressedStates.putIfAbsent(combo, false);
        });
    }

    public static void unregister(KeyCombo combo) {
        pendingActions.add(() -> {
            listeners.remove(combo);
            pressedStates.remove(combo);
        });
    }

    public static void unregister(KeyCombo combo, KeyComboEventType eventType) {
        pendingActions.add(() -> {
            EnumMap<KeyComboEventType, List<KeyComboCallback>> byType = listeners.get(combo);
            if(byType == null) return;

            byType.remove(eventType);

            if(byType.isEmpty()) {
                listeners.remove(combo);
                pressedStates.remove(combo);
            }
        });
    }

    private static void fireEvent(KeyCombo combo, KeyComboEventType eventType) {
        EnumMap<KeyComboEventType, List<KeyComboCallback>> byType = listeners.get(combo);
        if (byType == null) {
            return;
        }

        List<KeyComboCallback> callbacks = byType.get(eventType);
        if (callbacks == null || callbacks.isEmpty()) {
            return;
        }

        for(KeyComboCallback callback : callbacks) {
            callback.handle(combo);
        }
    }

    public static void onPress(KeyCombo combo, KeyComboCallback callback) {
        register(combo, KeyComboEventType.PRESS, callback);
    }

    public static void onHold(KeyCombo combo, KeyComboCallback callback) {
        register(combo, KeyComboEventType.HOLD, callback);
    }

    public static void onRelease(KeyCombo combo, KeyComboCallback callback) {
        register(combo, KeyComboEventType.RELEASE, callback);
    }
}
