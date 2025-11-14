package de.twyco.soundboard.interfaces;

import de.twyco.soundboard.util.keybinding.KeyCombo;

@FunctionalInterface
public interface KeyComboCallback {
    void handle(KeyCombo keyCombo);
}
