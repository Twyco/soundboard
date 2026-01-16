package de.twyco.soundboard.mixin;

import de.twyco.soundboard.util.keybinding.KeyComboManager;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void soundboard$onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        boolean consumed = KeyComboManager.handleRawKeyEvent(action, key);

        if (consumed) {
            ci.cancel();
        }
    }
}
