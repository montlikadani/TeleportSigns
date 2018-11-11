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

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;

public class TeleportSigns extends JavaPlugin implements PluginMessageListener {

	private static TeleportSigns instance;

	FileConfiguration messages;
	private File messages_file = new File(getDataFolder(), "messages.yml");

	private int msver = 3;
	private PingScheduler ping;
	private SignScheduler sign;
	private AnimationTask anim;
	private ConfigData data;

	@Override
	public void onEnable() {
		try {
			data = new ConfigData(this);
			data.loadConfig();
			if (!getMainConf().getBoolean("enabled")) {
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			/*if (!Bukkit.getBukkitVersion().split("\\.")[1].substring(0, 1).equals("8")) {
				Bukkit.getServer().getConsoleSender().sendMessage("§cIncorrect Bukkit/Spigot version, not loading plugin. Version support: 1.8.x");
				setEnabled(false);
				return;
			}*/
			instance = this;
			ping = new PingScheduler(this);
			sign = new SignScheduler(this);
			anim = new AnimationTask(this);
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
			getCommand("teleportsigns").setExecutor(new Commands(this));
			getCommand("teleportsigns").setTabCompleter(new Commands(this));
			if (getMainConf().getBoolean("check-update")) {
				logConsole(Level.INFO, checkVersion());
			}
			if (getMainConf().getBoolean("metrics")) {
				Metrics metrics = new Metrics(this);
				Boolean backgr = getMainConf().getBoolean("options.background.enable");
				metrics.addCustomChart(new Metrics.SimplePie("background_type", new Callable<String>() {
					@Override
					public String call() throws Exception {
						return getMainConf().getString("options.background.type");
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
			if (getMainConf().getString("plugin-enable") != null && !getMainConf().getString("plugin-enable").equals("")) {
				getServer().getConsoleSender().sendMessage(defaults(getMainConf().getString("plugin-enable")));
			}
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
	}

	@Override
	public void onDisable() {
		if (!isEnabled()) return;

		try {
			if (anim != null) {
				anim.resetAnimation();
				anim.stopAnimation();
			}
			Messenger messenger = Bukkit.getServer().getMessenger();
			messenger.unregisterOutgoingPluginChannel(instance, "BungeeCord");
			messenger.unregisterIncomingPluginChannel(instance, "BungeeCord", instance);
			messenger = null;
			instance = null;
			getServer().getScheduler().cancelTasks(this);
			if (getMainConf().getString("plugin-disable") != null && !getMainConf().getString("plugin-disable").equals("")) {
				getServer().getConsoleSender().sendMessage(defaults(getMainConf().getString("plugin-disable")));
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

	private String checkVersion() {
		String[] nVersion;
		String[] cVersion;
		String lineWithVersion;
		try {
			URL githubUrl = new URL("https://raw.githubusercontent.com/montlikadani/TeleportSigns/master/plugin.yml");
			lineWithVersion = "";
			@SuppressWarnings("resource")
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
			cVersion = getDescription().getVersion().split("\\.");
			double currentVersionNumber = Double.parseDouble(cVersion[0] + "." + cVersion[1]);
			if (newestVersionNumber > currentVersionNumber) {
				return "New version (" + versionString + ") is available at https://www.spigotmc.org/resources/37446/";
			} else {
				return "You're running the latest version.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			logConsole(Level.WARNING, "Failed to compare versions. " + e + " Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
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
		if (getMainConf().getBoolean("options.logconsole")) {
			Bukkit.getLogger().log(level, "[TeleportSigns] " + error);
		}
		if (getMainConf().getBoolean("log-to-file")) {
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
				SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
				String time = df.format(dt);
				pw.println(time + " - [" + level + "] " + error);
				pw.flush();
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
				throwMsg();
			}
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] msg) {
		if (!channel.equals("BungeeCord")) {
			return;
		}

		ByteArrayDataInput in = ByteStreams.newDataInput(msg);
		String subchannel = in.readUTF();
		if (subchannel.equals("NULL")) {
		}
	}

	public void reload() {
		data.loadConfig();
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

	public FileConfiguration getMainConf() {
		return data.getConfig(ConfigType.CONFIG);
	}

	public String getBackgroundType() {
		return getMainConf().getString("options.background.type");
	}

	public String defaults(String str) {
		if (str.contains("%prefix%")) {
			str = str.replace("%prefix%", messages.getString("prefix"));
		}
		if (str.contains("%newline%")) {
			str = str.replace("%newline%", "\n");
		}
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