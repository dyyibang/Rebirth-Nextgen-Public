/**
 * A class to represent a generic module.
 */
package me.rebirthclient.mod.modules;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.api.managers.ModuleManager;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.settings.*;
import me.rebirthclient.mod.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class Module extends Mod {

	public Module(String name, Category category) {
		this(name, "", category);
	}

	public Module(String name, String description, Category category) {
		super(name);
		this.category = category;
		this.description = description;
		ModuleManager.lastLoadMod = this;
		bindSetting = add(new BindSetting("Bind", name.equalsIgnoreCase("ClickGui") ? GLFW.GLFW_KEY_Y : -1));
	}
	private String description;
	private final Category category;
	private final BindSetting bindSetting;
	public boolean state;

	private final List<Setting> settings = new ArrayList<>();

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Module.Category getCategory() {
		return this.category;
	}

	public BindSetting getBind() {
		return this.bindSetting;
	}


	public boolean isOn() {
		return this.state;
	}

	public boolean isOff() {
		return !isOn();
	}

	public void toggle() {
		if (this.isOn()) {
			disable();
		} else {
			enable();
		}
	}

	public void enable() {
		if (this.state) return;
		CommandManager.sendChatMessageWidthId("\u00a7a[+] \u00a7f" + getName(), -1);
		this.state = true;
		Rebirth.EVENT_BUS.subscribe(this);
		this.onToggle();
		this.onEnable();
	}

	public void disable() {
		if (!this.state) return;
		CommandManager.sendChatMessageWidthId("\u00a74[-] \u00a7f" + getName(), -1);
		this.state = false;
		Rebirth.EVENT_BUS.unsubscribe(this);
		this.onToggle();
		this.onDisable();
	}
	public void setState(boolean state) {
		if (this.state == state) return;
		if (state) {
			enable();
		} else {
			disable();
		}
	}

	public boolean setBind(String rkey) {
		if (rkey.equalsIgnoreCase("none")) {
			this.bindSetting.setKey(-1);
			return true;
		}
		int key;
		try {
			key = InputUtil.fromTranslationKey("key.keyboard." + rkey.toLowerCase()).getCode();
		} catch (NumberFormatException e) {
			if (!nullCheck()) CommandManager.sendChatMessage("\u00a7c[!] \u00a7fBad key!");
			return false;
		}
		if (rkey.equalsIgnoreCase("none")) {
			key = -1;
		}
		if (key == 0) {
			return false;
		}
		this.bindSetting.setKey(key);
		return true;
	}

	public void addSetting(Setting setting) {
		this.settings.add(setting);
	}

	public ColorSetting add(ColorSetting setting) {
		addSetting(setting);
		return setting;
	}

	public SliderSetting add(SliderSetting setting) {
		addSetting(setting);
		return setting;
	}

	public BooleanSetting add(BooleanSetting setting) {
		addSetting(setting);
		return setting;
	}

	public EnumSetting add(EnumSetting setting) {
		addSetting(setting);
		return setting;
	}

	public BindSetting add(BindSetting setting) {
		addSetting(setting);
		return setting;
	}

	public List<Setting> getSettings() {
		return this.settings;
	}

	public boolean hasSettings() {
		return !this.settings.isEmpty();
	}

	public static boolean nullCheck() {
		return mc.player == null || mc.world == null;
	}

	public void onDisable() {

	}

	public void onEnable() {

	}

	public void onToggle() {

	}

	public void onUpdate() {

	}

	public void onLogin() {

	}

	public void onRender2D(DrawContext drawContext, float tickDelta) {

	}

	public void onRender3D(MatrixStack matrixStack, float partialTicks) {

	}

	public final boolean isCategory(Module.Category category) {
		return category == this.category;
	}

	public String getInfo() {
		return null;
	}

	public enum Category {
		Combat, Miscellaneous, Movement, Render, Player, Client
	}
}
