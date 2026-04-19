package de.twyco.soundboard.util.keybinding;

import de.twyco.soundboard.interfaces.KeyBindingCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyBindingManager {

    private static final Map<@NotNull KeyMapping, @NotNull KeyBindingCallback> keyBindings = new HashMap<>();
    private static final List<Runnable> pendingActions = new ArrayList<>();

    private KeyBindingManager() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(!pendingActions.isEmpty()) {
                List<Runnable> actions = new ArrayList<>(pendingActions);
                pendingActions.clear();
                actions.forEach(Runnable::run);
            }
            for (KeyMapping keyBinding : keyBindings.keySet()) {
                if(keyBinding.consumeClick()) {
                    keyBindings.get(keyBinding).handle(client);
                }
            }
        });
    }

    public static void register(@NotNull KeyMapping keyBinding, @NotNull KeyBindingCallback callback) {
        pendingActions.add(() -> keyBindings.put(keyBinding, callback));
    }
}
