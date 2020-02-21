package hu.montlikadani.TeleportSigns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class SignLayout {

	private String name;
	private String online;
	private String offline;
	private List<String> lines;
	private boolean teleport;
	private String offlineInt;
	private String offlineMotd;
	private String offlineMessage;
	private String fullMessage;
	private String cooldownMessage;
	private String full;
	private String cantTeleportMessage;

	public SignLayout(String name, String online, String offline, List<String> lines, boolean teleport,
			String offlineInt, String offlineMotd, String offlineMessage, String fullMessage, String cooldownMessage,
			String full, String cantTeleportMessage) {
		this.name = name;
		this.online = online;
		this.offline = offline;
		this.lines = lines;
		this.teleport = teleport;
		this.offlineInt = offlineInt;
		this.offlineMotd = offlineMotd;
		this.offlineMessage = offlineMessage;
		this.fullMessage = fullMessage;
		this.cooldownMessage = cooldownMessage;
		this.full = full;
		this.cantTeleportMessage = cantTeleportMessage;
	}

	public String getName() {
		return name;
	}

	public String getOnline() {
		return online;
	}

	public String getFull() {
		return full;
	}

	public String getOffline() {
		return offline;
	}

	public List<String> getLines() {
		return lines;
	}

	public boolean isTeleport() {
		return teleport;
	}

	public String getOfflineInt() {
		return offlineInt;
	}

	public String getOfflineMotd() {
		return offlineMotd;
	}

	public String getOfflineMessage() {
		return offlineMessage;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public String getCooldownMessage() {
		return cooldownMessage;
	}

	public String getcantTeleportMessageMessage() {
		return cantTeleportMessage;
	}

	public List<String> parseLayout(ServerInfo server) {
		List<String> layout = new ArrayList<>();

		for (String line : lines) {
			line = line.replace("%name%", server.getName());
			line = line.replace("%displayname%", server.getDisplayname());
			line = line.replace("%address%", server.getAddress().getHostName());
			line = line.replace("%port%", Integer.toString(server.getAddress().getPort()));

			if (server.isOnline()) {
				line = line.replace("%ping%", String.valueOf(server.getPingDelay()));
				line = line.replace("%numpl%", Integer.toString(server.getPlayerCount()));
				line = line.replace("%maxpl%", Integer.toString(server.getMaxPlayers()));
				line = line.replace("%motd%", formatDescription(server.getMotd()));
				line = line.replace("%version%", server.getVersion());

				line = line.replace("%isonline%", server.getPlayerCount() == server.getMaxPlayers() ? full
							: online);
			} else {
				line = line.replace("%ping%", "0ms");
				line = line.replace("%isonline%", offline);
				line = line.replace("%numpl%", offlineInt);
				line = line.replace("%maxpl%", offlineInt);
				line = line.replace("%motd%", offlineMotd);
				line = line.replace("%version%", "");
			}

			line = textValues(line);
			layout.add(line);
		}

		return layout;
	}

	public String parseOfflineMessage(ServerInfo server) {
		if (offlineMessage == null || offlineMessage.isEmpty()) {
			return "";
		}

		String line = offlineMessage;
		line = line.replace("%name%", server.getName());
		line = line.replace("%displayname%", server.getDisplayname());
		line = line.replace("%address%", server.getAddress().getHostName());
		line = line.replace("%port%", String.valueOf(server.getAddress().getPort()));
		line = textValues(line);

		return line;
	}

	public String parseFullMessage(ServerInfo server) {
		if (fullMessage == null || fullMessage.isEmpty()) {
			return "";
		}

		String line = fullMessage;
		line = line.replace("%name%", server.getName());
		line = line.replace("%displayname%", server.getDisplayname());
		line = line.replace("%address%", server.getAddress().getHostName());
		line = line.replace("%port%", String.valueOf(server.getAddress().getPort()));
		line = line.replace("%numpl%", Integer.toString(server.getPlayerCount()));
		line = line.replace("%maxpl%", Integer.toString(server.getMaxPlayers()));
		line = textValues(line);

		return line;
	}

	public String parseCooldownMessage(int seconds) {
		if (cooldownMessage == null || cooldownMessage.isEmpty()) {
			return "";
		}

		String line = cooldownMessage;
		line = line.replace("%cooldown%", Integer.toString(seconds));
		line = textValues(line);

		return line;
	}

	public String parseCantTeleportMessage(ServerInfo server) {
		if (cantTeleportMessage == null || cantTeleportMessage.isEmpty()) {
			return "";
		}

		String line = cantTeleportMessage;
		line = line.replace("%name%", server.getName());
		line = textValues(line);

		return line;
	}

	public String formatDescription(String description) {
		String line = "";
		int motdCount = 0;
		String tempMotd = description == null ? "" : description;
		String[] splitMotd = tempMotd.split("(?<=\\G.{15})");
		if (motdCount < splitMotd.length) {
			String motd = splitMotd[motdCount];
			if (motd != null) {
				line = motd;
			}
			motdCount++;
		} else {
			line = "";
		}

		return line;
	}

	private String textValues(String line) {
		line = line.replaceAll("&0", ChatColor.BLACK.toString());
		line = line.replaceAll("&1", ChatColor.DARK_BLUE.toString());
		line = line.replaceAll("&2", ChatColor.DARK_GREEN.toString());
		line = line.replaceAll("&3", ChatColor.DARK_AQUA.toString());
		line = line.replaceAll("&4", ChatColor.DARK_RED.toString());
		line = line.replaceAll("&5", ChatColor.DARK_PURPLE.toString());
		line = line.replaceAll("&6", ChatColor.GOLD.toString());
		line = line.replaceAll("&7", ChatColor.GRAY.toString());
		line = line.replaceAll("&8", ChatColor.DARK_GRAY.toString());
		line = line.replaceAll("&9", ChatColor.BLUE.toString());
		line = line.replaceAll("&a", ChatColor.GREEN.toString());
		line = line.replaceAll("&b", ChatColor.AQUA.toString());
		line = line.replaceAll("&c", ChatColor.RED.toString());
		line = line.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString());
		line = line.replaceAll("&e", ChatColor.YELLOW.toString());
		line = line.replaceAll("&f", ChatColor.WHITE.toString());
		line = line.replaceAll("&m", ChatColor.STRIKETHROUGH.toString());
		line = line.replaceAll("&n", ChatColor.UNDERLINE.toString());
		line = line.replaceAll("&l", ChatColor.BOLD.toString());
		line = line.replaceAll("&k", ChatColor.MAGIC.toString());
		line = line.replaceAll("&o", ChatColor.ITALIC.toString());
		line = line.replaceAll("&r", ChatColor.RESET.toString());
		line = line.replaceAll("&&", "&");
		line = line.replaceAll("%s", "\u00df");

		return line;
	}
}
