package me.rebirthclient.api.managers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import me.rebirthclient.api.interfaces.IShaderEffect;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.render.ShaderChams;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

public class ShaderManager implements Wrapper {
    private final static List<RenderTask> tasks = new ArrayList<>();
    private MyFramebuffer shaderBuffer;

    public float time = 0;

    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;

    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }
    public void renderShaders() {
        tasks.forEach(t -> applyShader(t.task(), t.shader()));
        tasks.clear();  
    }

    public void applyShader(Runnable runnable, Shader mode) {
        if (fullNullCheck()) return;
        Framebuffer MCBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (shaderBuffer.textureWidth != MCBuffer.textureWidth || shaderBuffer.textureHeight != MCBuffer.textureHeight)
            shaderBuffer.resize(MCBuffer.textureWidth, MCBuffer.textureHeight, false);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, shaderBuffer.fbo);
        shaderBuffer.beginWrite(true);
        runnable.run();
        shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, MCBuffer.fbo);
        MCBuffer.beginWrite(false);
        ManagedShaderEffect shader = getShader(mode);
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        PostEffectProcessor effect = shader.getShaderEffect();

        if (effect != null)
            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufIn", shaderBuffer);

        Framebuffer outBuffer = shader.getShaderEffect().getSecondaryTarget("bufOut");
        setupShader(mode, shader);
        shaderBuffer.clear(false);
        mainBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT;
            case Smoke -> SMOKE;
            case Snow -> SNOW;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT_OUTLINE;
            case Smoke -> SMOKE_OUTLINE;
            case Snow -> SNOW_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    private void setup(Shader shader, ManagedShaderEffect effect, boolean glow, ColorSetting outlineColor, SliderSetting fillAlpha, SliderSetting alpha2, SliderSetting lineWidth, SliderSetting octaves, int quality, SliderSetting factor, SliderSetting gradient, SliderSetting speed, ColorSetting smokeGlow, ColorSetting smokeGlow1, ColorSetting fill, ColorSetting fillColor2, ColorSetting fillColor3) {
        if (shader == Shader.Gradient) {
            effect.setUniformValue("alpha0", !glow ? -1.0f : outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("alpha1", fillAlpha.getValueInt() / 255f);
            effect.setUniformValue("alpha2", alpha2.getValueInt() / 255f);
            effect.setUniformValue("lineWidth", lineWidth.getValueInt());
            effect.setUniformValue("oct", octaves.getValueInt());
            effect.setUniformValue("quality", quality);
            effect.setUniformValue("factor", factor.getValueFloat());
            effect.setUniformValue("moreGradient", gradient.getValueFloat());
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += speed.getValueFloat() * 0.002;
        } else if (shader == Shader.Smoke) {
            effect.setUniformValue("alpha0", !glow ? -1.0f : outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("alpha1", fillAlpha.getValueInt() / 255f);
            effect.setUniformValue("lineWidth", lineWidth.getValueInt());
            effect.setUniformValue("quality", quality);
            effect.setUniformValue("first", outlineColor.getValue().getRed() / 255f, outlineColor.getValue().getGreen() / 255f, outlineColor.getValue().getBlue() / 255f, outlineColor.getValue().getAlpha() / 255f);
            effect.setUniformValue("second", smokeGlow.getValue().getRed() / 255f, smokeGlow.getValue().getGreen() / 255f, smokeGlow.getValue().getBlue() / 255f);
            effect.setUniformValue("third", smokeGlow1.getValue().getRed() / 255f, smokeGlow1.getValue().getGreen() / 255f, smokeGlow1.getValue().getBlue() / 255f);
            effect.setUniformValue("ffirst", fill.getValue().getRed() / 255f, fill.getValue().getGreen() / 255f, fill.getValue().getBlue() / 255f, fill.getValue().getAlpha() / 255f);
            effect.setUniformValue("fsecond", fillColor2.getValue().getRed() / 255f, fillColor2.getValue().getGreen() / 255f, fillColor2.getValue().getBlue() / 255f);
            effect.setUniformValue("fthird", fillColor3.getValue().getRed() / 255f, fillColor3.getValue().getGreen() / 255f, fillColor3.getValue().getBlue() / 255f);
            effect.setUniformValue("oct", octaves.getValueInt());
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += speed.getValueFloat() * 0.002;
        } else if (shader == Shader.Default) {
            effect.setUniformValue("alpha0", !glow ? -1.0f : outlineColor.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("lineWidth", lineWidth.getValueInt());
            effect.setUniformValue("quality", quality);
            effect.setUniformValue("color", fill.getValue().getRed() / 255f, fill.getValue().getGreen() / 255f, fill.getValue().getBlue() / 255f, fill.getValue().getAlpha() / 255f);
            effect.setUniformValue("outlinecolor", outlineColor.getValue().getRed() / 255f, outlineColor.getValue().getGreen() / 255f, outlineColor.getValue().getBlue() / 255f, outlineColor.getValue().getAlpha() / 255f);
            effect.render(mc.getTickDelta());
        } else if (shader == Shader.Snow) {
            effect.setUniformValue("color", fill.getValue().getRed() / 255f, fill.getValue().getGreen() / 255f, fill.getValue().getBlue() / 255f, fill.getValue().getAlpha() / 255f);
            effect.setUniformValue("quality", quality);
            effect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", time);
            effect.render(mc.getTickDelta());
            time += speed.getValueFloat() * 0.002;
        }
    }

    public void setupShader(Shader shader, ManagedShaderEffect effect) {
        ShaderChams shaderChams = ShaderChams.INSTANCE;
        setup(shader, effect, shaderChams.glow.getValue(), shaderChams.outlineColor, shaderChams.fillAlpha, shaderChams.alpha2, shaderChams.lineWidth, shaderChams.octaves, shaderChams.quality.getValueInt(), shaderChams.factor, shaderChams.gradient, shaderChams.speed, shaderChams.smokeGlow, shaderChams.smokeGlow1, shaderChams.fill, shaderChams.fillColor2, shaderChams.fillColor3);
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/snow.json"));

        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/snow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;

            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).rebirth_nextgen_master$addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
    }

    public static class MyFramebuffer extends Framebuffer {
        public MyFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            resize(width, height, true);
            setClearColor(0f, 0f, 0f, 0f);
        }
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null || shaderBuffer == null) {
            if (mc.getFramebuffer() == null) return true;
            shaderBuffer = new MyFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
            reloadShaders();
            return true;
        }

        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public enum Shader {
        Default,
        Smoke,
        Gradient,
        Snow
    }
}
