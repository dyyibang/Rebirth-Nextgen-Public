/**
 * A class to represent a setting related to a Slider.
 */
package me.rebirthclient.mod.settings.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.ModuleManager;
import me.rebirthclient.mod.settings.Setting;

import java.util.function.Predicate;

public class SliderSetting extends Setting {

	private double value;
	private final double minValue;
	private final double maxValue;
	private final double increment;


	public SliderSetting(String name, double min, double max, double increment) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.value = min;
		this.minValue = min;
		this.maxValue = max;
		this.increment = increment;
	}

	public SliderSetting(String name, double min, double max) {
		this(name, min, max, 0.1);
	}

	public SliderSetting(String name, int min, int max) {
		this(name, min, max, 1);
	}


	public SliderSetting(String name, double min, double max, double increment, Predicate visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.value = min;
		this.minValue = min;
		this.maxValue = max;
		this.increment = increment;
	}

	public SliderSetting(String name, double min, double max, Predicate visibilityIn) {
		this(name, min, max, 0.1, visibilityIn);
	}

	public SliderSetting(String name, int min, int max, Predicate visibilityIn) {
		this(name, min, max, 1, visibilityIn);
	}

	public final double getValue() {
		return this.value;
	}

	public final float getValueFloat() {
		return (float) this.value;
	}

	public final int getValueInt() {
		return (int) this.value;
	}

	public final void setValue(double value) {
		this.value = Math.round(value / increment) * increment;
	}

	public final double getMinimum() {
		return this.minValue;
	}

	public final double getMaximum() {
		return this.maxValue;
	}

	public final double getIncrement() {
		return increment;
	}

	public final double getRange() {
		return this.maxValue - this.minValue;
	}

	@Override
	public void loadSetting() {
		setValue(Rebirth.CONFIG.getSettingFloat(this.getLine(), (float) this.value));
	}
}
