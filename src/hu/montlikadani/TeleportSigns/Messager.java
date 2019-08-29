package hu.montlikadani.TeleportSigns;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Messager {

	static void logConsole(String error) {
		logConsole(Level.INFO, error);
	}

	static void logConsole(Level level, String error) {
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

	static void sendMsg(org.bukkit.command.CommandSender sender, String s) {
		if (s != null && !s.equals(""))
			sender.sendMessage(s);
	}

	static String defaults(String str) {
		if (str.contains("%newline%")) {
			str = str.replace("%newline%", "\n");
		}

		return colorMsg(str);
	}

	static void throwMsg() {
		logConsole(Level.WARNING, "There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		return;
	}

	public static String colorMsg(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static String replaceColor(String s) {
		return s.replace("&", "\u00a7");
	}
}
