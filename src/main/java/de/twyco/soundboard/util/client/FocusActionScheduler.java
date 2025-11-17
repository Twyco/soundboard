package de.twyco.soundboard.util.client;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FocusActionScheduler {

    private static final Map<UUID, @NotNull Runnable> actions = new HashMap<>();

    private FocusActionScheduler() {}

    public static void addActionNextFocus(@NotNull Runnable action) {
        actions.put(UUID.randomUUID(), action);
    }

    public static void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus || actions.isEmpty()) {
            return;
        }
        List<UUID> actionKeys = new ArrayList<>(actions.keySet());

        for (UUID uuid : actionKeys) {
            Runnable action = actions.remove(uuid);
            action.run();
        }
    }
}
