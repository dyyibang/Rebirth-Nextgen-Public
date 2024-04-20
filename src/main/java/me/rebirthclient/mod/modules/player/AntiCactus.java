/**
 * AntiCactus Module
 */
package me.rebirthclient.mod.modules.player;

import me.rebirthclient.mod.modules.Module;

public class AntiCactus extends Module {
	public static AntiCactus INSTANCE;
	public AntiCactus() {
		super("AntiCactus", Category.Player);
		this.setDescription("Prevents blocks from hurting you.");
		INSTANCE = this;
	}
}