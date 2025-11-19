package de.twyco.soundboard.client.hud;

import de.twyco.soundboard.Soundboard;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;

public class HudService {

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(Soundboard.MOD_ID, "sounds_hud"),
                SoundboardHudRenderer::render
        );
    }

}
