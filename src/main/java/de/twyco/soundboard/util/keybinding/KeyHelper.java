package de.twyco.soundboard.util.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public class KeyHelper {

    private KeyHelper() {}

    protected static boolean isKeyPressed(int keyCode) {
        Minecraft client = Minecraft.getInstance();
        return InputConstants.isKeyDown(keyCode);
    }
}
