package hu.montlikadani.TeleportSigns;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;

public class TeleportSigns extends JavaPlugin implements PluginMessageListener {

	private static TeleportSigns instance;

	FileConfiguration messages;
	private File messages_file = new File("plugins/TeleportSigns/messages.yml");

	private int msver = 1;
	private PingScheduler ping;
	private SignScheduler sign;
	private AnimationTask anim;
	private static ConfigData data;

	@Override
	public void onEnable() {
		try {
			super.onEnable();
			data = new ConfigData(this);
			data.loadConfig();
			if (!data.getConfig(ConfigType.SETTINGS).getBoolean("enabled")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return;
			}
			if (!Bukkit.getBukkitVersion().split("\\.")[1].substring(0, 1).equals("8")) {
				Bukkit.getServer().getConsoleSender().sendMessage("§cIncorrect Bukkit/Spigot version, not loading plugin. Version support: 1.8.x");
				return;
			}
			instance = this;
			this.ping = new PingScheduler(this);
			this.sign = new SignScheduler(this);
			this.anim = new AnimationTask(this);
			anim.resetAnimation();
			anim.startAnimation();
			long time = (long) (10.3*20L);
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					Bukkit.getScheduler().runTaskLater(instance, sign, 40L);
					Bukkit.getScheduler().runTaskLaterAsynchronously(instance, ping, 5L);
					Bukkit.getPluginManager().registerEvents(new Listeners(instance), instance);

					Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(instance, "BungeeCord");
					Bukkit.getServer().getMessenger().registerIncomingPluginChannel(instance, "BungeeCord", instance);
				}
			}, time);
			registerCommands();
			if (data.getConfig(ConfigType.SETTINGS).getString("options.connect-timeout") != null) {
				setBukkitConnectTimeOut();
			}
			if (data.getConfig(ConfigType.SETTINGS).getBoolean("check-update")) {
				logConsole(Level.INFO, checkVersion());
			}
			if (data.getConfig(ConfigType.SETTINGS).getBoolean("metrics")) {
				Metrics metrics = new Metrics(this);
				Boolean backgr = data.getConfig(ConfigType.SETTINGS).getBoolean("options.background-enable");
				metrics.addCustomChart(new Metrics.SimplePie("background_type", new Callable<String>() {
					@Override
					public String call() throws Exception {
						return data.getConfig(ConfigType.SETTINGS).getString("options.background").toString();
					}
				}));
				metrics.addCustomChart(new Metrics.SimplePie("using_background", new Callable<String>() {
					@Override
					public String call() throws Exception {
						return backgr.toString();
					}
				}));
				metrics.addCustomChart(new Metrics.SingleLineChart("sign_count", new Callable<Integer>() {
					@Override
					public Integer call() throws Exception {
						return data.getSigns().size();
					}
				}));
				metrics.addCustomChart(new Metrics.SingleLineChart("server_count", new Callable<Integer>() {
					@Override
					public Integer call() throws Exception {
						return data.getServers().size();
					}
				}));
				logConsole(Level.INFO, "Metrics enabled.");
			}
			if (data.getConfig(ConfigType.SETTINGS).getBoolean("plugin-enable-message")) {
				getServer().getConsoleSender().sendMessage(defaults(data.getConfig(ConfigType.SETTINGS).getString("plugin-enable")));
			}
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
	}

	@Override
	public void onDisable() {
		try {
			super.onDisable();
			if (anim != null) {
				anim.resetAnimation();
				anim.stopAnimation();
			}
			Messenger messenger = Bukkit.getServer().getMessenger();
			messenger.unregisterIncomingPluginChannel(instance, "BungeeCord", instance);
			messenger.unregisterOutgoingPluginChannel(instance);
			instance = null;
			getServer().getScheduler().cancelTasks(this);
			if (data.getConfig(ConfigType.SETTINGS).getBoolean("plugin-disable-message")) {
				getServer().getConsoleSender().sendMessage(defaults(data.getConfig(ConfigType.SETTINGS).getString("plugin-disable")));
			}
			data.unloadConfig();
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
	}

	public void createMsgFile() {
		try {
			if (messages_file.exists()) {
				messages = YamlConfiguration.loadConfiguration(messages_file);
				messages.load(messages_file);
				if (!messages.isSet("config-version") || !messages.get("config-version").equals(msver)) {
					logConsole(Level.WARNING, "Found outdated configuration (messages.yml)! (Your version: " + messages.getString("config-version") + " | Newest version: " + msver + ")");
				}
			} else {
				saveResource("messages.yml", false);
				messages = YamlConfiguration.loadConfiguration(messages_file);
				logConsole(Level.INFO, "The 'messages.yml' file successfully created!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throwMsg();
		}
	}

	public static String checkVersion() {
		TeleportSigns pl = getPlugin(TeleportSigns.class);
		String[] nVersion;
		String[] cVersion;
		String lineWithVersion;
		try {
			URL githubUrl = new URL("https://raw.githubusercontent.com/montlikadani/TeleportSigns/master/plugin.yml");
			lineWithVersion = "";
			Scanner websiteScanner = new Scanner(githubUrl.openStream());
			while (websiteScanner.hasNextLine()) {
				String line = websiteScanner.nextLine();
				if (line.toLowerCase().contains("version")) {
					lineWithVersion = line;
					break;
				}
			}
			String versionString = lineWithVersion.split(": ")[1];
			nVersion = versionString.split("\\.");
			double newestVersionNumber = Double.parseDouble(nVersion[0] + "." + nVersion[1]);
			cVersion = pl.getDescription().getVersion().split("\\.");
			double currentVersionNumber = Double.parseDouble(cVersion[0] + "." + cVersion[1]);
			if (newestVersionNumber > currentVersionNumber) {
				return "New version (" + versionString + ") is available at https://www.spigotmc.org/resources/teleport-signs.37446/";
			} else {
				return "You're running the latest version.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			pl.logConsole(Level.WARNING, "Failed to compare versions. " + e + " Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
		return "Failed to get newest version number.";
	}

	public void callSyncEvent(final Event event) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				getServer().getPluginManager().callEvent(event);
			}
		});
	}

	public void logConsole(Level level, String error) {
		if (data.getConfig(ConfigType.SETTINGS).getBoolean("options.logconsole")) {
			Bukkit.getLogger().log(level, "[TeleportSigns] " + error);
		}
		if (data.getConfig(ConfigType.SETTINGS).getBoolean("log-to-file")) {
			try {
				File dataFolder = getDataFolder();
				if (!dataFolder.exists()) {
					dataFolder.mkdir();
				}
				File saveTo = new File(getDataFolder(), "log.txt");
				if (!saveTo.exists()) {
					saveTo.createNewFile();
				}
				FileWriter fw = new FileWriter(saveTo, true);
				PrintWriter pw = new PrintWriter(fw);
				Date dt = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss");
				String time = df.format(dt);
				pw.println(time + " - " + "[" + level + "] " + error);
				pw.flush();
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
				throwMsg();
			}
		}
	}

	private void registerCommands() {
		getCommand("teleportsigns").setExecutor(new Commands(this));
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] msg) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
	}

	private void setBukkitConnectTimeOut() {
		try {
			File bukFile = new File(Bukkit.getServer().getWorldContainer().getName(), "bukkit.yml");
			if (!bukFile.exists()) {
				logConsole(Level.WARNING, "WARNING! The bukkit.yml file can not be found!");
				return;
			}
			FileConfiguration bfi = YamlConfiguration.loadConfiguration(bukFile);
			bfi.set("settings.connection-throttle", Integer.valueOf(data.getConfig(ConfigType.SETTINGS).getInt("options.connect-timeout")));
			bfi.save(bukFile);
		} catch (Exception e) {
			e.printStackTrace();
			throwMsg();
		}
	}

	public void reload() {
		getConfigData().loadConfig();
		ping = null;
		sign = null;
		ping = new PingScheduler(this);
		sign = new SignScheduler(this);
		if (anim != null) {
			anim.resetAnimation();
			anim.stopAnimation();
		}
		anim.startAnimation();
	}

	public static TeleportSigns getInstance() {
		return instance;
	}

	public ConfigData getConfigData() {
		return data;
	}

	public String defaults(String str) {
		str = str.replace("%prefix%", messages.getString("prefix"));
		str = str.replace("%newline%", "\n");
		return colorMsg(str);
	}

	public String colorMsg(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public void throwMsg() {
		logConsole(Level.WARNING, "There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		return;
	}
}