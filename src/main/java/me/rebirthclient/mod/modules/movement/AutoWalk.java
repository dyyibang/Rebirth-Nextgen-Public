/**
 * AutoWalk Module
 */
package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.mod.modules.Module;

public class AutoWalk extends Module {
	public static AutoWalk INSTANCE;
	public AutoWalk() {
		super("AutoWalk", Category.Movement);
		INSTANCE = this;
	}

	@Override
	public void onDisable() {
		mc.options.forwardKey.setPressed(false);
	}

	@Override
	public void onUpdate() {
		mc.options.forwardKey.setPressed(true);
	}

}
