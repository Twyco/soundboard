package de.twyco.soundboard.util.keybinding;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class KeyCombo {
    private final HashSet<Integer> keyCodes;

    public KeyCombo(Set<Integer> keyCodes) {
        this.keyCodes = new HashSet<>(keyCodes);
    }

    public static KeyCombo of(int... keyCodes) {
        HashSet<Integer> s = new HashSet<>();
        for (Integer i : keyCodes) {
            s.add(i);
        }
        return new KeyCombo(s);
    }

    public HashSet<Integer> getKeyCodes() {
        return keyCodes;
    }

    public static KeyCombo empty() {
        return new KeyCombo(Set.of());
    }

    public boolean isEmpty() {
        return keyCodes.isEmpty();
    }

    @Override
    public String toString() {
        if(isEmpty()) {
            return "Not Bound";
        }
        StringBuilder sb = new StringBuilder();
        for(Integer keyCode : keyCodes) {
            if(!sb.isEmpty()) {
                sb.append(" + ");
            }
            InputUtil.Key key =InputUtil.Type.KEYSYM.createFromCode(keyCode);
            String s = key.getTranslationKey();
            if (s != null && !s.isBlank()) {
                s = s.replace("key.keyboard.", "")
                        .replace("left.", "L-")
                        .replace("right.", "R-");
                sb.append(s.toUpperCase().replace(".", " "));
                continue;
            }
            String glfw = GLFW.glfwGetKeyName(keyCode, 0);
            sb.append(glfw != null ? glfw.toUpperCase() : ("KEY_" + keyCode));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        for(Integer keyCode : ((KeyCombo) o).keyCodes) {
            if(!keyCodes.contains(keyCode)) return false;
        }
        return true;
    }

    public boolean allKeysPressed() {
        for(Integer keyCode : keyCodes) {
            if(!KeyHelper.isKeyPressed(keyCode)) return false;
        }
        return true;
    }
}
