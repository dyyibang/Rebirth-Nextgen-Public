/**
 * A class that contains all of the drawing functions.
 */
package me.rebirthclient.api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import me.rebirthclient.mod.gui.font.FontRenderers;
import me.rebirthclient.mod.modules.client.ClickGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class TextUtil implements Wrapper {
	public static boolean isCustomFont() {
		return ClickGui.INSTANCE.customFont.getValue() && FontRenderers.Arial != null;
	}
	public static float getCustomWidth(String s) {
		return isCustomFont() ? FontRenderers.Arial.getWidth(s) : mc.textRenderer.getWidth(s);
	}
	public static float getCustomHeight() {
		return (isCustomFont() ? FontRenderers.Arial.getFontHeight() : mc.textRenderer.fontHeight) * 2;
	}
	public static void drawOutlinedBox(MatrixStack matrixStack, int x, int y, int width, int height, Color color,
			float alpha) {

		RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

		bufferBuilder.vertex(matrix, x, y, 0).next();
		bufferBuilder.vertex(matrix, x + width, y, 0).next();
		bufferBuilder.vertex(matrix, x + width, y + height, 0).next();
		bufferBuilder.vertex(matrix, x, y + height, 0).next();

		tessellator.draw();
		
		RenderSystem.setShaderColor(0, 0, 0, alpha);
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION);

		bufferBuilder.vertex(matrix, x, y, 0).next();
		bufferBuilder.vertex(matrix, x + width, y, 0).next();
		bufferBuilder.vertex(matrix, x + width, y + height, 0).next();
		bufferBuilder.vertex(matrix, x, y + height, 0).next();
		bufferBuilder.vertex(matrix, x, y, 0).next();

		tessellator.draw();
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public static void drawString(DrawContext drawContext, String text, float x, float y, Color color) {
		MinecraftClient mc = MinecraftClient.getInstance();
		MatrixStack matrixStack = drawContext.getMatrices();
		matrixStack.push();
		matrixStack.scale(2.0f, 2.0f, 1.0f);
		matrixStack.translate(-x / 2, -y / 2, 0.0f);
		drawContext.drawText(mc.textRenderer, text, (int)x, (int)y, color.getRGB(), true);
		matrixStack.pop();
	}
	public static void drawString(DrawContext drawContext, String text, double x, double y, int color) {
		MinecraftClient mc = MinecraftClient.getInstance();
		MatrixStack matrixStack = drawContext.getMatrices();
		matrixStack.push();
		matrixStack.scale(2.0f, 2.0f, 1.0f);
		matrixStack.translate(-x / 2, -y / 2, 0.0f);
		drawContext.drawText(mc.textRenderer, text, (int)x, (int)y, color, true);
		matrixStack.pop();
	}
	public static void drawStringWithScale(DrawContext drawContext, String text, float x, float y, Color color, float scale) {
		MinecraftClient mc = MinecraftClient.getInstance();
		MatrixStack matrixStack = drawContext.getMatrices();
		if (scale != 1) {
			matrixStack.push();
			matrixStack.scale(scale, scale, 1.0f);
			if (scale > 1.0f) {
				matrixStack.translate(-x / scale, -y / scale, 0.0f);
			} else {
				matrixStack.translate((x / scale) - x, (y * scale) - y, 0.0f);
			}
		}
		drawContext.drawText(mc.textRenderer, text, (int)x, (int)y, color.getRGB(), true);
		matrixStack.pop();
	}

	public static void drawCustomText(DrawContext drawContext, String text, double x, double y, Color color) {
		drawCustomText(drawContext, text, x, y, color.getRGB());
	}
	public static void drawCustomText(DrawContext drawContext, String text, double x, double y, int color) {
		MinecraftClient mc = MinecraftClient.getInstance();
		MatrixStack matrixStack = drawContext.getMatrices();
		matrixStack.push();
		matrixStack.scale(2.0f, 2.0f, 1.0f);
		matrixStack.translate(-x / 2, -y / 2, 0.0f);
		if (ClickGui.INSTANCE.customFont.getValue()) {
			//FontRenderers.Arial.drawString(drawContext.getMatrices(), text, (float) x + 1, (float) y + 3, new Color(0, 0, 0).getRGB());
			FontRenderers.Arial.drawString(drawContext.getMatrices(), text, (float) x, (float) y + 2, color);
		} else {
			drawContext.drawText(mc.textRenderer, text, (int) x, (int) y, color, true);
		}
		matrixStack.pop();
	}

	public static void drawCustomSmallText(DrawContext drawContext, String text, double x, double y, int color) {
		if (ClickGui.INSTANCE.customFont.getValue()) {
			FontRenderers.Arial.drawString(drawContext.getMatrices(), text, (float) x, (float) y + 1, color);
		} else {
			drawContext.drawText(mc.textRenderer, text, (int) x, (int) y, color, true);
		}
	}
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();
	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
		Camera camera = mc.getEntityRenderDispatcher().camera;
		int displayHeight = mc.getWindow().getHeight();
		int[] viewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Vector3f target = new Vector3f();

		double deltaX = pos.x - camera.getPos().x;
		double deltaY = pos.y - camera.getPos().y;
		double deltaZ = pos.z - camera.getPos().z;

		Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
		Matrix4f matrixProj = new Matrix4f(lastProjMat);
		Matrix4f matrixModel = new Matrix4f(lastModMat);
		matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
		return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
	}

	public static void drawText(DrawContext context, String text, Vec3d vector) {
		drawText(context, text, vector, -1);
	}

	public static void drawText(DrawContext context, String text, Vec3d vector, int color) {
		Vec3d preVec = vector;
		vector = worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
		if (vector.z > 0 && vector.z < 1) {
			double posX = vector.x;
			double posY = vector.y;
			double endPosX = Math.max(vector.x, vector.z);
			float scale = (float) Math.max(1 - EntityUtil.getEyesPos().distanceTo(preVec) * 0.025, 0);
			float diff = (float) (endPosX - posX) / 2;
			float textWidth = mc.textRenderer.getWidth(text) * scale;
			float tagX = (float) ((posX + diff - textWidth / 2) * 1);
			context.getMatrices().push();
			context.getMatrices().scale(scale, scale, scale);
			context.drawText(mc.textRenderer, text, (int) (tagX / scale), (int) ((posY - 11 + mc.textRenderer.fontHeight * 1.2) / scale), color, true);
			context.getMatrices().pop();
		}
	}
}
