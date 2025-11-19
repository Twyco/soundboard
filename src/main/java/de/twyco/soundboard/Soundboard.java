package de.twyco.soundboard;

import de.twyco.soundboard.client.GlobalKeybinds;
import de.twyco.soundboard.client.hud.SoundboardHudRenderer;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.keybinding.KeyComboManager;
import de.twyco.soundboard.util.sound.SoundManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Soundboard implements ModInitializer {
    public static final String MOD_ID = "soundboard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[SoundboardConfig/onInitialize] Initializing Soundboard");
        SoundboardConfig.init();
        KeyComboManager.init();
        SoundManager.init();
        GlobalKeybinds.init();
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(Soundboard.MOD_ID, "sounds_hud"),
                SoundboardHudRenderer::render
        );
    }

}