/**
 * PlayerESP Module
 */
package me.rebirthclient.mod.modules.render;

import me.rebirthclient.api.util.Render3DUtil;
import me.rebirthclient.asm.accessors.IEntity;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class PlayerESP extends Module {
	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	public PlayerESP() {
		super("PlayerESP", Category.Render);
		this.setDescription("Allows the player to see other players with an ESP.");

	}

    @Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		for (Entity player : mc.world.getPlayers()) {
			if(player != mc.player) {
				Render3DUtil.draw3DBox(matrixStack, ((IEntity) player).getDimensions().getBoxAt(new Vec3d(interpolate(player.lastRenderX, player.getX(), partialTicks), interpolate(player.lastRenderY, player.getY(), partialTicks), interpolate(player.lastRenderZ, player.getZ(), partialTicks))), color.getValue());
			}
		}
	}

	private double interpolate(double previous, double current, float delta) {
		return previous + (current - previous) * (double) delta;
	}
}
