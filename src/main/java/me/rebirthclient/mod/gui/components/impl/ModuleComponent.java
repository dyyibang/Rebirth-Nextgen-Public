package me.rebirthclient.mod.gui.components.impl;

import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.client.ClickGui;
import me.rebirthclient.mod.settings.Setting;
import me.rebirthclient.mod.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends me.rebirthclient.mod.gui.components.Component implements Wrapper {

	private final String text;
	private final Module module;
	private final ClickGuiTab parent;
	private boolean popped = false;

	private int expandedHeight = defaultHeight;

	private final List<me.rebirthclient.mod.gui.components.Component> settingsList = new ArrayList<>();
	public List<Component> getSettingsList() {
		return settingsList;
	}
	public ModuleComponent(String text, ClickGuiTab parent, Module module) {
		super();
		this.text = text;
		this.parent = parent;
		this.module = module;
		for (Setting setting : this.module.getSettings()) {
			me.rebirthclient.mod.gui.components.Component c;
			if (setting instanceof SliderSetting) {
				c = new SliderComponent(this.parent, (SliderSetting) setting);
			} else if (setting instanceof BooleanSetting) {
				c = new BooleanComponent(this.parent, (BooleanSetting) setting);
			} else if (setting instanceof BindSetting) {
				c = new BindComponent(this.parent, (BindSetting) setting);
			} else if (setting instanceof EnumSetting) {
				c = new EnumComponent(this.parent, (EnumSetting) setting);
			} else if (setting instanceof ColorSetting) {
				c= new ColorComponents(this.parent, (ColorSetting) setting);
			} else {
				c = null;
			}
			if (c != null)
				settingsList.add(c);
		}

		RecalculateExpandedHeight();
	}

	boolean hovered = false;

	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();

		// If the Module options are popped, display all of the options.
		if (this.popped) {
			// Updates all of the options. 
			int i = offset + defaultHeight;
			for (me.rebirthclient.mod.gui.components.Component children : this.settingsList) {
				children.update(i, mouseX, mouseY, mouseClicked);
				i += children.getHeight();
			}
		}


		// Check if the current Module Component is currently hovered over.
		hovered = ((mouseX >= parentX && mouseX <= (parentX + parentWidth)) && (mouseY >= parentY + offset && mouseY <= (parentY + offset + 28)));
		if (hovered && HudManager.currentGrabbed == null) {
			if (mouseClicked) {
				ClickGuiScreen.clicked = false;
				module.toggle();
			}

			if (ClickGuiScreen.rightClicked) {
				ClickGuiScreen.rightClicked = false;
				this.popped = !this.popped;
			}
		}
		RecalculateExpandedHeight();
		if (this.popped) {
			this.setHeight(expandedHeight);
		} else {
			this.setHeight(defaultHeight);
		}
	}

	public boolean isPopped = false;
	double currentHeight = 0;

	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		currentOffset = animate(currentOffset, offset);
		currentHeight = animate(currentHeight, getHeight());
		Render2DUtil.drawRect(matrixStack, parentX + 2, (int) (parentY + currentOffset), parentWidth - 4, (int) (currentHeight) - 2, hovered ? ClickGui.INSTANCE.mhColor.getValue() : ClickGui.INSTANCE.mbgColor.getValue());

		if (ClickGui.fade.easeOutQuad() >= 1) setScissorRegion(parentX, (int) (parentY + currentOffset + defaultHeight), parentWidth, mc.getWindow().getHeight() - (int) (parentY + currentOffset + defaultHeight));
		if (this.popped) {
			isPopped = true;
			int i = offset + defaultHeight;
			for (me.rebirthclient.mod.gui.components.Component children : this.settingsList) {
				if (children.isVisible()) {
					children.draw(i, drawContext, partialTicks, color, false);
					i += children.getHeight();
				} else {
					if (children instanceof SliderComponent sliderComponent) {
						sliderComponent.renderSliderPosition = 0;
					} else if (children instanceof BooleanComponent booleanComponent) {
						booleanComponent.currentWidth = 0;
					} else if (children instanceof ColorComponents colorComponents) {
						colorComponents.currentWidth = 0;
					}
					children.currentOffset = i - defaultHeight;
				}
			}
		} else if (isPopped) {
			boolean finish2 = true;
			boolean finish = false;
			for (me.rebirthclient.mod.gui.components.Component children : this.settingsList) {
				if (children.isVisible()) {
					if (!children.draw((int) currentOffset, drawContext, partialTicks, color, true)) {
						finish = true;
					} else {
						finish2 = false;
					}
				}
			}
			if (finish && finish2) {
				isPopped = false;
			}
		} else {
			for (me.rebirthclient.mod.gui.components.Component children : this.settingsList) {
				children.currentOffset = currentOffset;
			}
		}
		if (ClickGui.fade.easeOutQuad() >= 1) GL11.glDisable(GL11.GL_SCISSOR_TEST);

		TextUtil.drawCustomText(drawContext, this.text, (float) (parentX + 8), (float) (parentY + 8 + currentOffset),
				module.isOn() ? color.getRGB() : 0xFFFFFF);
		if (ClickGui.INSTANCE.gear.getValue()) {
			TextUtil.drawCustomText(drawContext, this.popped ? "-" : "+", parentX + parentWidth - 22,
					parentY + 8 + currentOffset, ClickGui.INSTANCE.gearColor.getValue().getRGB());
		}
		return true;
	}

	public void setScissorRegion(int x, int y, int width, int height) {
		if (y > mc.getWindow().getHeight()) return;
		double scaledY = (mc.getWindow().getHeight() - (y + height));
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x, (int) scaledY, width, height);
	}
	public void RecalculateExpandedHeight() {
		int height = defaultHeight;
		for (Component children : this.settingsList) {
			if (children != null && children.isVisible()) {
				height += children.getHeight();
			}
		}
		expandedHeight = height;
	}
}
