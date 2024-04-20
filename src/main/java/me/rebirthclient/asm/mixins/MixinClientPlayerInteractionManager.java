package me.rebirthclient.asm.mixins;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.impl.ClickBlockEvent;
import me.rebirthclient.mod.modules.player.Reach;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
	@Inject(at = { @At("HEAD") }, method = { "getReachDistance()F" }, cancellable = true)
	private void onGetReachDistance(CallbackInfoReturnable<Float> ci) {
		Reach reachHack = Reach.INSTANCE;
		if (reachHack.isOn()) {
			ci.setReturnValue(reachHack.distance.getValueFloat());
		}
	}

	@Inject(at = { @At("HEAD") }, method = { "hasExtendedReach()Z" }, cancellable = true)
	private void hasExtendedReach(CallbackInfoReturnable<Boolean> cir) {
		Reach reachHack = Reach.INSTANCE;
		if (reachHack.isOn())
			cir.setReturnValue(true);
	}

	@Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
	private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		ClickBlockEvent event = new ClickBlockEvent(pos);
		Rebirth.EVENT_BUS.post(event);
		if (event.isCancel()) {
			cir.setReturnValue(false);
		}
	}
}
