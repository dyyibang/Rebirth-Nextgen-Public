package me.rebirthclient.mod.modules.render;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.BreakManager;
import me.rebirthclient.api.util.Render3DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.HashMap;

public class BreakESP extends Module {
	public static BreakESP INSTANCE;
	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	public BooleanSetting outline = add(new BooleanSetting("Outline"));
	public BooleanSetting box = add(new BooleanSetting("Box"));
	public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 0, 2000));

	public BreakESP() {
		super("BreakESP", Category.Render);
		INSTANCE = this;
	}

	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		for (BreakManager.BreakData breakData : new HashMap<>(Rebirth.BREAK.breakMap).values()) {
			if (breakData == null || breakData.getEntity() == null) continue;
			double size = 0.5 * (1 - breakData.fade.easeOutQuad());
			Render3DUtil.draw3DBox(matrixStack, new Box(breakData.pos).shrink(size, size, size).shrink(-size, -size, -size), color.getValue(), outline.getValue(), box.getValue());
		}
	}

	@Override
	public void onRender2D(DrawContext drawContext, float tickDelta) {
		for (BreakManager.BreakData breakData : new HashMap<>(Rebirth.BREAK.breakMap).values()) {
			if (breakData == null || breakData.getEntity() == null) continue;
			TextUtil.drawText(drawContext, breakData.getEntity().getEntityName(), breakData.pos.toCenterPos().add(0, 0.1, 0));
			TextUtil.drawText(drawContext, mc.world.isAir(breakData.pos) ? "Broken" : "Breaking", breakData.pos.toCenterPos().add(0, -0.1, 0), new java.awt.Color(0, 255, 51).getRGB());
		}
	}
}
