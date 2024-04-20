package me.rebirthclient.mod.commands.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.mod.commands.Command;

import java.util.List;

public class SaveCommand extends Command {

	public SaveCommand() {
		super("save", "save", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("\u00a7e[!] \u00a7fSaving..");
		Rebirth.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
