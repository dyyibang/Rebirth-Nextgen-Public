package me.rebirthclient.mod.gui.components.impl;

import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.client.ClickGui;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class BooleanComponent extends Component {

	BooleanSetting setting;

	public BooleanComponent(ClickGuiTab parent, BooleanSetting setting) {
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

	boolean hover = false;
	@Override
	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if ((mouseX >= ((parentX + 2)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + 26))) {
			hover = true;
			if (HudManager.currentGrabbed == null && isVisible()) {
				if (mouseClicked) {
					ClickGuiScreen.clicked = false;
					setting.toggleValue();
				}
				if (ClickGuiScreen.rightClicked) {
					ClickGuiScreen.rightClicked = false;
					setting.popped = !setting.popped;
				}
			}
		} else {
			hover = false;
		}
	}

	public double currentWidth = 0;
	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			currentWidth = 0;
			return false;
		}
		int x = parent.getX();
		int y = (int) (parent.getY() + currentOffset - 2);
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();

		Render2DUtil.drawRect(matrixStack, (float) x + 3, (float) y + 1, (float) width - 6, (float) defaultHeight - 2, hover ? ClickGui.INSTANCE.shColor.getValue() : ClickGui.INSTANCE.sbgColor.getValue());

		currentWidth = animate(currentWidth, setting.getValue() ? (width - 6D) : 0D, ClickGui.INSTANCE.booleanSpeed.getValue());
		Render2DUtil.drawRect(matrixStack, (float) x + 3, (float) y + 1, (float) currentWidth, (float) defaultHeight - 2, hover ? color.brighter() : color);
		TextUtil.drawCustomText(drawContext, setting.getName(), x + 10, y + 8, new Color(-1).getRGB());
		if (setting.parent) {
			TextUtil.drawCustomText(drawContext, setting.popped ? "-" : "+", x + width - 22,
					y + 8, new Color(255, 255, 255).getRGB());
		}
		return true;
	}
}
