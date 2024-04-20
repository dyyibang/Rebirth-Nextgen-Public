/**
 * Sprint Module
 */
package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.UpdateWalkingEvent;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.settings.impl.BooleanSetting;

public class Sprint extends Module {

	public static Sprint INSTANCE;
	private final BooleanSetting legit =
			add(new BooleanSetting("Legit"));
	public Sprint() {
		super("Sprint", Category.Movement);
		this.setDescription("Permanently keeps player in sprinting mode.");
		INSTANCE = this;
	}

	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		if (legit.getValue()) {
			mc.options.sprintKey.setPressed(true);
		}
	}

	@EventHandler
	public void onUpdateWalking(UpdateWalkingEvent event) {
		if (!legit.getValue()) {
			if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
			mc.player.setSprinting(MovementUtil.isMoving() && !mc.player.isSneaking());
		}
	}
}
