package me.rebirthclient.mod.gui.components.impl;

import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.client.ClickGui;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class SliderComponent extends Component {

	private final ClickGuiTab parent;
	private double currentSliderPosition;
	SliderSetting setting;

	public SliderComponent(ClickGuiTab parent, SliderSetting setting) {
		super();
		this.parent = parent;
		this.setting = setting;
	}

	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.test(null);
		}
		return true;
	}

	private boolean hover = false;
	private boolean firstUpdate = true;
	@Override
	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		if (firstUpdate) {
			this.currentSliderPosition = (float) ((setting.getValue() - setting.getMinimum()) / setting.getRange());
			firstUpdate = false;
		}
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if ((mouseX >= ((parentX + 2)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + 26))) {
			hover = true;
			if (HudManager.currentGrabbed == null && isVisible()) {
				if (mouseClicked || ClickGuiScreen.hoverClicked) {
					ClickGuiScreen.hoverClicked = true;
					ClickGuiScreen.clicked = false;
					this.currentSliderPosition = (float) Math.min( (mouseX - (parentX + 2)) / (parentWidth - 6), 1f);
					this.currentSliderPosition = Math.max(0f, this.currentSliderPosition);
					this.setting.setValue((this.currentSliderPosition * this.setting.getRange()) + this.setting.getMinimum());
				} else {
					//this.currentSliderPosition = (float) ((setting.getValue() - setting.getMinimum()) / setting.getRange());
				}
			}
		} else {
			//this.currentSliderPosition = (float) ((setting.getValue() - setting.getMinimum()) / setting.getRange());
			hover = false;
		}
	}

	public double renderSliderPosition = 0;

	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			renderSliderPosition = 0;
			return false;
		}
		renderSliderPosition = animate(renderSliderPosition, Math.floor((parentWidth - 6) * currentSliderPosition), ClickGui.INSTANCE.sliderSpeed.getValue());
		//RenderUtil.drawBox(matrixStack, parentX + 3, (int) (parentY + currentOffset - 1), parentWidth - 6, 26, 0.5f, 0.5f, 0.5f, 0.3f);
		Render2DUtil.drawRect(matrixStack, parentX + 3, (int) (parentY + currentOffset - 1), (int) this.renderSliderPosition, 28, hover ? color.brighter() : color);
		if (this.setting == null) return true;
		TextUtil.drawCustomText(drawContext, setting.getName() + ": " + this.setting.getValueFloat(), (float) (parentX + 10),
				(float) (parentY + 6 + currentOffset), 0xFFFFFF);
		return true;
	}
}

