package me.rebirthclient.mod.gui.elements;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.ConfigManager;
import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.tabs.Tab;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.Module.Category;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public class IngameGUI extends Tab {
	private KeyBinding keybindUp;
	private KeyBinding keybindDown;
	private KeyBinding keybindLeft;
	private KeyBinding keybindRight;

	int index = 0;
	int indexMods = 0;
	boolean isCategoryMenuOpen = false;

	Category[] categories;
	ArrayList<Module> modules = new ArrayList<>();
	public IngameGUI() {
		this.keybindUp = new KeyBinding("key.tabup", GLFW.GLFW_KEY_UP, "key.categories.aoba");
		this.keybindDown = new KeyBinding("key.tabdown", GLFW.GLFW_KEY_DOWN, "key.categories.aoba");
		this.keybindLeft = new KeyBinding("key.tableft", GLFW.GLFW_KEY_LEFT, "key.categories.aoba");
		this.keybindRight = new KeyBinding("key.tabright", GLFW.GLFW_KEY_RIGHT, "key.categories.aoba");

		categories = Module.Category.values();
		this.x = Rebirth.CONFIG.getSettingInt("ingame_x", 0);
		this.y = Rebirth.CONFIG.getSettingInt("ingame_y", 30);
		this.width = 150;
		this.height = 30;
	}

	@Override
	public void update(double mouseX, double mouseY, boolean mouseClicked) {
		if (Rebirth.HUD.tabGui.getValue()) {
			// If the click GUI is open, and the
			if (Rebirth.HUD.isClickGuiOpen()) {
				if (HudManager.currentGrabbed == null) {
					if (mouseX >= (x) && mouseX <= (x + width)) {
						if (mouseY >= (y) && mouseY <= (y + height)) {
							if (mouseClicked) {
								HudManager.currentGrabbed = this;
							}
						}
					}
				}
			}

			if (this.keybindUp.isPressed()) {
				if (!isCategoryMenuOpen) {
					if (index == 0) {
						index = categories.length - 1;
					} else {
						index -= 1;
					}
				} else {
					if (indexMods == 0) {
						indexMods = modules.size() - 1;
					} else {
						indexMods -= 1;
					}
				}
				this.keybindUp.setPressed(false);
			} else if (this.keybindDown.isPressed()) {
				if (!isCategoryMenuOpen) {
					index = (index + 1) % categories.length;
				} else {
					indexMods = (indexMods + 1) % modules.size();
				}
				this.keybindDown.setPressed(false);
			} else if (this.keybindRight.isPressed()) {
				if (!isCategoryMenuOpen && x != -width) {
					isCategoryMenuOpen = true;
					if (modules.isEmpty()) {
						for (Module module : Rebirth.MODULE.modules) {
							if (module.isCategory(this.categories[this.index])) {
								modules.add(module);
							}
						}
					}
				} else {
					modules.get(indexMods).toggle();
				}
				this.keybindRight.setPressed(false);
			} else if (this.keybindLeft.isPressed()) {
				if (this.isCategoryMenuOpen) {
					this.indexMods = 0;
					this.modules.clear();
					this.isCategoryMenuOpen = false;
				}
				this.keybindLeft.setPressed(false);
			}
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks, Color color) {
		// Gets the client and window.
		MinecraftClient mc = MinecraftClient.getInstance();
		MatrixStack matrixStack = drawContext.getMatrices();
		Window window = mc.getWindow();

		matrixStack.push();
		matrixStack.scale(1.0f / mc.options.getGuiScale().getValue(), 1.0f / mc.options.getGuiScale().getValue(), 1.0f);
		// Draws the top bar including "Rebirth x.x"
		TextUtil.drawString(drawContext, Rebirth.getName() + " " + Rebirth.VERSION, 8, 8, color);

		if (Rebirth.HUD.tabGui.getValue()) {
			// Draws the table including all of the categories.
			TextUtil.drawOutlinedBox(matrixStack, x, y, width, height * this.categories.length, new Color(30, 30, 30), 0.4f);
			// For every category, draw a cell for it.
			for (int i = 0; i < this.categories.length; i++) {
				//TextUtils.drawString(drawContext, ">>", x + width - 24, y + (height * i) + 8, color);
				// Draws the name of the category dependent on whether it is selected.
				if (this.index == i) {
					TextUtil.drawString(drawContext, "> \u00a7f" + this.categories[i].name(), x + 8, y + (height * i) + 8, color);
				} else {
					TextUtil.drawString(drawContext, this.categories[i].name(), x + 8, y + (height * i) + 8, 0xFFFFFF);
				}
			}

			// If any particular category menu is open.
			if (isCategoryMenuOpen) {
				// Draw the table underneath
				TextUtil.drawOutlinedBox(matrixStack, x + width, y + (height * this.index), 165, height * modules.size(), new Color(30, 30, 30), 0.4f);
				// For every mod, draw a cell for it.
				for (int i = 0; i < modules.size(); i++) {
					if (this.indexMods == i) {
						if (modules.get(i).isOn()) {
							TextUtil.drawString(drawContext, "> " + modules.get(i).getName(), x + width + 5,
									y + (i * height) + (this.index * height) + 8, color.getRGB());
						} else {
							TextUtil.drawString(drawContext, "> \u00a7f" + modules.get(i).getName(), x + width + 5,
									y + (i * height) + (this.index * height) + 8, color.getRGB());
						}
					} else {
						TextUtil.drawString(drawContext, modules.get(i).getName(), x + width + 5,
								y + (i * height) + (this.index * height) + 8,
								modules.get(i).isOn() ? color.getRGB() : 0xFFFFFF);
					}
				}
			}
		}
		matrixStack.pop();
	}
}
