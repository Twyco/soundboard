package de.twyco.soundboard.util.keybinding;

import de.twyco.soundboard.enums.KeyComboEventType;
import de.twyco.soundboard.interfaces.KeyComboCallback;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class KeyCombo {
    private final String id;
    private final Set<Integer> keyCodes;

    private KeyCombo(@NotNull String id, Set<Integer> keyCodes) {
        this.id = id;
        this.keyCodes = new HashSet<>(keyCodes);
    }

    public static KeyCombo of(@NotNull String id, int... keyCodes) {
        HashSet<Integer> s = new HashSet<>();
        for (int i : keyCodes) {
            s.add(i);
        }
        return new KeyCombo(id, s);
    }

    public static KeyCombo empty(@NotNull String id) {
        return new KeyCombo(id, Set.of());
    }

    public String getId() {
        return id;
    }

    public Set<Integer> getKeyCodes() {
        return Collections.unmodifiableSet(keyCodes);
    }

    public boolean isEmpty() {
        return keyCodes.isEmpty();
    }

    public boolean allKeysPressed() {
        if(keyCodes.isEmpty()) {
            return false;
        }
        for(Integer keyCode : keyCodes) {
            if(!KeyHelper.isKeyPressed(keyCode)) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if(isEmpty()) {
            return "Not Bound";
        }
        StringBuilder sb = new StringBuilder();
        List<Integer> sorted = new ArrayList<>(keyCodes);
        Collections.sort(sorted);
        for (int keyCode : sorted) {
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
        if (!(o instanceof KeyCombo other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void onPress(@NotNull KeyComboCallback actionCallback) {
        KeyComboManager.onPress(this, actionCallback);
    }

    public void onHold(@NotNull KeyComboCallback actionCallback) {
        KeyComboManager.onHold(this, actionCallback);
    }

    public void onRelease(@NotNull KeyComboCallback actionCallback) {
        KeyComboManager.onRelease(this, actionCallback);
    }

    public void unregister() {
        KeyComboManager.unregister(this);
    }

    public void unregister(@NotNull KeyComboEventType  eventType) {
        KeyComboManager.unregister(this, eventType);
    }
}
