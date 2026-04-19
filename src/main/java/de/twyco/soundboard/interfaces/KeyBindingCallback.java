package de.twyco.soundboard.interfaces;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface KeyBindingCallback {
    void handle(Minecraft client);
}
