package hu.montlikadani.TeleportSigns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	private File messages_file;

	private int msver = 3;
	private PingScheduler ping;
	private SignScheduler sign;
	private AnimationTask anim;
	private ConfigData data;

	@Override
	public void onEnable() {
		instance = this;

		try {
			if (instance == null) {
				getLogger().log(Level.SEVERE, "Plugin instance is null. Disabling...");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

			data = new ConfigData(this);
			data.loadConfig();
			if (!getMainConf().getBoolean("enabled")) {
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			File spigotFile = new File("spigot.yml");
			if (spigotFile.exists()) {
				org.spigotmc.SpigotConfig.config.set("settings.bungeecord", true);
				org.spigotmc.SpigotConfig.config.save(spigotFile);
			}
			ping = new PingScheduler(this);
			getServer().getPluginManager().registerEvents(ping, this);

			sign = new SignScheduler(this);
			getServer().getPluginManager().registerEvents(sign, this);

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
				logConsole(Level.INFO, checkVersion("console"));
			}
			if (getMainConf().getBoolean("metrics")) {
				Metrics metrics = new Metrics(this);
				metrics.addCustomChart(new Metrics.SimplePie("background_type", new Callable<String>() {
					@Override
					public String call() throws Exception {
						return getMainConf().getString("options.background.type");
					}
				}));
				metrics.addCustomChart(new Metrics.SimplePie("using_background", new Callable<String>() {
					@Override
					public String call() throws Exception {
						return getMainConf().getString("options.background.enable");
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
			if (getMainConf().contains("plugin-enable") && !getMainConf().getString("plugin-enable").equals("")) {
				getServer().getConsoleSender().sendMessage(defaults(getMainConf().getString("plugin-enable")));
			}
		} catch (Throwable e) {
			e.printStackTrace();
			getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
	}

	@Override
	public void onDisable() {
		if (instance == null) return;

		try {
			if (anim != null) {
				anim.resetAnimation();
				anim.stopAnimation();
			}
			anim = null;
			Messenger messenger = Bukkit.getServer().getMessenger();
			messenger.unregisterOutgoingPluginChannel(instance, "BungeeCord");
			messenger.unregisterIncomingPluginChannel(instance, "BungeeCord", instance);
			messenger = null;
			instance = null;
			getServer().getScheduler().cancelTasks(this);
			if (getMainConf().contains("plugin-disable") && !getMainConf().getString("plugin-disable").equals("")) {
				getServer().getConsoleSender().sendMessage(defaults(getMainConf().getString("plugin-disable")));
			}
			data.unloadConfig();
		} catch (Throwable e) {
			e.printStackTrace();
			getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
	}

	public void createMsgFile() {
		try {
			messages_file = new File(data.getFolder(), "messages.yml");
			if (messages_file.exists()) {
				messages = YamlConfiguration.loadConfiguration(messages_file);
				messages.load(messages_file);
				if (!messages.isSet("config-version") || !messages.get("config-version").equals(msver)) {
					logConsole(Level.WARNING, "Found outdated configuration (messages.yml)! (Your version: " + messages.getInt("config-version") + " | Newest version: " + msver + ")");
				}
			} else {
				saveResource("messages.yml", false);
				messages = YamlConfiguration.loadConfiguration(messages_file);
				logConsole(Level.INFO, "The 'messages.yml' file successfully created!");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throwMsg();
		}
	}

	public String checkVersion(String sender) {
		String[] nVersion;
		String[] cVersion;
		String lineWithVersion;
		try {
			URL githubUrl = new URL("https://raw.githubusercontent.com/montlikadani/TeleportSigns/master/plugin.yml");
			lineWithVersion = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(githubUrl.openStream()));
			String s;
			while ((s = br.readLine()) != null) {
				String line = s;
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
				if (sender.equals("console")) {
					return "New version (" + versionString + ") is available at https://www.spigotmc.org/resources/37446/";
				} else if (sender.equals("player")) {
					return colorMsg("&8&m&l--------------------------------------------------\n" +
							getMsg("prefix") + "&a A new update is available!&4 Version:&7 " + versionString +
							"\n&6Download:&c &nhttps://www.spigotmc.org/resources/37446/" +
							"\n&8&m&l--------------------------------------------------");
				}
			} else {
				if (sender.equals("console")) {
					return "You're running the latest version.";
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logConsole(Level.WARNING, "Failed to compare versions. " + e + " Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
		if (sender.equals("console")) {
			return "Failed to get newest version number.";
		}
		return "";
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
				File saveTo = new File(data.getFolder(), "log.txt");
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

		Bukkit.getScheduler().cancelTask(ping.task.getTaskId());
		Bukkit.getScheduler().cancelTask(ping.pingTask.getTaskId());
		ping = null;

		Bukkit.getScheduler().cancelTask(sign.task.getTaskId());
		sign = null;

		ping = new PingScheduler(this);
		sign = new SignScheduler(this);

		anim.resetAnimation();
		anim.stopAnimation();
		anim.startAnimation();

		Bukkit.getScheduler().runTaskLater(instance, sign, 40L);
		Bukkit.getScheduler().runTaskLaterAsynchronously(instance, ping, 5L);
	}

	public String getMsg(String key, Object... placeholders) {
		String msg = "";

		if (!messages.contains(key) || messages.getString(key).equals("")) return msg;

		msg = colorMsg(messages.getString(key));

		if (placeholders.length > 0) {
			for (int i = 0; i < placeholders.length; i++) {
				if (placeholders.length >= i + 2) {
					msg = msg.replace(String.valueOf(placeholders[i]), String.valueOf(placeholders[i + 1]));
				}
				i++;
			}
		}
		return msg;
	}

	public String sendMsg(org.bukkit.command.CommandSender sender, String s) {
		if (s != null && !s.equals("")) sender.sendMessage(s);
		return s;
	}

	public String replaceColor(String s) {
		return s.replace("&", "\u00a7");
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
			str = str.replace("%prefix%", getMsg("prefix"));
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