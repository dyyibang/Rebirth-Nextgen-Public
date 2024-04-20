package me.rebirthclient.mod.gui.tabs;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.gui.components.impl.*;
import me.rebirthclient.mod.settings.Setting;
import me.rebirthclient.mod.settings.impl.*;

public class OptionsTab extends ClickGuiTab {
	
	public OptionsTab(String title, int x, int y) {
		super(title, x, y);
		this.setWidth(180);
	}

	public void addChild(Setting setting) {
		if (setting == null) return;
		Rebirth.CONFIG.SETTINGS.add(setting);
		if (setting instanceof SliderSetting) {
			addChild(new SliderComponent(this, (SliderSetting) setting));
		} else if (setting instanceof BooleanSetting) {
			addChild(new BooleanComponent(this, (BooleanSetting) setting));
		} else if (setting instanceof EnumSetting) {
			addChild(new EnumComponent(this, (EnumSetting) setting));
		} else if (setting instanceof BindSetting) {
			addChild(new BindComponent(this, (BindSetting) setting));
		} else if (setting instanceof ColorSetting) {
			addChild(new ColorComponents(this, (ColorSetting) setting));
		}
	}
}
