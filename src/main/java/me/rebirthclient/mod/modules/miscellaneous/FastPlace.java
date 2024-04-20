package me.rebirthclient.mod.modules.miscellaneous;

import me.rebirthclient.mod.modules.Module;

public class FastPlace extends Module {
	public FastPlace() {
		super("FastPlace", Category.Miscellaneous);
		this.setDescription("Places blocks exceptionally fast");
	}

    @Override
	public void onUpdate() {
		mc.itemUseCooldown = 0;
	}
}
