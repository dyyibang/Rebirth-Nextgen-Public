package me.rebirthclient.api.managers;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.gui.components.impl.ModuleComponent;
import me.rebirthclient.mod.gui.elements.ArmorHUD;
import me.rebirthclient.mod.gui.elements.IngameGUI;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.gui.tabs.OptionsTab;
import me.rebirthclient.mod.gui.tabs.Tab;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.Module.Category;
import me.rebirthclient.mod.modules.client.ClickGui;
import me.rebirthclient.mod.settings.Placement;
import me.rebirthclient.mod.settings.Setting;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class HudManager extends Mod {

	public ArrayList<ClickGuiTab> tabs = new ArrayList<>();
	public static ClickGuiScreen clickGui = new ClickGuiScreen();
	public static IngameGUI hud;
	public ArmorHUD armorHud;
	public static Tab currentGrabbed = null;
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private int mouseX;
	private int mouseY;
	public OptionsTab optionsTab;

	private final EnumSetting page;
	//Combat
	public BooleanSetting rotatePlus;
	public BooleanSetting rotations;
	public EnumSetting placement;
	public SliderSetting rotateTime;
	public SliderSetting attackDelay;
	public BooleanSetting inventorySync;
	public BooleanSetting obsMode;

	//HUD
	public final EnumSetting hackName;
	public BooleanSetting tabGui;
	public BooleanSetting ah;

	public HudManager() {
		super("HudManager");
		ModuleManager.lastLoadMod = this;
		page = new EnumSetting("Page", Pages.Combat);

		hackName = new EnumSetting("HackName", HackName.RebirthNew, v -> page.getValue() == Pages.HUD);
		tabGui = new BooleanSetting("TabGui", v -> page.getValue() == Pages.HUD);
		ah = new BooleanSetting("ArmorHUD", v -> page.getValue() == Pages.HUD);

		rotations = new BooleanSetting("ShowRotations", v -> page.getValue() == Pages.Combat);
		placement = new EnumSetting("Placement", Placement.Vanilla, v -> page.getValue() == Pages.Combat);
		rotateTime = new SliderSetting("RotateTime", 0, 1, 0.01, v -> page.getValue() == Pages.Combat);
		attackDelay = new SliderSetting("AttackDelay", 0, 1, 0.01, v -> page.getValue() == Pages.Combat);
		inventorySync = new BooleanSetting("InventorySync", v -> page.getValue() == Pages.Combat);
		obsMode = new BooleanSetting("OBSServer", v -> page.getValue() == Pages.Combat);
		rotatePlus = new BooleanSetting("RotateSync", v -> page.getValue() == Pages.Combat);

		hud = new IngameGUI();
		armorHud = new ArmorHUD();
		optionsTab = new OptionsTab("Options", 10, 500);
		try {
			for (Field field : HudManager.class.getDeclaredFields()) {
				if (!Setting.class.isAssignableFrom(field.getType()))
					continue;
				Setting setting = (Setting) field.get(this);
				optionsTab.addChild(setting);
			}
		} catch (Exception e) {
		}
		//radarTab = new RadarTab("Radar", 70, 250);

		tabs.add(optionsTab);
		//tabs.add(radarTab);
		int xOffset = 200;
		for (Category category : Module.Category.values()) {
			ClickGuiTab tab = new ClickGuiTab(category, xOffset, 1);
			for (Module module : Rebirth.MODULE.modules) {
				if (module.getCategory() == category) {
					ModuleComponent button = new ModuleComponent(module.getName(), tab, module);
					tab.addChild(button);
				}
			}
			tabs.add(tab);
			xOffset += tab.getWidth() + 20;
		}
	}
	
	public Color getColor() {
		return ClickGui.INSTANCE.color.getValue();
	}
	
	public void update() {
		if (isClickGuiOpen()) {
			for (ClickGuiTab tab : tabs) {
				tab.update(mouseX, mouseY, ClickGuiScreen.clicked);
			}
			if (ah.getValue()) {
				armorHud.update(mouseX, mouseY, ClickGuiScreen.clicked);
			}
		}
	}

	public void draw(DrawContext drawContext, float tickDelta) {
		MatrixStack matrixStack = drawContext.getMatrices();
		boolean mouseClicked = ClickGuiScreen.clicked;
		mouseX = (int) Math.ceil(mc.mouse.getX());
		mouseY = (int) Math.ceil(mc.mouse.getY());
		hud.update(mouseX, mouseY, mouseClicked);
		if (this.isClickGuiOpen()) {
			int dx = (int) (double) mouseX;
			int dy = (int) (double) mouseY;
			if (!mouseClicked)
				currentGrabbed = null;
			if (currentGrabbed != null)
				currentGrabbed.moveWindow((lastMouseX - dx), (lastMouseY - dy));
			this.lastMouseX = dx;
			this.lastMouseY = dy;
		}
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		hud.draw(drawContext, tickDelta, getColor());
		matrixStack.push();
		matrixStack.scale(1.0f / mc.options.getGuiScale().getValue(), 1.0f / mc.options.getGuiScale().getValue(), 1.0f);
		if (ah.getValue()) armorHud.draw(drawContext, tickDelta, getColor());
		if (isClickGuiOpen()) {
			double quad = ClickGui.fade.easeOutQuad();
			if (quad < 1) {
				switch ((ClickGui.Mode) ClickGui.INSTANCE.mode.getValue()) {
					case Pull -> {
						quad = 1 - quad;
						matrixStack.translate(0, -100 * quad, 0);
					}
					case Scale -> matrixStack.scale((float) quad, (float) quad, 1);
					case Scissor -> setScissorRegion(0, 0, mc.getWindow().getWidth(), (int) (mc.getWindow().getHeight() * quad));
				}
			}
			for (ClickGuiTab tab : tabs) {
				tab.draw(drawContext, tickDelta, getColor());
			}
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		matrixStack.pop();

		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	public void setScissorRegion(int x, int y, int width, int height) {
		double scaledY = (mc.getWindow().getHeight() - (y + height));
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x, (int) scaledY, width, height);
	}
	public boolean isClickGuiOpen() {
		return mc.currentScreen instanceof ClickGuiScreen;
	}

	public enum HackName {
		MoonEmoji,
		StarEmoji,
		RebirthNew,
		Rebirth,
		MoonGod,
		Mio
	}
	private enum Pages {
		HUD,
		Combat
	}
}
