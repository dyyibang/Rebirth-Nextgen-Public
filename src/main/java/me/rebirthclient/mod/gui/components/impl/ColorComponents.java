package me.rebirthclient.mod.gui.components.impl;

import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.ColorUtil;
import me.rebirthclient.api.util.MathUtil;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.client.ClickGui;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
public class ColorComponents extends Component {
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    private boolean afocused;
    private boolean hfocused;
    private boolean sbfocused;

    private float spos, bpos, hpos, apos;

    private Color prevColor;

    private boolean firstInit;

    private final ColorSetting colorSetting;

    public ColorSetting getColorSetting() {
        return colorSetting;
    }

    public ColorComponents(ClickGuiTab parent, ColorSetting setting) {
        super();
        this.parent = parent;
        this.colorSetting = setting;
        prevColor = getColorSetting().getValue();
        updatePos();
        firstInit = true;
    }

    @Override
    public boolean isVisible() {
        if (colorSetting.visibility != null) {
            return colorSetting.visibility.test(null);
        }
        return true;
    }
    private void updatePos() {
        float[] hsb = Color.RGBtoHSB(getColorSetting().getValue().getRed(), getColorSetting().getValue().getGreen(), getColorSetting().getValue().getBlue(), null);
        hue = -1 + hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = getColorSetting().getValue().getAlpha();
    }

    private void setColor(Color color) {
        getColorSetting().setValue(color.getRGB());
        prevColor = color;
    }

    boolean clicked = false;
    boolean popped = false;
    double currentHeight = defaultHeight;
    boolean hover = false;
    @Override
    public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
        int x = parent.getX();
        int y = (int) (parent.getY() + currentOffset) - 2;
        int width = parent.getWidth();
        double cx = x + 6;
        double cy = y + defaultHeight;
        double cw = width - 38;
        double ch = getHeight() - 34;
        rainbowHovered = Render2DUtil.isHovered(mouseX, mouseY, x + width - 81, y + 4, 40, 10);
        hover = Render2DUtil.isHovered(mouseX, mouseY, (float) x + 5, (float) y + 2, (float) width - 10, (float) 28);
        if (hover) {
            if (HudManager.currentGrabbed == null && isVisible()) {
                if (ClickGuiScreen.rightClicked) {
                    ClickGuiScreen.rightClicked = false;
                    this.popped = !this.popped;
                }
            }
        }
        if (popped) {
            setHeight(90 + defaultHeight);
        } else {
            setHeight(defaultHeight);
        }
        if (mouseClicked || ClickGuiScreen.hoverClicked) {
            if (!clicked) {
                if (Render2DUtil.isHovered(mouseX, mouseY, cx + cw + 17, cy, 8, ch)) afocused = true;
                if (Render2DUtil.isHovered(mouseX, mouseY, cx + cw + 4, cy, 8, ch)) hfocused = true;
                if (Render2DUtil.isHovered(mouseX, mouseY, cx, cy, cw, ch)) sbfocused = true;
                if (HudManager.currentGrabbed == null && isVisible()) {
                    if (rainbowHovered) getColorSetting().setRainbow(!getColorSetting().isRainbow);
                    else if (hover && getColorSetting().injectBoolean)
                        getColorSetting().booleanValue = !getColorSetting().booleanValue;
                }
            }
            clicked = true;
            ClickGuiScreen.hoverClicked = true;
            mouseClicked = false;
        } else {
            clicked = false;
            sbfocused = false;
            afocused = false;
            hfocused = false;
        }
        if (!popped) return;
        if (HudManager.currentGrabbed == null && isVisible()) {
            Color value = Color.getHSBColor(hue, saturation, brightness);
            if (sbfocused) {
                saturation = (float) ((MathUtil.clamp((float) (mouseX - cx), 0f, (float) cw)) / cw);
                brightness = (float) ((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
                value = Color.getHSBColor(hue, saturation, brightness);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }

            if (hfocused) {
                hue = (float) -((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
                value = Color.getHSBColor(hue, saturation, brightness);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }

            if (afocused) {
                alpha = (int) (((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch) * 255);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }
        }
    }

    boolean rainbowHovered = false;

    public double currentWidth = 0;
    @Override
    public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
        currentHeight = animate(currentHeight, back ? defaultHeight : getHeight());
        currentOffset = animate(currentOffset, offset);
        if (back && Math.abs(currentOffset - offset) <= 0.5) {
            currentWidth = 0;
            return false;
        }
        int x = parent.getX();
        int y = (int) (parent.getY() + currentOffset - 2);
        int width = parent.getWidth();
        MatrixStack matrixStack = drawContext.getMatrices();

        Render2DUtil.drawRect(matrixStack, (float) x + 3, (float) y + 1, (float) width - 6, (float) currentHeight - 2, hover ? ClickGui.INSTANCE.shColor.getValue() : ClickGui.INSTANCE.sbgColor.getValue());

        if (colorSetting.injectBoolean) {
            currentWidth = animate(currentWidth, colorSetting.booleanValue ? (width - 6D) : 0D, ClickGui.INSTANCE.booleanSpeed.getValue());
            Render2DUtil.drawRect(matrixStack, (float) x + 3, (float) y + 1, (float) currentWidth, (float) currentHeight - 2, hover ? color.brighter() : color);
        }
        TextUtil.drawCustomText(drawContext, colorSetting.getName(), x + 10, y + 8, new Color(-1).getRGB());

        Render2DUtil.drawRound(matrixStack, (float) (x + width - 34), (float) (y + 8), 22, 14, 1, ColorUtil.injectAlpha(getColorSetting().getValue(), 255));
        TextUtil.drawCustomSmallText(drawContext, "Rainbow", x + width - 79, y + 6, getColorSetting().isRainbow ? color.getRGB() : (rainbowHovered ? new Color(0xA3FFFFFF, true).getRGB() : Color.WHITE.getRGB()));

        if (back) return true;
        if (!popped && Math.abs(currentHeight - getHeight()) <= 0.2) {
            return true;
        }
        double cx = x + 6;
        double cy = y + defaultHeight;
        double cw = width - 38;
        double ch = currentHeight - 32;

        if (prevColor != getColorSetting().getValue()) {
            updatePos();
            prevColor = getColorSetting().getValue();
        }

        if (firstInit) {
            spos = (float) ((cx + cw) - (cw - (cw * saturation)));
            bpos = (float) ((cy + (ch - (ch * brightness))));
            hpos = (float) ((cy + (ch - 3 + ((ch - 3) * hue))));
            apos = (float) ((cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))));
            firstInit = false;
        }

        spos = (float) animate(spos, (float) ((cx + cw) - (cw - (cw * saturation))), .6f);
        bpos = (float) animate(bpos, (float) (cy + (ch - (ch * brightness))), .6f);
        hpos = (float) animate(hpos, (float) (cy + (ch - 3 + ((ch - 3) * hue))), .6f);
        apos = (float) animate(apos, (float) (cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))), .6f);

        Color colorA = Color.getHSBColor(hue, 0.0F, 1.0F), colorB = Color.getHSBColor(hue, 1.0F, 1.0F);
        Color colorC = new Color(0, 0, 0, 0), colorD = new Color(0, 0, 0);

        Render2DUtil.horizontalGradient(matrixStack, (float) cx + 2, (float) cy, (float) (cx + cw), (float) (cy + ch), colorA, colorB);
        Render2DUtil.verticalGradient(matrixStack, (float) (cx + 2), (float) cy, (float) (cx + cw), (float) (cy + ch), colorC, colorD);

        for (float i = 1f; i < ch - 2f; i += 1f) {
            float curHue = (float) (1f / (ch / i));
            Render2DUtil.drawRect(matrixStack, (float) (cx + cw + 4), (float) (cy + i), 8, 1, Color.getHSBColor(curHue, 1f, 1f));
        }

        Render2DUtil.drawRect(matrixStack, (float) (cx + cw + 17), (float) (cy + 1f), 8f, (float) (ch - 3), new Color(0xFFFFFFFF));

        Render2DUtil.verticalGradient(matrixStack, (float) (cx + cw + 17), (float) (cy + 0.8f), (float) (cx + cw + 25), (float) (cy + ch - 2), new Color(getColorSetting().getValue().getRed(), getColorSetting().getValue().getGreen(), getColorSetting().getValue().getBlue(), 255), new Color(0, 0, 0, 0));

        Render2DUtil.drawRect(matrixStack, (float) (cx + cw + 3), hpos + 0.5f, 10, 1, Color.WHITE);
        Render2DUtil.drawRect(matrixStack, (float) (cx + cw + 16), apos + 0.5f, 10, 1, Color.WHITE);
        Render2DUtil.drawRound(matrixStack, spos - 1.5f, bpos - 1.5f, 3, 3, 1.5f, new Color(-1));
        return true;
    }
}
