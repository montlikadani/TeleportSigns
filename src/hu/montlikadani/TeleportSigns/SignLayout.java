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
	private String cooldownMessage;

	public SignLayout(String name, String online, String offline, List<String> lines, boolean teleport, String offlineInt, String offlineMotd, String offlineMessage, String cooldownMessage) {
		this.name = name;
		this.online = online;
		this.offline = offline;
		this.lines = lines;
		this.teleport = teleport;
		this.offlineInt = offlineInt;
		this.offlineMotd = offlineMotd;
		this.offlineMessage = offlineMessage;
		this.cooldownMessage = cooldownMessage;
	}

	public String getName() {
		return this.name;
	}

	public String getOnline() {
		return this.online;
	}

	public String getOffline() {
		return this.offline;
	}

	public List<String> getLines() {
		return this.lines;
	}

	public boolean isTeleport() {
		return this.teleport;
	}

	public String getOfflineInt() {
		return this.offlineInt;
	}

	public String getOfflineMotd() {
		return this.offlineMotd;
	}

	public String getOfflineMessage() {
		return this.offlineMessage;
	}

	public String getCooldownMessage() {
		return this.cooldownMessage;
	}

	public List<String> parseLayout(ServerInfo server) {
		List<String> layout = new ArrayList<>();

		for (String line : this.lines) {
			line = line.replaceAll("%name%", server.getName());
			line = line.replaceAll("%displayname%", server.getDisplayname());

			if (server.isOnline()) {
				line = line.replaceAll("%isonline%", getOnline());
				line = line.replaceAll("%numpl%", String.valueOf(server.getPlayerCount()));
				line = line.replaceAll("%maxpl%", String.valueOf(server.getMaxPlayers()));
				line = line.replaceAll("%motd%", formatDescription(server.getMotd()));
				line = line.replaceAll("%address%", server.getAddress().getHostName());
				line = line.replaceAll("%port%", String.valueOf(server.getAddress().getPort()));
				line = line.replaceAll("%ping%", String.valueOf(server.getPingDelay()));
				line = line.replaceAll("%version%", server.getVersion());

				for (CustomVariable var : TeleportSigns.getInstance().getConfigData().getVariables()) {
					if (line.contains(var.getVariable())) {
						line = var.parseVariable(server.getMotd());
					}
				}

				line = textValues(line);
				line = editText(line);
			} else {
				line = line.replaceAll("%isonline%", getOffline());
				line = line.replaceAll("%numpl%", String.valueOf(getOfflineInt()));
				line = line.replaceAll("%maxpl%", String.valueOf(getOfflineInt()));
				line = line.replaceAll("%motd%", getOfflineMotd());
				line = line.replaceAll("%address%", server.getAddress().getHostName());
				line = line.replaceAll("%port%", String.valueOf(server.getAddress().getPort()));
				line = line.replaceAll("%ping%", String.valueOf(server.getPingDelay()));
				line = line.replaceAll("%version%", "");

				for (CustomVariable var : TeleportSigns.getInstance().getConfigData().getVariables()) {
					if (line.contains(var.getVariable())) {
						line = line.replaceAll(var.getVariable(), "");
					}
				}

				line = textValues(line);
				line = editText(line);
			}

			layout.add(line);
		}

		return layout;
	}

	public String parseOfflineMessage(ServerInfo server) {
		String line = this.offlineMessage;
		line = line.replaceAll("%name%", server.getName());
		line = line.replaceAll("%address%", server.getAddress().getHostName());
		line = line.replaceAll("%port%", String.valueOf(server.getAddress().getPort()));
		line = textValues(line);

		return line;
	}

	public String parseCooldownMessage(int seconds) {
		String line = this.cooldownMessage;
		line = line.replaceAll("%cooldown%", String.valueOf(seconds));
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
		line = line.replaceAll("%s", "ß");

		return line;
	}

	private String editText(String text) {
		int length = text.length();

		if (length > 16) {
			text = text.substring(0, 16);
		}

		return text;
	}
}
