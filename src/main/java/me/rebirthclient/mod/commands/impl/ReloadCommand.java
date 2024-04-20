package me.rebirthclient.mod.commands.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.mod.commands.Command;

import java.util.List;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", "debug", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("\u00a7e[!] \u00a7fReloading..");
		Rebirth.unload();
		Rebirth.load();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
