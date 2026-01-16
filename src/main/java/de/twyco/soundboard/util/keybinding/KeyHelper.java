package de.twyco.soundboard.util.keybinding;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;

public class KeyHelper {

    private KeyHelper() {}

    protected static boolean isKeyPressed(int keyCode) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        return InputUtil.isKeyPressed(window.getHandle(), keyCode);
    }
}
