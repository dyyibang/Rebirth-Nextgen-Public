/**
 * AntiCactus Module
 */
package me.rebirthclient.mod.modules.miscellaneous;

import me.rebirthclient.mod.modules.Module;

public class ShulkerViewer extends Module {
	public static ShulkerViewer INSTANCE;
	public ShulkerViewer() {
		super("ShulkerViewer", Category.Miscellaneous);
		INSTANCE = this;
	}
}