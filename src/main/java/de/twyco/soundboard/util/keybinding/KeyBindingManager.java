package de.twyco.soundboard.util.keybinding;

import de.twyco.soundboard.interfaces.KeyBindingCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyBindingManager {

    private static final Map<@NotNull KeyBinding, @NotNull KeyBindingCallback> keyBindings = new HashMap<>();
    private static final List<Runnable> pendingActions = new ArrayList<>();

    private KeyBindingManager() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(!pendingActions.isEmpty()) {
                List<Runnable> actions = new ArrayList<>(pendingActions);
                pendingActions.clear();
                actions.forEach(Runnable::run);
            }
            for (KeyBinding keyBinding : keyBindings.keySet()) {
                if(keyBinding.wasPressed()) {
                    keyBindings.get(keyBinding).handle(client);
                }
            }
        });
    }

    public static void register(@NotNull KeyBinding keyBinding, @NotNull KeyBindingCallback callback) {
        pendingActions.add(() -> keyBindings.put(keyBinding, callback));
    }
}
