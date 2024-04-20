/**
 * NoSlowdown Module
 */
package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.mod.modules.Module;

public class NoSlowdown extends Module {
	public static NoSlowdown INSTANCE;
	public NoSlowdown() {
		super("NoSlowdown", Category.Movement);
		this.setDescription("Prevents the player from being slowed down by blocks.");
		INSTANCE = this;
	}
}
