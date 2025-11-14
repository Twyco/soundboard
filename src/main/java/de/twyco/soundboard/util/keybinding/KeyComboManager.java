package de.twyco.soundboard.util.keybinding;

import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.enums.KeyComboEventType;
import de.twyco.soundboard.interfaces.KeyComboCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.*;

public class KeyComboManager {
    private static final Logger LOG = Soundboard.LOGGER;

    private static class KeyComboState {
        final KeyCombo combo;
        final EnumMap<KeyComboEventType, List<KeyComboCallback>> listeners =
                new EnumMap<>(KeyComboEventType.class);
        boolean pressed = false;

        KeyComboState(KeyCombo combo) {
            this.combo = combo;
        }
    }

    private static final Map<String, KeyComboState> combosById = new HashMap<>();
    private static final List<Runnable> pendingActions = new ArrayList<>();

    private KeyComboManager() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(!pendingActions.isEmpty()) {
                List<Runnable> actions = new ArrayList<>(pendingActions);
                pendingActions.clear();
                actions.forEach(Runnable::run);
            }
        });
    }

    private static void register(KeyCombo combo, KeyComboEventType eventType, KeyComboCallback callback) {
        if(combo.isEmpty()) {
            LOG.warn("[KeyComboManager/register]  Tried to register an empty KeyCombo! KeyComboId='{}'", combo.getId());
            return;
        }
        pendingActions.add(() -> {
            KeyComboState state = combosById.computeIfAbsent(
                    combo.getId(),
                    id -> new KeyComboState(combo)
            );
            List<KeyComboCallback> callbacks =
                    state.listeners.computeIfAbsent(eventType, t -> new ArrayList<>());

            callbacks.add(callback);
        });
    }

    public static void unregister(@NotNull KeyCombo keyCombo) {
        pendingActions.add(() -> combosById.remove(keyCombo.getId()));
    }

    public static void unregister(@NotNull String keyComboId) {
        pendingActions.add(() -> combosById.remove(keyComboId));
    }

    public static void unregister(@NotNull KeyCombo combo, @NotNull KeyComboEventType eventType) {
        pendingActions.add(() -> {
            KeyComboState state = combosById.get(combo.getId());
            if(state == null) return;

            EnumMap<KeyComboEventType, List<KeyComboCallback>> listenersByType = state.listeners;

            listenersByType.remove(eventType);

            if(listenersByType.isEmpty()) {
                combosById.remove(combo.getId());
            }
        });
    }

    public static void unregister(@NotNull String keyComboId, @NotNull KeyComboEventType eventType) {
        pendingActions.add(() -> {
            KeyComboState state = combosById.get(keyComboId);
            if(state == null) return;

            EnumMap<KeyComboEventType, List<KeyComboCallback>> listenersByType = state.listeners;

            listenersByType.remove(eventType);

            if(listenersByType.isEmpty()) {
                combosById.remove(keyComboId);
            }
        });
    }

    private static void fireEvent(String keyComboId, KeyComboEventType eventType) {
        KeyComboState state = combosById.get(keyComboId);
        if (state == null) {
            return;
        }

        List<KeyComboCallback> callbacks = state.listeners.get(eventType);
        if (callbacks == null || callbacks.isEmpty()) {
            return;
        }

        for(KeyComboCallback callback : callbacks) {
            callback.handle(state.combo);
        }
    }

    public static boolean handleRawKeyEvent(int action, KeyInput input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client == null || client.currentScreen != null) {
            return false;
        }
        if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_RELEASE && action != GLFW.GLFW_REPEAT) {
            return false;
        }

        List<KeyComboState> comboStates = findMatchingComboStates(input.key());
        if(comboStates.isEmpty()) {
            return false;
        }

        boolean consumed = false;
        for (KeyComboState state : comboStates) {
            boolean nowPressed = state.combo.allKeysPressed();
            boolean wasPressed = state.pressed;

            KeyComboEventType eventType = null;

            if (nowPressed && !wasPressed) {
                eventType = KeyComboEventType.PRESS;
            } else if (nowPressed) {
                eventType = KeyComboEventType.HOLD;
            } else if (wasPressed) {
                eventType = KeyComboEventType.RELEASE;
            }

            state.pressed = nowPressed;

            if (eventType != null) {
                fireEvent(state.combo.getId(), eventType);
                consumed = true;
                break;
            }
        }
        return consumed;
    }

    private static List<KeyComboState> findMatchingComboStates(int keyCode) {
        List<KeyComboState> matchingComboStates = new ArrayList<>();
        for (KeyComboState comboState : combosById.values()) {
            if (comboState.combo.getKeyCodes().contains(keyCode)) {
                matchingComboStates.add(comboState);
            }
        }
        return matchingComboStates;
    }

    public static void onPress(@NotNull KeyCombo combo, @NotNull KeyComboCallback callback) {
        register(combo, KeyComboEventType.PRESS, callback);
    }

    public static void onHold(@NotNull KeyCombo combo, @NotNull KeyComboCallback callback) {
        register(combo, KeyComboEventType.HOLD, callback);
    }

    public static void onRelease(@NotNull KeyCombo combo, @NotNull KeyComboCallback callback) {
        register(combo, KeyComboEventType.RELEASE, callback);
    }
}
