package de.twyco.soundboard.util.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class FocusWatcher {

    private static boolean wasWindowActive = true;

    private FocusWatcher() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(FocusWatcher::onEndClientTick);
    }

    private static void onEndClientTick(Minecraft client) {
        boolean isWindowActive = client.isWindowActive();

        if (!wasWindowActive && isWindowActive) {
            FocusActionScheduler.onWindowFocusChanged(true);
        }

        wasWindowActive = isWindowActive;
    }
}
