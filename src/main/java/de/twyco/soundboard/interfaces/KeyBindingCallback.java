package de.twyco.soundboard.interfaces;

import net.minecraft.client.MinecraftClient;

@FunctionalInterface
public interface KeyBindingCallback {
    void handle(MinecraftClient client);
}
