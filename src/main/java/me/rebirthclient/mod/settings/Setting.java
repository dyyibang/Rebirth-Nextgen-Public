/**
 * An abstract setting.
 */
package me.rebirthclient.mod.settings;

import java.util.function.Predicate;

public abstract class Setting {
	private final String name;
	private final String line;
	public final Predicate visibility;
	
	public Setting(String name, String line) {
		this.name = name;
		this.line = line;
		this.visibility = null;
	}

	public Setting(String name, String line, Predicate visibilityIn) {
		this.name = name;
		this.line = line;
		this.visibility = visibilityIn;
	}

	public final String getName() {
		return this.name;
	}

	public final String getLine() {
		return this.line;
	}

	public abstract void loadSetting();
}
