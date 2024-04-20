package me.rebirthclient.mod.modules.miscellaneous;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.SendMessageEvent;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.EnumSetting;

public class ChatAppend extends Module {
	public static ChatAppend INSTANCE;
	private final EnumSetting mode = add(new EnumSetting("Mode", Mode.Rebirth));

	public ChatAppend() {
		super("ChatAppend", Category.Miscellaneous);
		INSTANCE = this;
	}

	private enum Mode {
		Rebirth,
		Melon,
		Mio
	}

	public static String rebirthSuffix = "ℜ\uD835\uDD22\uD835\uDD1F\uD835\uDD26\uD835\uDD2F\uD835\uDD31\uD835\uDD25";
	public static String melonSuffix = "\uD835\uDD10\uD835\uDD22\uD835\uDD29\uD835\uDD2C\uD835\uDD2B\uD835\uDD05\uD835\uDD22\uD835\uDD31\uD835\uDD1E";
	public static String MioSuffix = "\uD835\uDDE0\uD835\uDDF6\uD835\uDDFC";
	@EventHandler
	public void onSendMessage(SendMessageEvent event) {
		if (nullCheck() || event.isCancel()) return;
		String message = event.message;

		if (message.startsWith("/") || message.startsWith("!") || message.endsWith(rebirthSuffix) || message.endsWith(melonSuffix)) {
			return;
		}
		String suffix = "";
		switch ((Mode) mode.getValue()) {
			case Rebirth -> suffix = ChatAppend.rebirthSuffix;
			case Melon -> suffix = melonSuffix;
			case Mio -> suffix = MioSuffix;
		}
		message = message + " " + suffix;
		event.message = message;
	}
}