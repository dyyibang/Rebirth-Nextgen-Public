package me.rebirthclient.mod.modules.render;

import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.ColorSetting;

import java.awt.*;

public class Fullbright extends Module {
	public static Fullbright INSTANCE;
	public ColorSetting color = add(new ColorSetting("Color", new Color(0xFFFFFFFF, true)));
	public Fullbright() {
		super("FullBright", Category.Render);
		this.setDescription("Maxes out the brightness.");
		INSTANCE = this;
	}
}
