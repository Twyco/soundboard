package de.twyco.soundboard.mixin;

import de.twyco.soundboard.util.keybinding.KeyComboManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void soundboard$onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        boolean consumed = KeyComboManager.handleRawKeyEvent(action, input);

        if (consumed) {
            ci.cancel();
        }
    }

}
