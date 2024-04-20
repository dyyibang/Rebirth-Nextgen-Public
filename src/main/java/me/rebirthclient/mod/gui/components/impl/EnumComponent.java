package me.rebirthclient.mod.gui.components.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
public class EnumComponent extends Component {
	private final EnumSetting setting;
	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.test(null);
		}
		return true;
	}
	public EnumComponent(ClickGuiTab parent, EnumSetting enumSetting) {
		super();
		this.parent = parent;
		setting = enumSetting;
	}

	private boolean hover = false;

	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if ((mouseX >= ((parentX + 2)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + 26))) {
			hover = true;
			if (HudManager.currentGrabbed == null && isVisible()) {
				if (mouseClicked) {
					ClickGuiScreen.clicked = false;
					setting.increaseEnum();
				}
				if (ClickGuiScreen.rightClicked) {
					setting.popped = !setting.popped;
					ClickGuiScreen.rightClicked = false;
				}
			}
		} else {
			hover = false;
		}

		if (HudManager.currentGrabbed == null && isVisible() && mouseClicked) {
			int cy = parentY + offset - 2;
			if (setting.popped) {
				for (Object o : setting.getValue().getClass().getEnumConstants()) {
					cy += TextUtil.getCustomHeight();
					if (mouseX >= parentX && mouseX <= parentX + parentWidth && mouseY >= 14 + cy && mouseY < TextUtil.getCustomHeight() + 14 + cy) {
						setting.setEnumValue(String.valueOf(o));
						ClickGuiScreen.clicked = false;
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		if (setting.popped && !back) {
			int y = 0;
			for (Object ignored : setting.getValue().getClass().getEnumConstants()) {
				y += TextUtil.getCustomHeight();
			}
			setHeight(defaultHeight + y);
		} else {
			setHeight(defaultHeight);
		}
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			return false;
		}
		int x = parent.getX();
		int y = (int) (parent.getY() + currentOffset - 2);
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();

		Render2DUtil.drawRect(matrixStack, (float) x + 3, (float) y + 1, (float) width - 6, (float) 28, hover ? Rebirth.HUD.getColor().brighter() : Rebirth.HUD.getColor());
		TextUtil.drawCustomText(drawContext, setting.getName() + ": " + setting.getValue().name(), x + 10, y + 8, new Color(-1).getRGB());
		TextUtil.drawCustomText(drawContext, setting.popped ? "-" : "+", x + width - 22, y + 8, new Color(255, 255, 255).getRGB());

		int cy = y;
		if (setting.popped && !back) {
			for (Object o : setting.getValue().getClass().getEnumConstants()) {

				cy += TextUtil.getCustomHeight();
				String s = o.toString();

				TextUtil.drawCustomText(drawContext, s, width / 2.0f - TextUtil.getCustomWidth(s) + 2.0f + x, 14 + (cy), setting.getValue().name().equals(s) ? -1 : new Color(120, 120, 120).getRGB());
			}
		}
		return true;
	}
}