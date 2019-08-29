package hu.montlikadani.TeleportSigns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import static hu.montlikadani.TeleportSigns.Messager.logConsole;
import static hu.montlikadani.TeleportSigns.Messager.throwMsg;

public class ConfigData {

	private TeleportSigns plugin;

	private File config_file, messages_file, layout_file, sign_file;
	private FileConfiguration config, messages, layout, sign;

	private List<ServerInfo> servers = new ArrayList<>();
	private List<TeleportSign> signs = new ArrayList<>();
	private List<Block> blocks = new ArrayList<>();
	private Map<String, SignLayout> layouts = new HashMap<>();

	private long cooldown, signUpdates;
	private int pingTimeout, pingInterval;
	private boolean externalServer, logConsole, ignorePlayerSneaking, background;
	private String bgType;

	private int cver = 5;
	private int msver = 5;
	private int lyver = 2;

	public enum ConfigType {
		CONFIG,
		MESSAGES,
		LAYOUTS,
		SIGNS;
	}

	public ConfigData(TeleportSigns plugin) {
		this.plugin = plugin;

		config_file = new File(getFolder(), "config.yml");
		messages_file = new File(getFolder(), "messages.yml");
		layout_file = new File(getFolder(), "layout.yml");
		sign_file = new File(getFolder(), "signs.yml");
	}

	public void loadConfig() {
		unloadConfig();

		try {
			if (config_file.exists()) {
				if (config == null) {
					config = YamlConfiguration.loadConfiguration(config_file);
				}
				config.load(config_file);

				if (!config.isSet("config-version") || !config.get("config-version").equals(cver)) {
					logConsole(Level.WARNING, "Found outdated configuration (config.yml)! (Your version: "
							+ config.getInt("config-version") + " | Newest version: " + cver + ")");
				}
			} else {
				createFile("config.yml", config_file, false);
			}

			if (messages_file.exists()) {
				if (messages == null) {
					messages = YamlConfiguration.loadConfiguration(messages_file);
				}
				messages.load(messages_file);

				if (!messages.isSet("config-version") || !messages.get("config-version").equals(msver)) {
					logConsole(Level.WARNING, "Found outdated configuration (messages.yml)! (Your version: " +
							messages.getInt("config-version") + " | Newest version: " + msver + ")");
				}
			} else {
				createFile("messages.yml", messages_file, false);
			}

			if (layout_file.exists()) {
				if (layout == null) {
					layout = YamlConfiguration.loadConfiguration(layout_file);
				}
				layout.load(layout_file);

				if (!layout.isSet("config-version") || !layout.get("config-version").equals(lyver)) {
					logConsole(Level.WARNING, "Found outdated configuration (layout.yml)! (Your version: "
							+ layout.getInt("config-version") + " | Newest version: " + lyver + ")");
				}
			} else {
				createFile("layout.yml", layout_file, false);
			}

			if (sign_file.exists()) {
				if (sign == null) {
					sign = YamlConfiguration.loadConfiguration(sign_file);
				}
				sign.load(sign_file);
				sign.save(sign_file);
			} else {
				createFile("signs.yml", sign_file, true);
			}
		} catch (Throwable e) {
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
				throwMsg();
			}
		} else {
			plugin.saveResource(name, false);
		}

		logConsole("The '" + name + "' file successfully created!");
		return YamlConfiguration.loadConfiguration(file);
	}

	public File getFolder() {
		File dataFolder = plugin.getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		return dataFolder;
	}

	public void unloadConfig() {
		config = null;
		messages = null;
		layout = null;
		sign = null;

		servers.clear();
		signs.clear();
		layouts.clear();
	}

	private void loadSettings() {
		this.externalServer = this.config.getBoolean("options.external-server");
		this.logConsole = this.config.getBoolean("options.logConsole");
		this.cooldown = (this.config.getInt("options.use-cooldown") * 1000);
		this.signUpdates = this.config.getInt("options.sign-updates");
		this.pingInterval = this.config.getInt("options.ping-interval");
		this.pingTimeout = this.config.getInt("options.ping-timeout");
		this.ignorePlayerSneaking = this.config.getBoolean("options.ignore-player-sneaking");

		this.background = this.config.getBoolean("options.background.enable");
		this.bgType = this.config.getString("options.background.type");
	}

	private void loadServers() {
		ConfigurationSection srv = this.config.getConfigurationSection("servers");

		for (String server : srv.getKeys(false)) {
			ConfigurationSection cs = srv.getConfigurationSection(server);
			String displayname = cs.getString("displayname");
			String[] address = cs.getString("address").split(":");
			String ip = address[0];
			String port = address[1];

			ServerInfo serverping = new ServerInfo(server, displayname, ip, Integer.valueOf(port), this.pingTimeout);
			serverping.resetPingDelay();
			this.servers.add(serverping);
		}
	}

	private void loadLayouts() {
		ConfigurationSection layouts = this.layout.getConfigurationSection("layouts");

		for (String layout : layouts.getKeys(false)) {
			ConfigurationSection cs = layouts.getConfigurationSection(layout);
			String online = cs.getString("online");
			String offline = cs.getString("offline");
			List<String> lines = cs.getStringList("layout");
			boolean teleport = cs.getBoolean("teleport");
			String offlineInt = cs.getString("offline-int");
			String offlineMotd = cs.getString("offline-motd");
			String offlineMessage = cs.getString("offline-message");
			String fullMessage = cs.getString("full-message");
			String cooldownMessage = cs.getString("cooldown-message");
			String full = cs.getString("full");
			SignLayout signLayout = new SignLayout(layout, online, offline, lines, teleport, offlineInt,
					offlineMotd, offlineMessage, fullMessage, cooldownMessage, full);
			this.layouts.put(layout, signLayout);
		}
	}

	private void loadSigns() {
		List<String> list = this.sign.getStringList("signs");
		if (list == null || list.isEmpty()) {
			logConsole(Level.WARNING, "No saved sign was found.");
		} else {
			for (String sign : list) {
				try {
					Location location = LocationSerialiser.stringToLocationSign(sign);
					ServerInfo server = getServer(LocationSerialiser.getServerFromSign(sign));
					SignLayout layout = getLayout(LocationSerialiser.getLayoutFromSign(sign));

					if (location == null) {
						logConsole(Level.WARNING, "The location for the sign is null.");
						logConsole("Probably world not exists or the sign was broken.");
						return;
					}

					Block b = location.getBlock();
					if (b.getState() instanceof Sign) {
						TeleportSign tsign = new TeleportSign(server, location, layout);
						this.signs.add(tsign);
						this.blocks.add(b);
					}
				} catch (Throwable e) {
					e.printStackTrace();
					throwMsg();
				}
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

	public boolean isBackgroundEnabled() {
		return background;
	}

	public String getBackgroundType() {
		return bgType;
	}

	public List<ServerInfo> getServers() {
		return servers;
	}

	public void setServers(List<ServerInfo> servers) {
		this.servers = servers;
	}

	public List<TeleportSign> getSigns() {
		return signs;
	}

	public void setSigns(List<TeleportSign> signs) {
		this.signs = signs;
	}

	public Map<String, SignLayout> getLayouts() {
		return layouts;
	}

	public void setLayouts(Map<String, SignLayout> layouts) {
		this.layouts = layouts;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<Block> blocks) {
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
			if (l.equals(sign.getLocation())) {
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
			throwMsg();
		}

		blocks.add(location.getBlock());
		TeleportSign tsign = new TeleportSign(server, location, layout);
		signs.add(tsign);
	}

	public void removeSign(Location location) {
		for (TeleportSign sign : signs) {
			if (location.equals(sign.getLocation())) {
				String index = LocationSerialiser.locationSignToString(location, sign.getServer().getName(),
						sign.getLayout().getName());
				List<String> list = this.sign.getStringList("signs");

				if (list != null && !list.isEmpty()) {
					list.remove(index);
					this.sign.set("signs", list);
				} else {
					this.sign.set("signs", null);
				}

				try {
					this.sign.save(sign_file);
				} catch (IOException e) {
					e.printStackTrace();
					throwMsg();
				}

				blocks.remove(location.getBlock());
				signs.remove(sign);
				break;
			}
		}
	}
}
