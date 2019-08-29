package hu.montlikadani.TeleportSigns;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.StandardSystemProperty;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;

import static hu.montlikadani.TeleportSigns.Messager.logConsole;
import static hu.montlikadani.TeleportSigns.Messager.defaults;
import static hu.montlikadani.TeleportSigns.Messager.colorMsg;

public class TeleportSigns extends JavaPlugin {

	private static TeleportSigns instance;

	private PingScheduler ping = null;
	private SignScheduler sign = null;
	private AnimationTask anim = null;
	private ConfigData data = null;

	@Override
	public void onEnable() {
		instance = this;

		try {
			if (!checkJavaVersion()) {
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

			// Just a version check
			// hack we checking the version to mc 1.0, lol
			if (isLower("1.7", "1.0")) {
				getLogger().log(Level.SEVERE, "Your server version does not supported by this plugin! Please use 1.8+ or higher versions!");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

			data = new ConfigData(this);
			data.loadConfig();

			ping = new PingScheduler(this);
			getServer().getPluginManager().registerEvents(ping, this);

			anim = new AnimationTask(this);
			anim.resetAnimation();
			anim.startAnimation();

			sign = new SignScheduler(this);
			getServer().getPluginManager().registerEvents(sign, this);

			long time = (long) (10.3 * 20L);
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					Bukkit.getScheduler().runTaskLater(instance, sign, 40L);
					Bukkit.getScheduler().runTaskLaterAsynchronously(instance, ping, 5L);

					Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(instance, "BungeeCord");
				}
			}, time);

			Bukkit.getPluginManager().registerEvents(new Listeners(this), this);

			Commands cmds = new Commands(this);
			getCommand("teleportsigns").setExecutor(cmds);
			getCommand("teleportsigns").setTabCompleter(cmds);

			if (getMainConf().getBoolean("check-update")) {
				logConsole(checkVersion("console"));
			}

			if (isHigher("1.8.6", "1.8.5")) {
				Metrics metrics = new Metrics(this);
				if (metrics.isEnabled()) {
					metrics.addCustomChart(new Metrics.SimplePie("background_type", () -> data.getBackgroundType()));
					metrics.addCustomChart(new Metrics.SimplePie("using_background",
							() -> getMainConf().getString("options.background.enable")));
					metrics.addCustomChart(new Metrics.SingleLineChart("sign_count", data.getSigns()::size));
					metrics.addCustomChart(new Metrics.SingleLineChart("server_count", data.getServers()::size));
					logConsole("Metrics enabled.");
				}
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
				anim = null;
			}
			Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(instance, "BungeeCord");
			HandlerList.unregisterAll(this);
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

	String checkVersion(String sender) {
		String[] nVersion;
		String[] cVersion;
		String lineWithVersion;
		String msg = "";
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
					msg = "New version (" + versionString + ") is available at https://www.spigotmc.org/resources/37446/";
				} else if (sender.equals("player")) {
					msg = colorMsg("&8&m&l--------------------------------------------------\n" +
							getMsg("prefix") + "&a A new update is available!&4 Version:&7 " + versionString +
							"\n&6Download:&c &nhttps://www.spigotmc.org/resources/37446/" +
							"\n&8&m&l--------------------------------------------------");
				}
			} else {
				if (sender.equals("console")) {
					msg = "You're running the latest version.";
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logConsole(Level.WARNING, "Failed to compare versions. " + e + " Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}

		/*if (sender.equals("console")) {
			msg = "Failed to get newest version number.";
		}*/

		return msg;
	}

	/**
	 * Call an event with asynchronous or without.
	 * @param event Event
	 */
	public void callEvent(final Event event) {
		if (!event.isAsynchronous()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> getServer().getPluginManager().callEvent(event));
		} else {
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	public void reload() {
		data.loadConfig();

		if (ping.task != null) {
			Bukkit.getScheduler().cancelTask(ping.task.getTaskId());
		}
		if (ping.pingTask != null) {
			Bukkit.getScheduler().cancelTask(ping.pingTask.getTaskId());
		}
		if (sign.task != null) {
			Bukkit.getScheduler().cancelTask(sign.task.getTaskId());
		}

		ping = null;
		sign = null;
		HandlerList.unregisterAll(this);

		ping = new PingScheduler(this);
		sign = new SignScheduler(this);

		Bukkit.getPluginManager().registerEvents(ping, this);
		Bukkit.getPluginManager().registerEvents(sign, this);
		Bukkit.getPluginManager().registerEvents(new Listeners(this), this);

		anim.resetAnimation();
		anim.stopAnimation();
		anim.startAnimation();

		Bukkit.getScheduler().runTaskLater(this, sign, 40L);
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, ping, 5L);
	}

	String getMsg(String key, Object... placeholders) {
		String msg = "";

		if (!getMessages().contains(key) || getMessages().getString(key).equals(""))
			return msg;

		msg = colorMsg(getMessages().getString(key));

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

	/**
	 * Gets the plugin instance in this class
	 * @return {@link #TeleportSigns()} instance
	 */
	public static TeleportSigns getInstance() {
		return instance;
	}

	public ConfigData getConfigData() {
		return data;
	}

	public FileConfiguration getMainConf() {
		return data.getConfig(ConfigType.CONFIG);
	}

	public FileConfiguration getMessages() {
		return data.getConfig(ConfigType.MESSAGES);
	}

	public boolean isHigher(String highest, String lowest) {
		return convertVersion(highest) > convertVersion(lowest);
	}

	public boolean isLower(String highest, String lowest) {
		return convertVersion(highest) < convertVersion(lowest);
	}

	public boolean isCurrentEqualOrHigher(String lowest, String highest) {
		return convertVersion(highest) >= convertVersion(lowest);
	}

	public boolean isCurrentEqualOrLower(String highest, String lowest) {
		return convertVersion(highest) <= convertVersion(lowest);
	}

	private boolean checkJavaVersion() {
		try {
			if (Float.parseFloat(StandardSystemProperty.JAVA_CLASS_VERSION.value()) < 52.0) {
				getLogger().log(Level.WARNING, "You are using an older Java that is not supported. Please use 1.8 or higher versions!");
				return false;
			}
		} catch (NumberFormatException e) {
			getLogger().log(Level.WARNING, "Failed to detect Java version.");
			return false;
		}
		return true;
	}

	private Integer convertVersion(String v) {
		v = v.replaceAll("[^\\d.]", "");
		Integer version = 0;

		if (v.contains(".")) {
			String lVersion = "";
			for (String one : v.split("\\.")) {
				String s = one;
				if (s.length() == 1)
					s = "0" + s;

				lVersion += s;
			}

			try {
				version = Integer.parseInt(lVersion);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			try {
				version = Integer.parseInt(v);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return version;
	}
}