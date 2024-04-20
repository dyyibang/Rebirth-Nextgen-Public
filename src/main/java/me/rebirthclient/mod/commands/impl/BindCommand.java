package me.rebirthclient.mod.commands.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.api.managers.ModuleManager;

import java.util.ArrayList;
import java.util.List;

public class BindCommand extends Command {

	public BindCommand() {
		super("bind", "Bind key", "[module] [key]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		String moduleName = parameters[0];
		Module module = Rebirth.MODULE.getModuleByName(moduleName);
		if (module == null) {
			CommandManager.sendChatMessage("\u00a74[!] \u00a7fUnknown \u00a7bmodule!");
			return;
		}
		if (parameters.length == 1) {
			CommandManager.sendChatMessage("\u00a76[!] \u00a7fPlease specify a \u00a7bkey.");
			return;
		}
		String rkey = parameters[1];
		if (rkey == null) {
			CommandManager.sendChatMessage("\u00a74Unknown Error");
			return;
		}
		if (module.setBind(rkey.toUpperCase())) {
			CommandManager.sendChatMessage("\u00a7a[√] \u00a7fBind for \u00a7a" + module.getName() + "\u00a7f set to \u00a77" + rkey.toUpperCase());
		}
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			ModuleManager cm = Rebirth.MODULE;
			List<String> correct = new ArrayList<>();
			for (Module x : cm.modules) {
				if (input.equalsIgnoreCase(Rebirth.PREFIX + "bind") || x.getName().toLowerCase().startsWith(input)) {
					correct.add(x.getName());
				}
			}
			int numCmds = correct.size();
			String[] commands = new String[numCmds];

			int i = 0;
			for (String x : correct) {
				commands[i++] = x;
			}

			return commands;
		}
		return null;
	}
}
