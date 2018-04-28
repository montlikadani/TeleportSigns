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

public class TeleportSigns extends JavaPlugin implements PluginMessageListener {
	private PingScheduler ping;
	private SignScheduler sign;
	private AnimationTask anim;
	private static TeleportSigns instance;
	private static ConfigData data;

	FileConfiguration config = getConfig();
	FileConfiguration layout, signf, messages;
	File config_file = new File("plugins/TeleportSigns/config.yml");
	File layout_file = new File("plugins/TeleportSigns/layout.yml");
	File sign_file = new File("plugins/TeleportSigns/signs.yml");
	File messages_file = new File("plugins/TeleportSigns/messages.yml");

	@Override
	public void onEnable() {
		try {
			super.onEnable();
			createFiles();
	        if (!getConfig().getBoolean("enabled")) {
	            this.getServer().getPluginManager().disablePlugin(this);
	            return;
	        }
			if (!Bukkit.getBukkitVersion().split("\\.")[1].substring(0, 1).equals("8")) {
				Bukkit.getServer().getConsoleSender().sendMessage("§cIncorrect Bukkit/Spigot version, not loading plugin. Version support: 1.8.x");
				return;
			}
			instance = this;
			data = new ConfigData(this);
			this.ping = new PingScheduler(this);
			this.sign = new SignScheduler(this);
			data.loadConfig();
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
			loadConfigs();
			if (getConfig().getString("options.connect-timeout") != null) {
				setBukkitConnectTimeOut();
			}
			//setSpigotBungeeBoolean();
			if (getConfig().getBoolean("check-update")) {
				logConsole(Level.INFO, checkVersion());
			}
			if (getConfig().getBoolean("metrics")) {
				Metrics metrics = new Metrics(this);
				Boolean backgr = getConfig().getBoolean("options.background-enable");
				metrics.addCustomChart(new Metrics.SimplePie("background_type", new Callable<String>() {
		            @Override
		            public String call() throws Exception {
		                return getConfig().getString("options.background").toString();
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
			if (getConfig().getBoolean("plugin-enable-message")) {
				getServer().getConsoleSender().sendMessage(colorMsg(getConfig().getString("plugin-enable").replace("%newline%", "\n").replace("%prefix%", messages.getString("prefix"))));
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
			data.unloadConfig();
	    	Messenger messenger = Bukkit.getServer().getMessenger();
	    	messenger.unregisterIncomingPluginChannel(instance, "BungeeCord", instance);
	    	messenger.unregisterOutgoingPluginChannel(instance);
	    	instance = null;
			getServer().getScheduler().cancelTasks(this);
			if (getConfig().getBoolean("plugin-disable-message")) {
				getServer().getConsoleSender().sendMessage(colorMsg(getConfig().getString("plugin-disable").replace("%newline%", "\n").replace("%prefix%", messages.getString("prefix"))));
			}
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}
	}

	public void loadConfigs() {
		try {
			createFiles();
			config.load(config_file);
			messages.load(messages_file);
			layout.load(layout_file);
			signf.save(sign_file);
			signf.load(sign_file);
			YamlConfiguration.loadConfiguration(new File(getDataFolder(), "layout.yml"));
			YamlConfiguration.loadConfiguration(new File(getDataFolder(), "signs.yml"));
			reloadConfig();
			saveDefaultConfig();
		} catch (Exception e) {
			e.printStackTrace();
			throwMsg();
		}
	}

    public void createFiles() {
		if (config_file.exists()) {
			config = YamlConfiguration.loadConfiguration(config_file);
		} else {
			saveResource("config.yml", false);
			config = YamlConfiguration.loadConfiguration(config_file);
			logConsole(Level.INFO, "The 'config.yml' file successfully created!");
		}
		if (messages_file.exists()) {
			messages = YamlConfiguration.loadConfiguration(messages_file);
		} else {
			saveResource("messages.yml", false);
			messages = YamlConfiguration.loadConfiguration(messages_file);
			logConsole(Level.INFO, "The 'messages.yml' file successfully created!");
		}
		if (layout_file.exists()) {
			layout = YamlConfiguration.loadConfiguration(layout_file);
		} else {
			saveResource("layout.yml", false);
			layout = YamlConfiguration.loadConfiguration(layout_file);
			logConsole(Level.INFO, "The 'layout.yml' file successfully created!");
		}
		if (sign_file.exists()) {
			signf = YamlConfiguration.loadConfiguration(sign_file);
		} else {
			saveResource("signs.yml", false);
			signf = YamlConfiguration.loadConfiguration(sign_file);
			logConsole(Level.INFO, "The 'signs.yml' file successfully created!");
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
                return "[TeleportSigns] New version (" + versionString + ") is available at https://www.spigotmc.org/resources/teleport-signs.37446/";
            } else {
                return "[TeleportSigns] You're running the latest version.";
            }
        } catch (Exception e) {
        	e.printStackTrace();
            pl.logConsole(Level.WARNING, "Failed to compare versions. " + e + " Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
        }
        return "[TeleportSigns] Failed to get newest version number.";
    }

	public static TeleportSigns getInstance() {
		return instance;
	}

	public ConfigData getConfigData() {
		return data;
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
		if (getConfig().getBoolean("options.logconsole")) {
			Bukkit.getLogger().log(level, "[TeleportSigns] " + error);
		}
		if (getConfig().getBoolean("log-to-file")) {
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

	public static String getIP(Player player) {
		String playerIP = player.getAddress().getAddress().getHostAddress();
		return playerIP;
	}

	public static int getPort(Player player) {
		int playerPort = player.getServer().getPort();
		return playerPort;
	}

    public String colorMsg(String msg) {
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void reload() {
		getConfigData().unloadConfig();
		loadConfigs();
		getConfigData().reloadConfig();
		Bukkit.getPluginManager().disablePlugin(this);
		Bukkit.getPluginManager().enablePlugin(this);
	}

/**	private void setSpigotBungeeBoolean() {
		YamlConfiguration spigFile = Bukkit.spigot().getConfig();
		if (spigFile == null) {
			logConsole(Level.WARNING, "[TeleportSigns] WARNING! The spigot.yml file can not be found!");
			return;
		}
		if (spigFile.getBoolean("settings.bungeecord", true)) {
			return;
		} else {
			spigFile.set("settings.bungeecord", true);
			try {
				spigFile.save("spigot.yml");
				spigFile.options().copyDefaults(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}*/

	private void setBukkitConnectTimeOut() {
		File bukFile = new File(Bukkit.getServer().getWorldContainer().getName(), "bukkit.yml");
		if (!bukFile.exists()) {
			logConsole(Level.WARNING, "WARNING! The bukkit.yml file can not be found!");
			return;
		}
		FileConfiguration bfi = YamlConfiguration.loadConfiguration(bukFile);
		bfi.set("settings.connection-throttle", Integer.valueOf(getConfig().getInt("options.connect-timeout")));
		try {
			bfi.save(bukFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void throwMsg() {
		logConsole(Level.WARNING, "There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		return;
	}
}