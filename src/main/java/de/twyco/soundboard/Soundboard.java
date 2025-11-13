package de.twyco.soundboard;

import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.keybinding.KeyComboManager;
import net.fabricmc.api.ModInitializer;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Soundboard implements ModInitializer {
	public static final String MOD_ID = "soundboard";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final KeyCombo COMBO_TEST_KEY_1 = new KeyCombo(Set.of(
			GLFW.GLFW_KEY_LEFT_CONTROL,
			GLFW.GLFW_KEY_C
	));
	public static final KeyCombo COMBO_TEST_KEY_2 = new KeyCombo(Set.of(
			GLFW.GLFW_KEY_LEFT_SHIFT,
			GLFW.GLFW_KEY_C
	));

	@Override
	public void onInitialize() {
		KeyComboManager.init();
		registerKeyCombos();
	}

	private void registerKeyCombos() {
		LOGGER.info("Registering key combos.");
		KeyComboManager.register(COMBO_TEST_KEY_1, combo -> {
			LOGGER.info("COMBO_TEST_KEY_1");
		});
		KeyComboManager.register(COMBO_TEST_KEY_2, combo -> {
			LOGGER.info("COMBO_TEST_KEY_2");
		});
	}
}