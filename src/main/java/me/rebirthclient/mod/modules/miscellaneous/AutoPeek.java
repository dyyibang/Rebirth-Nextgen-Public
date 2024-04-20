package me.rebirthclient.mod.modules.miscellaneous;

import com.mojang.blaze3d.systems.RenderSystem;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.ColorUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.movement.HoleSnap;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class AutoPeek extends Module {

	public final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	public SliderSetting circleSize = add(new SliderSetting("CircleSize", 0.1f, 2.5f));
	public BooleanSetting fade = add(new BooleanSetting("Fade"));
	public SliderSetting segments = add(new SliderSetting("Segments", 0, 360));
	public BooleanSetting inAir = add(new BooleanSetting("InAir"));
	public BooleanSetting timer = add(new BooleanSetting("Timer"));
	public BooleanSetting teleport = add(new BooleanSetting("Teleport"));
	public BooleanSetting onlyUsing = add(new BooleanSetting("OnlyUsing"));
	public AutoPeek() {
		super("AutoPeek", Category.Miscellaneous);
	}

	private Vec3d pos;

	private boolean back = false;
	@EventHandler
	public void onPacket(PacketEvent.Send event) {
		if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && mc.player.getActiveItem().getItem() instanceof BowItem) {
			back = onlyUsing.getValue();
		}
	}

	@Override
	public void onEnable() {
		if (nullCheck()) {
			disable();
			return;
		}
		pos = mc.player.getPos();
	}

	@Override
	public void onDisable() {
		Rebirth.TIMER.reset();
	}

	@EventHandler
	public void onMove(MoveEvent event) {
		if (!mc.player.isAlive() || mc.player.isFallFlying()) {
			disable();
			return;
		}
		if ((MovementUtil.isMoving() && (!onlyUsing.getValue() || EntityUtil.isUsing()) || (onlyUsing.getValue() && EntityUtil.isUsing())) && !back || (!mc.player.isOnGround() && !inAir.getValue())) {
			Rebirth.TIMER.set(1);
			return;
		}
		if (pos == null) {
			disable();
			return;
		}
		if (timer.getValue()) {
			Rebirth.TIMER.set(3);
		}
		double x;
		double z;
		if (teleport.getValue()) {
			Vec3d playerPos = mc.player.getPos();
			x = pos.x - playerPos.x;
			z = pos.z - playerPos.z;
		} else {
			Vec3d playerPos = mc.player.getPos();
			float rotation = HoleSnap.getRotationTo(playerPos, new Vec3d(pos.x, playerPos.y, pos.z)).x;
			float yawRad = rotation / 180.0f * 3.1415927f;
			double dist = playerPos.distanceTo(new Vec3d(pos.x, playerPos.y, pos.z));
			double cappedSpeed = Math.min(0.2873, dist);
			x = -(float) Math.sin(yawRad) * cappedSpeed;
			z = (float) Math.cos(yawRad) * cappedSpeed;
		}
		if (Math.abs(x) < 0.1 && Math.abs(z) < 0.1) {
			back = false;
			Rebirth.TIMER.set(1);
		}
		event.setX(x);
		event.setZ(z);
	}

	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		if (pos == null) {
			disable();
			return;
		}
		GL11.glEnable(GL11.GL_BLEND);
		Color color = this.color.getValue();

		if (fade.getValue()) {
			double temp = 0.01;
			for (double i = 0; i < circleSize.getValue(); i += temp) {
				doCircle(matrixStack, ColorUtil.injectAlpha(color, (int) Math.min(color.getAlpha() * 2 / (circleSize.getValue() / temp), 255)), i, pos, segments.getValueInt());
			}
		} else {
			doCircle(matrixStack, color, circleSize.getValue(), pos, segments.getValueInt());
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public static void doCircle(MatrixStack matrixStack, Color color, double circleSize, Vec3d pos, int segments) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
		RenderSystem.setShader(GameRenderer::getPositionProgram);

		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);

		for (double i = 0; i < 360; i += ((double) 360 / segments)) {
			double x = Math.sin(Math.toRadians(i)) * circleSize;
			double z = Math.cos(Math.toRadians(i)) * circleSize;
			Vec3d tempPos = new Vec3d(pos.x + x, pos.y, pos.z + z);
			bufferBuilder.vertex(matrix, (float) tempPos.x, (float) tempPos.y, (float) tempPos.z).next();
		}

		tessellator.draw();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}
