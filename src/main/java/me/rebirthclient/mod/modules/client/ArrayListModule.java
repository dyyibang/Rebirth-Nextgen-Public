package me.rebirthclient.mod.modules.client;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.util.ColorUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListModule extends Module {
    public ArrayListModule() {
        super("ArrayList", "", Category.Client);
        INSTANCE = this;
    }

    public static ArrayListModule INSTANCE;
    private final SliderSetting height = add(new SliderSetting("Height", 0, 10));
    private final SliderSetting yOffset = add(new SliderSetting("FontOffset", 0, 10));
    private final SliderSetting listX = add(new SliderSetting("X", 0, 500));
    private final SliderSetting listY = add(new SliderSetting("Y", 0, 500));
    private final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 0, 1000));
    private final BooleanSetting forgeHax = add(new BooleanSetting("ForgeHax"));
    private final BooleanSetting reverse = add(new BooleanSetting("Reverse"));
    private final BooleanSetting down = add(new BooleanSetting("Down"));
    private final BooleanSetting onlyBind = add(new BooleanSetting("OnlyBind"));
    private final BooleanSetting animationY = add(new BooleanSetting("AnimationY"));
    private final EnumSetting colorMode = add(new EnumSetting("ColorMode", ColorMode.Pulse));
    private final SliderSetting rainbowSpeed = add(new SliderSetting("RainbowSpeed", 1, 400, v -> colorMode.getValue() == ColorMode.Rainbow || colorMode.getValue() == ColorMode.PulseRainbow));
    private final SliderSetting saturation = add(new SliderSetting("Saturation", 1.0f, 255.0f, v -> colorMode.getValue() == ColorMode.Rainbow || colorMode.getValue() == ColorMode.PulseRainbow));
    private final SliderSetting pulseSpeed = add(new SliderSetting("PulseSpeed", 1, 400, v -> colorMode.getValue() == ColorMode.Pulse || colorMode.getValue() == ColorMode.PulseRainbow));
    private final SliderSetting rainbowDelay = add(new SliderSetting("Delay", 0, 600, v -> colorMode.getValue() == ColorMode.Rainbow));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 255), v -> colorMode.getValue() != ColorMode.Rainbow));
    private final BooleanSetting rect = add(new BooleanSetting("Rect"));
    private final BooleanSetting backGround = add(new BooleanSetting("BackGround").setParent());
    private final BooleanSetting bgSync = add(new BooleanSetting("Sync", v -> backGround.isOpen()));
    private final ColorSetting bgColor = add(new ColorSetting("BGColor", new Color(0, 0, 0, 100), v -> backGround.isOpen()));
    private List<Modules> modulesList = new java.util.ArrayList<>();

    private enum ColorMode {
        Custom,
        Pulse,
        Rainbow,
        PulseRainbow,
    }

    boolean update = true;
    @Override
    public void onUpdate() {
        if (update) {
            for (Module module : Rebirth.MODULE.modules) {
                modulesList.add(new Modules(module));
            }
            modulesList = modulesList.stream().sorted(Comparator.comparing(module -> getStringWidth(module.module.getName()) * (-1))).collect(Collectors.toList());
            update = false;
        }
        progress -= rainbowSpeed.getValueInt();
        pulseProgress -= pulseSpeed.getValueInt();
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        int lastY = down.getValue() ? mc.getWindow().getHeight() / 2 - listY.getValueInt() - getFontHeight() : listY.getValueInt();
        int counter = 20;
        for (Modules modules : modulesList) {
            if (onlyBind.getValue() && modules.module.getBind().getKey() == -1) continue;
            modules.fade.setLength(animationTime.getValueInt());
            if (modules.module.isOn()) {
                modules.enable();
            } else {
                modules.disable();
            }
            int x;
            int y;
            double size;
            if (!reverse.getValue()) {
                if (modules.isEnabled) {
                    size = Math.min(modules.fade.easeOutQuad(), 1);
                    x = (int) ((getStringWidth(getSuffix(modules.module.getName()))) * size);
                    y = (int) (getFontHeight() * size);
                    modules.lastY = y;
                    modules.lastX = x;
                    modules.lastSize = size;
                } else {
                    size = 1 - Math.min(modules.fade.easeOutQuad(), 1);
                    x = (int) (modules.lastX * size);
                    y = (int) (modules.lastY * size);
                    if (size <= 0) continue;
                }
            } else {
                if (modules.isEnabled) {
                    size = Math.abs(modules.fade.easeOutQuad() - 1);
                    x = (int) (getStringWidth(getSuffix(modules.module.getName())) * size);
                    size = modules.fade.easeOutQuad();
                    y = (int) (getFontHeight() * size);
                    modules.lastY = y;
                    modules.lastX = x;
                } else {
                    size = modules.fade.easeOutQuad();
                    x = (int) ((getStringWidth(getSuffix(modules.module.getName()))) * size) + modules.lastX;
                    size = Math.abs(modules.fade.easeOutQuad() - 1);
                    y = (int) (modules.lastY * size);
                    if (size <= 0) continue;
                    if (x >= getStringWidth(getSuffix(modules.module.getName()))) continue;
                }
            }
            counter = counter + 1;
            int showX;
            if (!reverse.getValue()) {
                showX = mc.getWindow().getWidth() / 2 - x - listX.getValueInt() - (rect.getValue() ? 2 : 0);
                if (backGround.getValue()) {
                    Render2DUtil.drawRect(drawContext.getMatrices(), showX - 1,
                            lastY - (animationY.getValue() ? Math.abs(y - getFontHeight()) : 0) - 1,
                            ((float) mc.getWindow().getWidth() / 2 - listX.getValueInt() + 1) - showX + 1,
                            getFontHeight() + height.getValueInt(),
                            bgSync.getValue() ? ColorUtil.injectAlpha(getColor(counter), bgColor.getValue().getAlpha()) : bgColor.getValue().getRGB());
                }
                if (rect.getValue()) {
                    Render2DUtil.drawRect(drawContext.getMatrices(), (float) mc.getWindow().getWidth() /2 - listX.getValueInt() - 1,
                            lastY - (animationY.getValue() ? Math.abs(y - getFontHeight()) : 0) - 1,
                            1,
                            getFontHeight() + height.getValueInt(),
                            getColor(counter));
                }
            } else {
                showX = -x + listX.getValueInt() + (rect.getValue() ? 2 : 0);
                if (backGround.getValue()) {
                    Render2DUtil.drawRect(drawContext.getMatrices(), listX.getValueInt(),
                            lastY - (animationY.getValue() ? Math.abs(y - getFontHeight()) : 0) - 1,
                            Math.abs(x - getStringWidth(getSuffix(modules.module.getName()))) + (rect.getValue() ? 2 : 0) + 1,
                            getFontHeight() + height.getValueInt(),
                            bgSync.getValue() ? ColorUtil.injectAlpha(getColor(counter), bgColor.getValue().getAlpha()) : bgColor.getValue().getRGB());
                }
                if (rect.getValue()) {
                    Render2DUtil.drawRect(drawContext.getMatrices(), listX.getValueInt(),
                            lastY - (animationY.getValue() ? Math.abs(y - getFontHeight()) : 0) - 1,
                            1,
                            getFontHeight() + height.getValueInt(),
                            getColor(counter));
                }
            }
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            int tWidth = getStringWidth(getSuffix(modules.module.getName()));
            if (!reverse.getValue()) {
                GL11.glScissor((mc.getWindow().getWidth() / 2 - tWidth - listX.getValueInt() - (rect.getValue() ? 2 : 0)) * 2, 0, tWidth * 2, mc.getWindow().getHeight());
            } else {
                GL11.glScissor(listX.getValueInt() * 2, 0, tWidth * 2 + 2, mc.getWindow().getHeight());
            }
            drawContext.drawTextWithShadow(mc.textRenderer, getSuffix(modules.module.getName()), showX, lastY - (animationY.getValue() ? Math.abs(y - getFontHeight()) : 0) + yOffset.getValueInt(), getColor(counter));
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            if (modules.isEnabled) {
                size = Math.min(modules.fade.easeOutQuad(), 1);
            } else {
                size = 1 - Math.min(modules.fade.easeOutQuad(), 1) * modules.lastSize;
            }
            if (down.getValue()) {
                lastY -= (getFontHeight() + height.getValueInt()) * size;
            } else {
                lastY += (getFontHeight() + height.getValueInt()) * size;
            }
        }
    }

    private String getSuffix(String s) {
        if (forgeHax.getValue()) {
            if (reverse.getValue()) {
                return "\u00a7r>" + s;
            } else {
                return s + "\u00a7r<";
            }
        }
        return s;
    }

    private int getColor(int counter) {
        if (colorMode.getValue() != ColorMode.Custom) {
            return rainbow(counter).getRGB();
        }
        return color.getValue().getRGB();
    }

    int progress = 0;
    int pulseProgress = 0;

    private Color rainbow(int delay) {
        double rainbowState = Math.ceil((progress + delay * rainbowDelay.getValue()) / 20.0);
        if (colorMode.getValue() == ColorMode.Pulse) {
            return pulseColor(color.getValue(), delay);
        } else if (colorMode.getValue() == ColorMode.Rainbow) {
            return Color.getHSBColor((float) (rainbowState % 360.0 / 360), saturation.getValueFloat() / 255.0f, 1.0f);
        } else {
            return pulseColor(Color.getHSBColor((float) (rainbowState % 360.0 / 360), saturation.getValueFloat() / 255.0f, 1.0f), delay);
        }
    }

    private Color pulseColor(Color color, int index) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((pulseProgress % ((long) 1230675006 ^ 0x495A9BEEL) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + index / (float) 14 * Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923f) ^ 0x7E97F9B3) + Float.intBitsToFloat(Float.floatToIntBits(2.7958195f) ^ 0x7F32EEB5) * brightness;
        hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424);
        return ColorUtil.injectAlpha(new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])), color.getAlpha());
    }

    private int getStringWidth(String text) {
        return mc.textRenderer.getWidth(text);
    }
    
    private int getFontHeight() {
        return mc.textRenderer.fontHeight;
    }
    
    public static class Modules {
        public final FadeUtils fade;
        public boolean isEnabled = false;
        public final Module module;
        public int lastX = 0;
        public int lastY = 0;
        public double lastSize = 0;

        public Modules(Module module) {
            this.module = module;
            this.fade = new FadeUtils(500L).reset();
        }

        public void enable() {
            if (isEnabled) return;
            isEnabled = true;
            fade.reset();
        }

        public void disable() {
            if (!isEnabled) return;
            isEnabled = false;
            fade.reset();
        }
    }
}
