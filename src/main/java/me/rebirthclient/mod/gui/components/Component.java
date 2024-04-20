package me.rebirthclient.mod.gui.components;

import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.client.ClickGui;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public abstract class Component {
	public int defaultHeight = 30;
	protected ClickGuiTab parent;
	private int height = defaultHeight;
	
	public Component() {
	}

	public boolean isVisible() {
		return true;
	}
	
	public int getHeight()
	{
		if (!isVisible()) {
			return 0;
		}
		return height;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public ClickGuiTab getParent()
	{
		return parent;
	}
	
	public void setParent(ClickGuiTab parent)
	{
		this.parent = parent;
	}

	public abstract void update(int offset, double mouseX, double mouseY, boolean mouseClicked);
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		this.currentOffset = offset;
		return false;
	}
	public double currentOffset = 0;

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
}
