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

public class ConfigData {

	private TeleportSigns plugin;

	private File config_file, layout_file, sign_file;
	private FileConfiguration config, layout, sign;
	private List<ServerInfo> servers = new ArrayList<>();
	private List<TeleportSign> signs = new ArrayList<>();
	private List<Block> blocks = new ArrayList<>();
	private Map<String, SignLayout> layouts = new HashMap<>();
	private long cooldown, signUpdates;
	private int pingTimeout, pingInterval;
	private boolean externalServer;
	private int cver = 5;
	private int lyver = 2;

	public enum ConfigType {
		CONFIG,
		LAYOUTS,
		SIGNS;
	}

	public ConfigData(TeleportSigns plugin) {
		this.plugin = plugin;

		config_file = new File(getFolder(), "config.yml");
		layout_file = new File(getFolder(), "layout.yml");
		sign_file = new File(getFolder(), "signs.yml");
	}

	public void loadConfig() {
		unloadConfig();

		String resultMsg = "";
		try {
			if (config_file.exists()) {
				config = YamlConfiguration.loadConfiguration(config_file);
				config.load(config_file);

				if (!config.isSet("config-version") || !config.get("config-version").equals(cver)) {
					plugin.logConsole(Level.WARNING, "Found outdated configuration (config.yml)! (Your version: "
							+ config.getInt("config-version") + " | Newest version: " + cver + ")");
				}
			} else {
				plugin.saveResource("config.yml", false);
				config = YamlConfiguration.loadConfiguration(config_file);
				resultMsg += "The 'config.yml' file successfully created!";
			}

			plugin.createMsgFile();

			if (layout_file.exists()) {
				layout = YamlConfiguration.loadConfiguration(layout_file);
				layout.load(layout_file);
				if (!layout.isSet("config-version") || !layout.get("config-version").equals(lyver)) {
					plugin.logConsole(Level.WARNING, "Found outdated configuration (layout.yml)! (Your version: "
							+ layout.getInt("config-version") + " | Newest version: " + lyver + ")");
				}
			} else {
				plugin.saveResource("layout.yml", false);
				layout = YamlConfiguration.loadConfiguration(layout_file);
				resultMsg += "The 'layout.yml' file successfully created!";
			}

			if (sign_file.exists()) {
				sign = YamlConfiguration.loadConfiguration(sign_file);
				sign.load(sign_file);
				sign.save(sign_file);
			} else {
				sign_file.createNewFile();
				sign = YamlConfiguration.loadConfiguration(sign_file);
				resultMsg += "The 'signs.yml' file successfully created!";
			}

			if (!resultMsg.isEmpty()) {
				plugin.logConsole(resultMsg);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			plugin.throwMsg();
		}

		loadSomeSettings();
		loadServers();
		loadLayouts();
		loadSigns();
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
		plugin.messages = null;
		layout = null;
		sign = null;

		servers.clear();
		signs.clear();
		layouts.clear();
	}

	private void loadSomeSettings() {
		this.externalServer = this.config.getBoolean("options.external-server");
		this.cooldown = (this.config.getInt("options.use-cooldown") * 1000);
		this.signUpdates = this.config.getInt("options.sign-updates");
		this.pingInterval = this.config.getInt("options.ping-interval");
		this.pingTimeout = this.config.getInt("options.ping-timeout");
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
			plugin.logConsole(Level.WARNING, "No saved sign was found.");
		} else {
			for (String sign : list) {
				try {
					Location location = LocationSerialiser.stringToLocationSign(sign);
					ServerInfo server = getServer(LocationSerialiser.getServerFromSign(sign));
					SignLayout layout = getLayout(LocationSerialiser.getLayoutFromSign(sign));

					if (location == null) {
						plugin.logConsole(Level.WARNING, "The location for the sign is null.");
						plugin.logConsole("Probably world not exists or the sign was broken.");
						break;
					}

					Block b = location.getBlock();
					if (b.getState() instanceof Sign) {
						TeleportSign tsign = new TeleportSign(server, location, layout);
						this.signs.add(tsign);
						this.blocks.add(b);
					}
				} catch (Throwable e) {
					e.printStackTrace();
					plugin.throwMsg();
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
		if (type.equals(ConfigType.CONFIG)) {
			return config;
		} else if (type.equals(ConfigType.LAYOUTS)) {
			return layout;
		} else if (type.equals(ConfigType.SIGNS)) {
			return sign;
		}

		return null;
	}

	public boolean isExternal() {
		return externalServer;
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
			plugin.throwMsg();
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
					plugin.throwMsg();
				}

				blocks.remove(location.getBlock());
				signs.remove(sign);
				break;
			}
		}
	}
}
