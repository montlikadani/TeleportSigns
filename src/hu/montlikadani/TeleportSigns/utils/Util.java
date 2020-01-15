package hu.montlikadani.TeleportSigns.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import hu.montlikadani.TeleportSigns.TeleportSigns;

public class Util {

	public static void logConsole(String error) {
		logConsole(Level.INFO, error, true);
	}

	public static void logConsole(String error, boolean loaded) {
		logConsole(Level.INFO, error, loaded);
	}

	public static void logConsole(Level level, String error) {
		logConsole(level, error, true);
	}

	public static void logConsole(Level level, String error, boolean loaded) {
		if (!loaded) {
			Bukkit.getLogger().log(level, "[TeleportSigns] " + error);
			return;
		}

		if (TeleportSigns.getInstance().getMainConf().getBoolean("options.logconsole")) {
			Bukkit.getLogger().log(level, "[TeleportSigns] " + error);
		}

		if (TeleportSigns.getInstance().getMainConf().getBoolean("log-to-file")) {
			try {
				File saveTo = new File(TeleportSigns.getInstance().getConfigData().getFolder(), "log.txt");
				if (!saveTo.exists()) {
					saveTo.createNewFile();
				}

				FileWriter fw = new FileWriter(saveTo, true);
				PrintWriter pw = new PrintWriter(fw);
				Date dt = new Date();
				SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
				String time = df.format(dt);
				pw.println(time + " - [" + level + "] " + error);
				pw.flush();
				pw.close();
			} catch (Throwable e) {
				e.printStackTrace();
				throwMsg();
			}
		}
	}

	public static void sendMsg(org.bukkit.command.CommandSender sender, String s) {
		if (s != null && !s.isEmpty()) {
			if (s.contains("\n")) {
				for (String msg : s.split("\n")) {
					sender.sendMessage(msg);
				}
			} else {
				sender.sendMessage(s);
			}
		}
	}

	public static void throwMsg() {
		logConsole(Level.WARNING, "There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
	}

	public static String colorMsg(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
}
