/**
 * A class to represent a Tab containing Armor Information
 */

package me.rebirthclient.mod.gui.elements;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.ConfigManager;
import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.mod.gui.tabs.Tab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ArmorHUD extends Tab {

	public ArmorHUD() {
		this.width = 170;
		this.height = 64;
		this.x = (int) Rebirth.CONFIG.getSettingFloat("armor_x", 0);
		this.y = (int) Rebirth.CONFIG.getSettingFloat("armor_y", 200);
	}

	@Override
	public void update(double mouseX, double mouseY, boolean mouseClicked) {
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

	@Override
	public void draw(DrawContext drawContext, float partialTicks, Color color) {
		if (Rebirth.HUD.isClickGuiOpen()) {
			Render2DUtil.drawRect(drawContext.getMatrices(), x, y, width, height, new Color(0, 0, 0, 70));
		}
		int xOff = 0;
		for (ItemStack armor : mc.player.getInventory().armor) {
			xOff += 20;

			if (armor.isEmpty()) continue;
			MatrixStack matrixStack = drawContext.getMatrices();
			matrixStack.push();
			matrixStack.translate(-x, -y - height, 0);
			matrixStack.scale(2, 2, 0);
			int damage = EntityUtil.getDamagePercent(armor);
			int yOffset = height / 2 + 15;
			drawContext.drawItem(armor, this.x + width / 2 - xOff , this.y + yOffset);
			drawContext.drawItemInSlot(mc.textRenderer, armor, this.x + width / 2 - xOff , this.y + yOffset);
			drawContext.drawText(mc.textRenderer,
					String.valueOf(damage),
					x + width / 2 + 8 - xOff - mc.textRenderer.getWidth(String.valueOf(damage)) / 2 ,
					y + yOffset - mc.textRenderer.fontHeight - 2,
					new Color((int) (255f * (1f - ((float) damage / 100f))), (int) (255f * ((float) damage / 100f)), 0).getRGB(),
					true);
			matrixStack.pop();
		}
	}
}
