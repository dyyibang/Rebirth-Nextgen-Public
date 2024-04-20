package me.rebirthclient.api.managers;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.alts.Alt;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.asm.accessors.IMinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.Session.AccountType;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AltManager implements Wrapper {
	private final ArrayList<Alt> alts = new ArrayList<>();

	public AltManager() {
		readAlts();
	}

	public void readAlts() {
		try {
			File altFile = new File(mc.runDirectory, "rebirth_alts.txt");
			if (!altFile.exists())
				throw new IOException("File not found! Could not load alts...");
			List<String> list = IOUtils.readLines(new FileInputStream(altFile), StandardCharsets.UTF_8);

			for (String s : list) {
				alts.add(new Alt(s));
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void saveAlts() {
		PrintWriter printwriter = null;
		try {
			File altFile = new File(mc.runDirectory, "rebirth_alts.txt");
			System.out.println("[" + Rebirth.LOG_NAME + "] Saving Alts");
			printwriter = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(altFile), StandardCharsets.UTF_8));

			for (Alt alt : alts) {
				printwriter.println(alt.getEmail());
			}
		} catch (Exception exception) {
			System.out.println("[" + Rebirth.LOG_NAME + "] Failed to save alts");
		}
		printwriter.close();
	}


	public void addAlt(Alt alt) {
		alts.add(alt);
	}

	public void removeAlt(Alt alt) {
		alts.remove(alt);
	}

	public ArrayList<Alt> getAlts() {
		return this.alts;
	}

	public void loginCracked(String alt) {
		try {
			((IMinecraftClient) this.mc).setSession(new Session(alt, "", "", Optional.empty(), Optional.empty(), AccountType.MOJANG));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loginToken(String name, String token, String uuid) {
		try {
			((IMinecraftClient) this.mc).setSession(new Session(name, uuid, token, Optional.empty(), Optional.empty(), AccountType.MOJANG));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
