package me.rebirthclient.asm.mixins;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.impl.SendMessageEvent;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.miscellaneous.SilentDisconnect;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    public abstract void sendChatMessage(String content);

    @Unique
    private boolean ignoreChatMessage;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (ignoreChatMessage) return;
        if (message.startsWith(Rebirth.PREFIX)) {
            Rebirth.COMMAND.command(message.split(" "));
            ci.cancel();
        } else {
            SendMessageEvent event = new SendMessageEvent(message);
            Rebirth.EVENT_BUS.post(event);
            if (event.isCancel()) {
                ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
                ignoreChatMessage = true;
                sendChatMessage(event.message);
                ignoreChatMessage = false;
                ci.cancel();
            }
        }
    }
    @Inject(method = "onDisconnected", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(Text reason, CallbackInfo ci) {
        if (Wrapper.mc.player != null && Wrapper.mc.world != null && SilentDisconnect.INSTANCE.isOn()) {
            CommandManager.sendChatMessage("\u00a74[!] \u00a7cYou get kicked! reason: \u00a77" + reason.getString());
            ci.cancel();
        }
    }
}
