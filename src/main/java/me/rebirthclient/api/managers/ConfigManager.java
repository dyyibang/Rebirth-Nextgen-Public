/**
 * A class to represent a basic Settings file manager.
 */
package me.rebirthclient.api.managers;

import com.google.common.base.Splitter;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.gui.tabs.ClickGuiTab;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.*;
import me.rebirthclient.mod.settings.impl.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigManager implements Wrapper {
	public final ArrayList<Setting> SETTINGS = new ArrayList<>();
	private final File rebirthOptions;
	private final Hashtable<String, String> settings = new Hashtable<>();

	public ConfigManager() {
		rebirthOptions = new File(mc.runDirectory, "rebirth_options.txt");
		readSettings();
	}

	public void loadSettings() {
		for (Setting setting : Rebirth.CONFIG.SETTINGS) {
			setting.loadSetting();
		}
		// Write Module Settings
		for (Module module : Rebirth.MODULE.modules) {
			for (Setting setting : module.getSettings()) {
				setting.loadSetting();
			}
			module.setState(Rebirth.CONFIG.getSettingBoolean(module.getName() + "_state", false));
		}
	}
	public void saveSettings() {
		PrintWriter printwriter = null;
		try {
			printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rebirthOptions), StandardCharsets.UTF_8));

			printwriter.println("prefix:" + Rebirth.PREFIX);
			// Write HUD information and 'other' settings.
			printwriter.println("ingame_x:" + HudManager.hud.getX());
			printwriter.println("ingame_y:" + HudManager.hud.getY());

			for (ClickGuiTab tab : Rebirth.HUD.tabs) {
				printwriter.println(tab.getTitle() + "_x:" + tab.getX());
				printwriter.println(tab.getTitle() + "_y:" + tab.getY());
			}
			printwriter.println("armor_x:" + Rebirth.HUD.armorHud.getX());
			printwriter.println("armor_y:" + Rebirth.HUD.armorHud.getY());

			for (Setting setting : Rebirth.CONFIG.SETTINGS) {
				if (setting instanceof BooleanSetting bs) {
					printwriter.println(bs.getLine() + ":" + bs.getValue());
				}else if (setting instanceof SliderSetting ss) {
					printwriter.println(ss.getLine() + ":" + ss.getValue());
				} else if (setting instanceof BindSetting bs) {
					printwriter.println(bs.getLine() + ":" + bs.getKey());
				} else if (setting instanceof EnumSetting es) {
					printwriter.println(es.getLine() + ":" + es.getValue().name());
				} else if (setting instanceof ColorSetting cs) {
					printwriter.println(cs.getLine() + ":" + cs.getValue().getRGB());
					printwriter.println(cs.getLine() + "Rainbow:" + cs.isRainbow);
					if (cs.injectBoolean) {
						printwriter.println(cs.getLine() + "Boolean:" + cs.booleanValue);
					}
				}
			}
			// Write Module Settings
			for (Module module : Rebirth.MODULE.modules) {
				for (Setting setting : module.getSettings()) {
					if (setting instanceof BooleanSetting bs) {
						printwriter.println(bs.getLine() + ":" + bs.getValue());
					}else if (setting instanceof SliderSetting ss) {
						printwriter.println(ss.getLine() + ":" + ss.getValue());
					} else if (setting instanceof BindSetting bs) {
						printwriter.println(bs.getLine() + ":" + bs.getKey());
					} else if (setting instanceof EnumSetting es) {
						printwriter.println(es.getLine() + ":" + es.getValue().name());
					} else if (setting instanceof ColorSetting cs) {
						printwriter.println(cs.getLine() + ":" + cs.getValue().getRGB());
						printwriter.println(cs.getLine() + "Rainbow:" + cs.isRainbow);
						if (cs.injectBoolean) {
							printwriter.println(cs.getLine() + "Boolean:" + cs.booleanValue);
						}
					}
				}
				printwriter.println(module.getName() + "_state:" + module.isOn());
			}
		} catch (Exception exception) {
			System.out.println("[" + Rebirth.LOG_NAME + "] Failed to save settings");
		} finally {
			IOUtils.closeQuietly(printwriter);
		}
	}

	public void readSettings() {
		final Splitter COLON_SPLITTER = Splitter.on(':');
		try {
			if (!this.rebirthOptions.exists()) {
				return;
			}
			List<String> list = IOUtils.readLines(new FileInputStream(this.rebirthOptions), StandardCharsets.UTF_8);
			for (String s : list) {
				try {
					Iterator<String> iterator = COLON_SPLITTER.limit(2).split(s).iterator();
					settings.put(iterator.next(), iterator.next());
				} catch (Exception var10) {
					System.out.println("Skipping bad option: " + s);
				}
			}
			//KeyBinding.updateKeysByCode();
		} catch (Exception exception) {
			System.out.println("[" + Rebirth.LOG_NAME + "] Failed to load settings");
		}
	}

	public static boolean isInteger(final String str) {
		final Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	public static boolean isFloat(String str) {
		String pattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
		return str.matches(pattern);
	}
	public int getSettingInt(String setting, int defaultValue) {
		String s = settings.get(setting);
		if(s == null || !isInteger(s)) return defaultValue;
		return Integer.parseInt(s);
	}

	public float getSettingFloat(String setting, float defaultValue) {
		String s = settings.get(setting);
		if (s == null || !isFloat(s)) return defaultValue;
		return Float.parseFloat(s);
	}
	public boolean getSettingBoolean(String setting) {
		String s = settings.get(setting);
		return Boolean.parseBoolean(s);
	}

	public boolean getSettingBoolean(String setting, boolean defaultValue) {
		if (settings.get(setting) != null) {
			String s = settings.get(setting);
			return Boolean.parseBoolean(s);
		} else {
			return defaultValue;
		}
	}
	
	public String getSettingString(String setting) {
		return settings.get(setting);
	}

	public String getSettingString(String setting, String defaultValue) {
		if (settings.get(setting) == null) {
			return defaultValue;
		}
		return settings.get(setting);
	}
}
