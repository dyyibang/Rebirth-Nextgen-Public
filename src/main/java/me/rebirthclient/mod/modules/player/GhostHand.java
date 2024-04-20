package me.rebirthclient.mod.modules.player;

import me.rebirthclient.mod.modules.Module;

public class GhostHand extends Module {
	public static GhostHand INSTANCE;
	public boolean isActive;
	public GhostHand() {
		super("GhostHand", Category.Player);
		INSTANCE = this;
	}

	@Override
	public void onDisable() {
		isActive = false;
	}

	public boolean canWork() {
		return isOn() && !mc.options.useKey.isPressed() && !mc.options.sneakKey.isPressed();
	}
}
