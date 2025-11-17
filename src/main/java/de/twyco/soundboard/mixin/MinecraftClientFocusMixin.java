package de.twyco.soundboard.mixin;

import de.twyco.soundboard.util.client.FocusActionScheduler;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientFocusMixin {

    @Inject(method = "onWindowFocusChanged", at = @At("TAIL"))
    private void soundboard$onWindowFocusChanged(boolean hasFocus, CallbackInfo ci) {
        FocusActionScheduler.onWindowFocusChanged(hasFocus);
    }
}
