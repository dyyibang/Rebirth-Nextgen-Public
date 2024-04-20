/**
 * A class to represent a system to manage Commands.
 */
package me.rebirthclient.api.managers;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.interfaces.IChatHud;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.commands.impl.*;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CommandManager implements Wrapper {
	public static String syncCode = "\u00a7(";
	private final HashMap<String, Command> commands = new HashMap<>();
	public final AimCommand aim = new AimCommand();
	public final TeleportCommand tp = new TeleportCommand();
	public final BindCommand bind = new BindCommand();
	public final ToggleCommand toggle = new ToggleCommand();
	public final PrefixCommand prefix = new PrefixCommand();
	public final FriendCommand friend = new FriendCommand();
	public final ReloadCommand reload = new ReloadCommand();
	public final SaveCommand save = new SaveCommand();
	public CommandManager() {
		try
		{
			for(Field field : CommandManager.class.getDeclaredFields())
			{
				if (!Command.class.isAssignableFrom(field.getType())) 
					continue;
				Command cmd = (Command)field.get(this);
				commands.put(cmd.getName(), cmd);
			}
		}catch(Exception e)
		{
			System.out.println("Error initializing " + Rebirth.LOG_NAME + " commands.");
			System.out.println(e.getStackTrace().toString());
		}
	}

	public Command getCommandBySyntax(String string) {
		return this.commands.get(string);
	}

	public HashMap<String, Command> getCommands() {
		return this.commands;
	}

	public int getNumOfCommands() {
		return this.commands.size();
	}

	public void command(String[] commandIn) {

		// Get the command from the user's message. (Index 0 is Username)
		Command command = commands.get(commandIn[0].substring(Rebirth.PREFIX.length()));

		// If the command does not exist, throw an error.
		if (command == null)
			sendChatMessage("\u00a7c[!] \u00a7fInvalid Command! Type \u00a7e" + "help \u00a7ffor a list of commands.");
		else {
			// Otherwise, create a new parameter list.
			String[] parameterList = new String[commandIn.length - 1];
			for (int i = 1; i < commandIn.length; i++) {
				parameterList[i - 1] = commandIn[i];
			}
			if (parameterList.length == 1 && parameterList[0].equals("help")) {
				command.sendUsage();
				return;
			}
			// Runs the command.
			command.runCommand(parameterList);
		}
	}

	public static void sendChatMessage(String message) {
		if (Module.nullCheck() || !Rebirth.loaded) return;
		mc.inGameHud.getChatHud().addMessage(Text.of(syncCode + "\u00a7r[" + Rebirth.getName() + "]§f " + message));
	}

	public static void sendChatMessageWidthId(String message, int id) {
		if (Module.nullCheck() || !Rebirth.loaded) return;
		((IChatHud) mc.inGameHud.getChatHud()).rebirth_nextgen_master$add(Text.of(syncCode + "\u00a7r[" + Rebirth.getName() + "]§f " + message), id);
	}
}
