package hu.montlikadani.TeleportSigns;

import static hu.montlikadani.TeleportSigns.utils.Util.logConsole;
import static hu.montlikadani.TeleportSigns.utils.Util.throwMsg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.TeleportSigns.server.ServerInfo;
import hu.montlikadani.TeleportSigns.sign.SignLayout;
import hu.montlikadani.TeleportSigns.sign.TeleportSign;
import hu.montlikadani.TeleportSigns.utils.LocationSerialiser;
import hu.montlikadani.TeleportSigns.utils.SignUtil;

public class ConfigData {

	private TeleportSigns plugin;

	private File config_file, messages_file, layout_file, sign_file;
	private FileConfiguration config, messages, layout, sign;

	private Set<ServerInfo> servers = new HashSet<>();
	private Set<TeleportSign> signs = new HashSet<>();
	private Set<Block> blocks = new HashSet<>();

	private Map<String, SignLayout> layouts = new HashMap<>();

	private long cooldown, signUpdates;
	private int pingTimeout, pingInterval;
	private boolean externalServer, logConsole, ignorePlayerSneaking;
	private String bgType;

	private int cver = 5;
	private int lyver = 2;

	public enum ConfigType {
		CONFIG, MESSAGES, LAYOUTS, SIGNS;
	}

	public ConfigData(TeleportSigns plugin) {
		this.plugin = plugin;

		config_file = new File(getFolder(), "config.yml");
		messages_file = new File(getFolder(), "messages.yml");
		layout_file = new File(getFolder(), "layout.yml");
		sign_file = new File(getFolder(), "signs.yml");
	}

	void loadConfig() {
		unloadConfig();

		try {
			if (config_file.exists()) {
				config = YamlConfiguration.loadConfiguration(config_file);
				config.load(config_file);

				if (!config.isSet("config-version") || !config.get("config-version").equals(cver)) {
					logConsole(Level.WARNING, "Found outdated configuration (config.yml)! (Your version: "
							+ config.getInt("config-version") + " | Newest version: " + cver + ")");
				}
			} else {
				config = createFile("config.yml", config_file, false);
			}

			if (messages_file.exists()) {
				messages = YamlConfiguration.loadConfiguration(messages_file);
				messages.load(messages_file);
				messages.save(messages_file);
			} else {
				messages = createFile("messages.yml", messages_file, false);
			}

			if (layout_file.exists()) {
				layout = YamlConfiguration.loadConfiguration(layout_file);
				layout.load(layout_file);

				if (!layout.isSet("config-version") || !layout.get("config-version").equals(lyver)) {
					logConsole(Level.WARNING, "Found outdated configuration (layout.yml)! (Your version: "
							+ layout.getInt("config-version") + " | Newest version: " + lyver + ")");
				}
			} else {
				layout = createFile("layout.yml", layout_file, false);
			}

			if (sign_file.exists()) {
				sign = YamlConfiguration.loadConfiguration(sign_file);
				sign.load(sign_file);
				sign.save(sign_file);
			} else {
				sign = createFile("signs.yml", sign_file, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throwMsg();
		}

		loadSettings();
		loadServers();
		loadLayouts();
		loadSigns();
	}

	FileConfiguration createFile(String name, File file, boolean newFile) {
		if (newFile) {
			try {
				sign_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			plugin.saveResource(name, false);
		}

		logConsole("The '" + name + "' file successfully created!", false);
		return YamlConfiguration.loadConfiguration(file);
	}

	public File getFolder() {
		File dataFolder = plugin.getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		return dataFolder;
	}

	void unloadConfig() {
		config = null;
		messages = null;
		layout = null;
		sign = null;

		servers.clear();
		signs.clear();
		layouts.clear();
	}

	private void loadSettings() {
		this.externalServer = config.getBoolean("options.external-server", true);
		this.logConsole = config.getBoolean("options.logConsole", true);
		this.cooldown = (config.getInt("options.use-cooldown", 3) * 1000);
		this.signUpdates = config.getInt("options.sign-updates", 200);
		this.pingInterval = config.getInt("options.ping-interval", 30);
		this.pingTimeout = config.getInt("options.ping-timeout", 29);
		this.ignorePlayerSneaking = config.getBoolean("options.ignore-player-sneaking", true);
		this.bgType = config.getString("options.background-type", "none");
	}

	private void loadServers() {
		if (!config.contains("servers")) {
			return;
		}

		ConfigurationSection srv = config.getConfigurationSection("servers");
		for (String server : srv.getKeys(false)) {
			ConfigurationSection cs = srv.getConfigurationSection(server);

			String displayname = cs.getString("displayname");
			String[] address = cs.getString("address").split(":");
			String ip = address[0];
			String port = address[1];

			ServerInfo serverping = new ServerInfo(server, displayname, ip, Integer.valueOf(port), pingTimeout);
			servers.add(serverping);
		}
	}

	private void loadLayouts() {
		if (!layout.contains("layouts")) {
			return;
		}

		ConfigurationSection layouts = layout.getConfigurationSection("layouts");
		for (String layout : layouts.getKeys(false)) {
			ConfigurationSection cs = layouts.getConfigurationSection(layout);

			String online = cs.getString("online", "Online");
			String offline = cs.getString("offline", "Offline");
			String full = cs.getString("full", "Full");
			List<String> lines = cs.getStringList("layout");
			boolean teleport = cs.getBoolean("teleport", true);
			String offlineInt = cs.getString("offline-int", "--");
			String offlineMotd = cs.getString("offline-motd", "&cOffline");
			String offlineMessage = cs.getString("offline-message", "&cThe server is offline!");
			String fullMessage = cs.getString("full-message", "&cThe server is full!");
			String cooldownMessage = cs.getString("cooldown-message",
					"&cYou have to wait&7 %cooldown%&c seconds before you can use this sign again.");
			String cantTeleportMessage = cs.getString("cant-teleport", "&cYou can't teleport to the server!");
			SignLayout signLayout = new SignLayout(layout, online, offline, lines, teleport, offlineInt, offlineMotd,
					offlineMessage, fullMessage, cooldownMessage, full, cantTeleportMessage);
			this.layouts.put(layout, signLayout);
		}
	}

	private void loadSigns() {
		List<String> list = sign.getStringList("signs");
		for (String sign : list) {
			Location location = LocationSerialiser.stringToLocationSign(sign);
			if (location == null) {
				logConsole(Level.WARNING, "The location for the sign is null.");
				logConsole("Probably world not exists or the sign was broken.");
				continue;
			}

			ServerInfo server = getServer(LocationSerialiser.getServerFromSign(sign));
			SignLayout layout = getLayout(LocationSerialiser.getLayoutFromSign(sign));

			Block b = location.getBlock();
			if (SignUtil.isSign(b.getState())) {
				TeleportSign tsign = new TeleportSign(server, location, layout);
				this.signs.add(tsign);
				this.blocks.add(b);
			}
		}
	}

	public SignLayout getLayout(String layout) {
		return layouts.get(layout);
	}

	public ServerInfo getServer(String server) {
		for (ServerInfo info : servers) {
			if (info.getName().equals(server)) {
				return info;
			}
		}

		return null;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(int seconds) {
		cooldown = seconds * 1000;
	}

	public FileConfiguration getConfig(ConfigType type) {
		switch (type) {
		case CONFIG:
			return config;
		case MESSAGES:
			return messages;
		case LAYOUTS:
			return layout;
		case SIGNS:
			return sign;
		default:
			return null;
		}
	}

	public boolean isExternal() {
		return externalServer;
	}

	public boolean isLogConsole() {
		return logConsole;
	}

	public boolean isIgnoringSneak() {
		return ignorePlayerSneaking;
	}

	public String getBackgroundType() {
		return bgType;
	}

	public Set<ServerInfo> getServers() {
		return servers;
	}

	public void setServers(Set<ServerInfo> servers) {
		this.servers = servers;
	}

	public Set<TeleportSign> getSigns() {
		return signs;
	}

	public void setSigns(Set<TeleportSign> signs) {
		this.signs = signs;
	}

	public Map<String, SignLayout> getLayouts() {
		return layouts;
	}

	public void setLayouts(Map<String, SignLayout> layouts) {
		this.layouts = layouts;
	}

	public Set<Block> getBlocks() {
		return blocks;
	}

	public void setBlocks(Set<Block> blocks) {
		this.blocks = blocks;
	}

	public long getUpdateInterval() {
		return signUpdates;
	}

	public void setUpdateInterval(long signUpdates) {
		this.signUpdates = signUpdates;
	}

	public int getPingInterval() {
		return pingInterval;
	}

	public int getPingTimeout() {
		return pingTimeout;
	}

	public void setPingInterval(int pingInterval) {
		this.pingInterval = pingInterval;
	}

	public void setPingTimeout(int pingTimeout) {
		this.pingTimeout = pingTimeout;
	}

	public TeleportSign getSignFromLocation(Location l) {
		for (TeleportSign sign : signs) {
			if (sign.getLocation().equals(l)) {
				return sign;
			}
		}

		return null;
	}

	public boolean containsSign(Block b) {
		if (blocks.contains(b)) {
			return true;
		}

		return false;
	}

	public void addSign(Location location, ServerInfo server, SignLayout layout) {
		String index = LocationSerialiser.locationSignToString(location, server.getName(), layout.getName());
		List<String> list = sign.getStringList("signs");

		list.add(index);
		sign.set("signs", list);

		try {
			sign.save(sign_file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		blocks.add(location.getBlock());
		TeleportSign tsign = new TeleportSign(server, location, layout);
		signs.add(tsign);
	}

	public void removeSign(Location location) {
		List<String> list = this.sign.getStringList("signs");

		for (TeleportSign sign : signs) {
			if (sign.getLocation().equals(location)) {
				String index = LocationSerialiser.locationSignToString(location, sign.getServer().getName(),
						sign.getLayout().getName());

				if (!list.isEmpty()) {
					list.remove(index);
					this.sign.set("signs", list);
				}

				try {
					this.sign.save(sign_file);
				} catch (IOException e) {
					e.printStackTrace();
				}

				blocks.remove(location.getBlock());
				signs.remove(sign);
				break;
			}
		}
	}
}
