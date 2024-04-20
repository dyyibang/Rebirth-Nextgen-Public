package me.rebirthclient;

import me.rebirthclient.api.events.eventbus.EventBus;
import me.rebirthclient.api.managers.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Session;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Rebirth implements ModInitializer {

	@Override
	public void onInitialize()
	{
		load();
	}

	public static final String LOG_NAME = "Rebirth";
	public static final String VERSION = "1.5";
	public static String PREFIX = ";";
	public static final EventBus EVENT_BUS = new EventBus();
	// Systems
	public static ModuleManager MODULE;
	public static CommandManager COMMAND;
	public static AltManager ALT;
	public static HudManager HUD;
	public static ConfigManager CONFIG;
	public static RunManager RUN;
	public static BreakManager BREAK;
	public static PopManager POP;
	public static FriendManager FRIEND;
	public static TimerManager TIMER;
	public static ShaderManager SHADER;
	public static boolean loaded = false;

	public static void update() {
		if (!loaded) return;
		MODULE.onUpdate();
		HUD.update();
		POP.update();
	}

	public static void drawHUD(DrawContext context, float partialTicks) {
		if (!loaded) return;
		if (!HUD.isClickGuiOpen()) HUD.draw(context, partialTicks);
	}

	public static void load() {
		System.out.println("[" + Rebirth.LOG_NAME + "] Starting Client");
		System.out.println("[" + Rebirth.LOG_NAME + "] Register eventbus");
		EVENT_BUS.registerLambdaFactory("me.rebirthclient", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
		System.out.println("[" + Rebirth.LOG_NAME + "] Reading Settings");
		CONFIG = new ConfigManager();
		//Set prefix
		PREFIX = Rebirth.CONFIG.getSettingString("prefix", PREFIX);
		System.out.println("[" + Rebirth.LOG_NAME + "] Initializing Modules");
		MODULE = new ModuleManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Initializing Commands");
		COMMAND = new CommandManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Initializing GUI");
		HUD = new HudManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading Alts");
		ALT = new AltManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading Friends");
		FRIEND = new FriendManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading RunManager");
		RUN = new RunManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading BreakManager");
		BREAK = new BreakManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading PopManager");
		POP = new PopManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading TimerManager");
		TIMER = new TimerManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading ShaderManager");
		SHADER = new ShaderManager();
		System.out.println("[" + Rebirth.LOG_NAME + "] Loading Settings");
		CONFIG.loadSettings();
		System.out.println("[" + Rebirth.LOG_NAME + "] Initialized and ready to play!");

		Runtime.getRuntime().addShutdownHook(new Thread(Rebirth::save));
		loaded = true;
	}

	public static void unload() {
		loaded = false;
		System.out.println("[" + Rebirth.LOG_NAME + "] Unloading..");
		EVENT_BUS.listenerMap.clear();
		CONFIG = null;
		MODULE = null;
		COMMAND = null;
		HUD = null;
		ALT = null;
		FRIEND = null;
		RUN = null;
		POP = null;
		TIMER = null;
		System.out.println("[" + Rebirth.LOG_NAME + "] Unloading success!");
	}
	public static void save() {
		System.out.println("[" + Rebirth.LOG_NAME + "] Saving...");
		CONFIG.saveSettings();
		FRIEND.saveFriends();
		ALT.saveAlts();
		System.out.println("[" + Rebirth.LOG_NAME + "] Saving success!");
	}

	public static String getName() {
		switch ((HudManager.HackName) HUD.hackName.getValue()) {
			case Rebirth -> {
				return "Rebirth";
			}
			case MoonGod -> {
				return "MoonGod";
			}
			case RebirthNew -> {
				return "ℜ\uD835\uDD22\uD835\uDD1F\uD835\uDD26\uD835\uDD2F\uD835\uDD31\uD835\uDD25";
			}
			case MoonEmoji -> {
				return "☽";
			}
			case StarEmoji -> {
				return "✷";
			}
			case Mio -> {
				return "Mio";
			}
		}
		return "Rebirth";
	}
}
