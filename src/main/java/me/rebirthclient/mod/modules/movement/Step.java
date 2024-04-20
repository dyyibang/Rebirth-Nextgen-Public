/**
 * Step Module
 */
package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.combat.AutoPush;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import me.rebirthclient.api.util.EntityUtil;

public class Step extends Module {

	private final SliderSetting stepHeight = add(new SliderSetting("Height", 0.0f, 5f, 0.5f));
	
	public Step() {
		super("Step", "Steps up blocks.", Category.Movement);
	}

	@Override
	public void onDisable() {
		if (nullCheck()) return;
		mc.player.setStepHeight(0.5f);
	}

	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		if (mc.player.isSneaking() || EntityUtil.isInsideBlock() || mc.player.isInLava() || mc.player.isTouchingWater() || AutoPush.isInWeb(mc.player) || !mc.player.isOnGround()) {
			mc.player.setStepHeight(0.5f);
			return;
		}
		mc.player.setStepHeight(stepHeight.getValueFloat());
	}
}
