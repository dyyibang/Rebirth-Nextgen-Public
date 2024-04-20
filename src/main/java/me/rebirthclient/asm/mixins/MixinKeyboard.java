package me.rebirthclient.asm.mixins;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.api.util.Wrapper;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Keyboard.class)
public class MixinKeyboard implements Wrapper {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (mc.currentScreen instanceof ClickGuiScreen && action == 1 && Rebirth.MODULE.setBind(key)) {
            return;
        }
        if (action == 1) {
            Rebirth.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            Rebirth.MODULE.onKeyReleased(key);
        }
    }
}
