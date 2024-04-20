package me.rebirthclient.mod.modules.client;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.HudManager;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.mod.gui.components.Component;
import me.rebirthclient.mod.gui.components.impl.BooleanComponent;
import me.rebirthclient.mod.gui.components.impl.ColorComponents;
import me.rebirthclient.mod.gui.components.impl.ModuleComponent;
import me.rebirthclient.mod.gui.components.impl.SliderComponent;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;

import java.awt.*;

public class ClickGui extends Module {
	public static ClickGui INSTANCE;
	private final EnumSetting page = add(new EnumSetting("Page", Pages.General));
	public BooleanSetting gear = add(new BooleanSetting("GearToggle", v -> page.getValue() == Pages.General));;
	public EnumSetting mode = add(new EnumSetting("ToggleAnimation", Mode.Reset, v -> page.getValue() == Pages.General));
	public BooleanSetting snow = add(new BooleanSetting("Snow", v -> page.getValue() == Pages.General));
	public SliderSetting animationSpeed = add(new SliderSetting("AnimationSpeed", 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public SliderSetting booleanSpeed = add(new SliderSetting("BooleanSpeed", 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public BooleanSetting customFont = add(new BooleanSetting("CustomFont", v -> page.getValue() == Pages.General));
	public ColorSetting color = add(new ColorSetting("Main", new Color(140, 146, 255), v -> page.getValue() == Pages.Color));
	public ColorSetting gearColor = add(new ColorSetting("Gear", new Color(150, 150, 150), v -> page.getValue() == Pages.Color));
	public ColorSetting mbgColor = add(new ColorSetting("Module", new Color(24, 24, 24, 42), v -> page.getValue() == Pages.Color));
	public ColorSetting mhColor = add(new ColorSetting("ModuleHover", new Color(152, 152, 152, 123), v -> page.getValue() == Pages.Color));
	public ColorSetting sbgColor = add(new ColorSetting("Setting", new Color(24, 24, 24, 42), v -> page.getValue() == Pages.Color));
	public ColorSetting shColor = add(new ColorSetting("SettingHover", new Color(152, 152, 152, 123), v -> page.getValue() == Pages.Color));
	public ColorSetting bgColor = add(new ColorSetting("Background", new Color(24, 24, 24, 42), v -> page.getValue() == Pages.Color));
	public ClickGui() {
		super("ClickGui", Category.Client);
		INSTANCE = this;
	}

	public static final FadeUtils fade = new FadeUtils(400);

	@Override
	public void onUpdate() {
		if (!(mc.currentScreen instanceof ClickGuiScreen)) {
			disable();
		}
	}

	@Override
	public void onEnable() {
		if (mode.getValue() == Mode.Reset) {
			for (ClickGuiTab tab : Rebirth.HUD.tabs) {
				for (Component component : tab.getChildren()) {
					component.currentOffset = 0;
					if (component instanceof ModuleComponent moduleComponent) {
						moduleComponent.isPopped = false;
						for (Component settingComponent : moduleComponent.getSettingsList()) {
							settingComponent.currentOffset = 0;
							if (settingComponent instanceof SliderComponent sliderComponent) {
								sliderComponent.renderSliderPosition = 0;
							} else if (settingComponent instanceof BooleanComponent booleanComponent) {
								booleanComponent.currentWidth = 0;
							} else if (settingComponent instanceof ColorComponents colorComponents) {
								colorComponents.currentWidth = 0;
							}
						}
					}
				}
				tab.currentHeight = 0;
			}
		}
		fade.reset();
		if (nullCheck()) {
			disable();
			return;
		}
		mc.setScreen(HudManager.clickGui);
	}

	@Override
	public void onDisable() {
		if (mc.currentScreen instanceof ClickGuiScreen) {
			mc.setScreen(null);
		}
	}

	public enum Mode {
		Scale, Pull, Scissor, Reset, None
	}

	private enum Pages {
		General,
		Color
	}
}