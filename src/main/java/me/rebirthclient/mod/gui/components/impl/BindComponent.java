package me.rebirthclient.mod.gui.components.impl;

import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.client.ClickGui;
import me.rebirthclient.mod.settings.impl.BindSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class BindComponent extends Component {
	private final BindSetting setting;
	public BindComponent(ClickGuiTab parent, BindSetting setting) {
		super();
		this.setting = setting;
		this.parent = parent;
	}

	boolean hover = false;
	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if (HudManager.currentGrabbed == null && isVisible()) {
			if ((mouseX >= ((parentX + 2)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset)))
					&& mouseY <= ((parentY + offset) + 24))) {
				hover = true;
				if (mouseClicked) {
					ClickGuiScreen.clicked = false;
					setting.setListening(!setting.isListening());
				}
			} else {
				hover = false;
			}
		} else {
			hover = false;
		}
	}

	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		if (back) {
			setting.setListening(false);
		}
		int parentX = this.parent.getX();
		int parentY = this.parent.getY();
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			return false;
		}
		int y = (int) (parent.getY() + currentOffset - 2);
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		String text;
		if (setting.isListening()) {
			text = "Press Key..";
		} else {
			text = setting.getBind();
		}
		if (hover) Render2DUtil.drawRect(matrixStack, (float) parentX + 3, (float) y + 1, (float) width - 6, (float) 28, ClickGui.INSTANCE.shColor.getValue());
		TextUtil.drawCustomText(drawContext, setting.getName() + ": " + text, (float) (parentX + 10),
				(float) (parentY + 8 + currentOffset), 0xFFFFFF);
		return true;
	}
}