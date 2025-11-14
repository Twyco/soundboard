package de.twyco.soundboard;

import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.keybinding.KeyComboManager;
import de.twyco.soundboard.util.sound.SoundManager;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Soundboard implements ModInitializer {
	public static final String MOD_ID = "soundboard";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[SoundboardConfig/onInitialize] Initializing Soundboard");
		KeyComboManager.init();
		SoundManager.init();
		SoundboardConfig.init();
	}

}