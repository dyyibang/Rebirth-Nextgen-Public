/**
 * ItemESP Module
 */
package me.rebirthclient.mod.modules.render;

import me.rebirthclient.api.util.Render3DUtil;
import me.rebirthclient.asm.accessors.IEntity;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class ItemESP extends Module {
	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	public ItemESP() {
		super("ItemESP", Category.Render);
		this.setDescription("Allows the player to see items with an ESP.");

	}

    @Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		for (Entity entity : mc.world.getEntities()) {
			if(entity instanceof ItemEntity) {
				Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(interpolate(entity.lastRenderX, entity.getX(), partialTicks), interpolate(entity.lastRenderY, entity.getY(), partialTicks), interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))), color.getValue());
			}
		}
	}

	private double interpolate(double previous, double current, float delta) {
		return previous + (current - previous) * (double) delta;
	}
}
