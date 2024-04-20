package me.rebirthclient.mod.modules.player;

import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.SliderSetting;

public class Reach extends Module {
	public static Reach INSTANCE;
	public final SliderSetting distance = add(new SliderSetting("Distance", 1f, 15f, 1f));

	public Reach() {
		super("Reach", Category.Player);
		INSTANCE = this;
	}
}