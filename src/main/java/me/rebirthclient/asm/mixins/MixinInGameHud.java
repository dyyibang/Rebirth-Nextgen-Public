package me.rebirthclient.asm.mixins;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.modules.client.Chat;
import me.rebirthclient.mod.modules.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

	@Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.portal.getValue()) ci.cancel();
	}

	@Inject(at = {@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", remap = false, ordinal = 3)}, method = {"render(Lnet/minecraft/client/gui/DrawContext;F)V"})
	private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
		//if (!MinecraftClient.getInstance().options.debugEnabled)
		Rebirth.drawHUD(context, tickDelta);
		Rebirth.MODULE.render2D(context);
	}

	@Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
	private void onClear(CallbackInfo info) {
		if (Chat.INSTANCE.isOn() && Chat.INSTANCE.keepHistory.getValue()) {
			info.cancel();
		}
	}
}
