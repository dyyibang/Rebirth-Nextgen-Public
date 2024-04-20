package me.rebirthclient.mod.gui.screens;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.gui.particle.Snow;
import me.rebirthclient.mod.gui.tabs.Tab;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.client.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Random;

public class ClickGuiScreen extends Screen implements Wrapper {

    public ClickGuiScreen() {
        super(Text.of("ClickGui"));
    }
    private final ArrayList<Snow> snow = new ArrayList<>();
    public static boolean clicked = false;
    public static boolean rightClicked = false;
    public static boolean hoverClicked = false;

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
        renderBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, partialTicks);
        if (ClickGui.INSTANCE.snow.getValue()) this.snow.forEach(snow -> snow.drawSnow(drawContext));
        Rebirth.HUD.draw(drawContext, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            hoverClicked = false;
            clicked = true;
        } else if (button == 1) {
            rightClicked = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            clicked = false;
            hoverClicked = false;
        } else if (button == 1) {
            rightClicked = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        super.close();
        rightClicked = false;
        hoverClicked = false;
        clicked = false;
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        snow.clear();
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            for (int y = 0; y < 3; ++y) {
                Snow snow = new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1);
                this.snow.add(snow);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        for (Tab tab : Rebirth.HUD.tabs) {
            tab.setY((int) (tab.getY() + (amount * 30)));
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
