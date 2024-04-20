/**
 * A class to represent a ClickGui Tab that contains different Components.
 */

package me.rebirthclient.mod.gui.tabs;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.client.ClickGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;


public class ClickGuiTab extends Tab {
	protected String title;
	protected boolean drawBorder = true;
	protected boolean inheritHeightFromChildren = true;
	private Module.Category category = null;
	protected ArrayList<Component> children = new ArrayList<>();

	public ClickGuiTab(String title, int x, int y) {
		this.title = title;
		this.x = Rebirth.CONFIG.getSettingInt(title + "_x", x);
		this.y = Rebirth.CONFIG.getSettingInt(title + "_y", y);
		this.width = 190;
		this.mc = MinecraftClient.getInstance();
	}

	public ClickGuiTab(Module.Category category, int x, int y) {
		this(category.name(), x, y);
		this.category = category;
	}
	public ArrayList<Component> getChildren() {
		return children;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {

		this.title = title;
	}

	public final int getX() {
		return x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return y;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final int getWidth() {
		return width;
	}

	public final void setWidth(int width) {
		this.width = width;
	}

	public final int getHeight() {
		return height;
	}

	public final void setHeight(int height) {
		this.height = height;
	}

	public final boolean isGrabbed() {
		return (HudManager.currentGrabbed == this);
	}

	public final void addChild(Component component) {
		this.children.add(component);
	}

	@Override
	public void update(double mouseX, double mouseY, boolean mouseClicked) {
		if (this.inheritHeightFromChildren) {
			int tempHeight = 1;
			for (Component child : children) {
				tempHeight += (child.getHeight());
			}
			this.height = tempHeight;
		}

		onMouseClick(mouseX, mouseY, mouseClicked);
	}

	public void onMouseClick(double mouseX, double mouseY, boolean mouseClicked) {
		if (Rebirth.HUD.isClickGuiOpen()) {
			if (HudManager.currentGrabbed == null) {
				if (mouseX >= (x - 5) && mouseX <= (x + width + 5)) {
					if (mouseY >= (y - 5) && mouseY <= (y + 25)) {
						if (mouseClicked) {
							HudManager.currentGrabbed = this;
						}
					}
				}
			}
			int i = defaultHeight;
			for (Component child : this.children) {
				child.update(i, mouseX, mouseY, mouseClicked);
				i += child.getHeight();
			}
		}
	}

	public static double animate(double current, double endPoint) {
		return animate(current, endPoint, ClickGui.INSTANCE.animationSpeed.getValue());
	}

	public static double animate(double current, double endPoint, double speed) {
		boolean shouldContinueAnimation = endPoint > current;

		double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
		double factor = dif * speed;
		if (Math.abs(factor) <= 0.001) return endPoint;
		return current + (shouldContinueAnimation ? factor : -factor);
	}
	public double currentHeight = 0;

	@Override
	public void draw(DrawContext drawContext, float partialTicks, Color color) {
		if (this.inheritHeightFromChildren) {
			int tempHeight = 1;
			for (Component child : children) {
				tempHeight += (child.getHeight());
			}
			this.height = tempHeight;
		}

		MatrixStack matrixStack = drawContext.getMatrices();
		currentHeight = animate(currentHeight, height);
		if (drawBorder) {
			Render2DUtil.drawRect(matrixStack, x - 4, y - 4, width + 7, 30, color.getRGB());
			Render2DUtil.drawRect(matrixStack, x - 4, y - 4 + 30, width + 7, 1, new Color(38, 38, 38));
			Render2DUtil.drawRect(matrixStack, x, y + 28, width, (int) currentHeight + 1, ClickGui.INSTANCE.bgColor.getValue());
		}
		int i = defaultHeight;
		for (Component child : children) {
			if (child.isVisible()) {
				child.draw(i, drawContext, partialTicks, color, false);
				i += child.getHeight();
			} else {
				child.currentOffset = i - defaultHeight;
			}
		}
		//TextUtil.drawCustomText(drawContext, this.title, x + (float) width / 2 - (float) mc.textRenderer.getWidth(title), y + 4, new Color(255, 255, 255));
		TextUtil.drawCustomText(drawContext, this.title, x + 4, y + 4, new Color(255, 255, 255));
		if (category != null) {
			String text = "[" + Rebirth.MODULE.categoryModules.get(category) + "]";
			TextUtil.drawCustomText(drawContext, text, x + width - 4 - TextUtil.getCustomWidth(text) * 2, y + 4, new Color(255, 255, 255));
		}
	}
}
