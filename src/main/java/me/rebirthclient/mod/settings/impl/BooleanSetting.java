package me.rebirthclient.mod.settings.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.ModuleManager;
import me.rebirthclient.mod.settings.Setting;

import java.util.function.Predicate;

public class BooleanSetting extends Setting {
	public boolean parent = false;
	public boolean popped = false;
	private boolean value;

	public BooleanSetting(String name) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.value = false;
	}

	public BooleanSetting(String name, Predicate visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.value = false;
	}

	public final boolean getValue() {
		return this.value;
	}
	
	public final void setValue(boolean value) {
		this.value = value;
	}
	
	public final void toggleValue() {
		this.value = !value;
	}
	public final boolean isOpen() {
		if (parent) {
			return popped;
		} else {
			return true;
		}
	}
	@Override
	public void loadSetting() {
		this.value = Rebirth.CONFIG.getSettingBoolean(this.getLine(), value);
	}

	public BooleanSetting setParent() {
		parent = true;
		return this;
	}
}
