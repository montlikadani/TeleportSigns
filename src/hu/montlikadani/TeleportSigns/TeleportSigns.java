package hu.montlikadani.TeleportSigns;

import static hu.montlikadani.TeleportSigns.utils.Util.colorMsg;
import static hu.montlikadani.TeleportSigns.utils.Util.logConsole;
import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;
import hu.montlikadani.TeleportSigns.commands.Commands;
import hu.montlikadani.TeleportSigns.server.ServerVersion;
import hu.montlikadani.TeleportSigns.server.ServerVersion.Version;
import hu.montlikadani.TeleportSigns.sign.SignScheduler;

public class TeleportSigns extends JavaPlugin {

	private static TeleportSigns instance;
	private static ServerVersion serverVersion = null;

	private PingScheduler ping = null;
	private SignScheduler sign = null;
	private AnimationTask anim = null;
	private ConfigData data = null;

	@Override
	public void onEnable() {
		long load = System.currentTimeMillis();

		instance = this;

		try {
			serverVersion = new ServerVersion();

			if (Version.isCurrentLower(Version.v1_8_R1)) {
				logConsole(Level.SEVERE,
						"Your server version does not supported by this plugin! Please use 1.8+ or higher versions!",
						false);
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

			data = new ConfigData(this);
			data.loadConfig();

			ping = new PingScheduler(this);
			anim = new AnimationTask(this);
			sign = new SignScheduler(this);

			anim.resetAnimation();
			anim.startAnimation();

			delayPinging();

			Stream.of(sign, ping, new Listeners(this))
					.forEach(l -> getServer().getPluginManager().registerEvents(l, this));

			Commands cmds = new Commands(this);
			getCommand("teleportsigns").setExecutor(cmds);
			getCommand("teleportsigns").setTabCompleter(cmds);

			if (getMainConf().getBoolean("check-update")) {
				logConsole(checkVersion("console"));
			}

			Metrics metrics = new Metrics(this, 1246);
			if (metrics.isEnabled()) {
				metrics.addCustomChart(new Metrics.SimplePie("background_type", data::getBackgroundType));
				metrics.addCustomChart(new Metrics.SimplePie("using_background",
						() -> getMainConf().getString("options.background.enable")));
				metrics.addCustomChart(new Metrics.SingleLineChart("sign_count", data.getSigns()::size));
				metrics.addCustomChart(new Metrics.SingleLineChart("server_count", data.getServers()::size));
				logConsole("Metrics enabled.");
			}

			if (data.isLogConsole()) {
				String msg = "&6[&2Teleport&eSigns&6]&7 >&a The plugin successfully enabled&6 v"
						+ getDescription().getVersion() + "&a! (" + (System.currentTimeMillis() - load) + "ms)";
				sendMsg(getServer().getConsoleSender(), colorMsg(msg));
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logConsole(Level.WARNING,
					"There was an error. Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues",
					false);
		}
	}

	@Override
	public void onDisable() {
		if (instance == null)
			return;

		if (anim != null) {
			anim.resetAnimation();
			anim.stopAnimation();
			anim = null;
		}

		HandlerList.unregisterAll(this);

		getServer().getScheduler().cancelTasks(this);

		if (data.isLogConsole()) {
			String msg = "&6[&2Teleport&eSigns&6]&7 >&c The plugin successfully disabled!";
			sendMsg(getServer().getConsoleSender(), colorMsg(msg));
		}

		data.unloadConfig();

		instance = null;
	}

	String checkVersion(String sender) {
		String[] nVersion;
		String[] cVersion;
		String lineWithVersion = "";
		String msg = "";
		try {
			URL githubUrl = new URL("https://raw.githubusercontent.com/montlikadani/TeleportSigns/master/plugin.yml");
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
			nVersion = versionString.replaceAll("[^0-9.]", "").split("\\.");
			double newestVersionNumber = Double.parseDouble(nVersion[0] + "." + nVersion[1]);

			cVersion = getDescription().getVersion().replaceAll("[^0-9.]", "").split("\\.");
			double currentVersionNumber = Double.parseDouble(cVersion[0] + "." + cVersion[1]);

			if (newestVersionNumber > currentVersionNumber) {
				if ("console".equals(sender)) {
					msg = "New version (" + versionString
							+ ") is available at https://www.spigotmc.org/resources/37446/";
				} else if ("player".equals(sender)) {
					msg = colorMsg("&8&m&l--------------------------------------------------\n"
							+ "&aA new update is available!&4 Version:&7 " + versionString
							+ "\n&6Download:&c &nhttps://www.spigotmc.org/resources/37446/"
							+ "\n&8&m&l--------------------------------------------------");
				}
			} else {
				if ("console".equals(sender)) {
					msg = "You're running the latest version.";
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logConsole(Level.WARNING, "Failed to compare versions. " + e
					+ " Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
		}

		return msg;
	}

	/**
	 * Call an event with asynchronous or without.
	 * @param event Event
	 */
	public void callEvent(final Event event) {
		if (!event.isAsynchronous()) {
			getServer().getScheduler().scheduleSyncDelayedTask(this,
					() -> getServer().getPluginManager().callEvent(event));
		} else {
			getServer().getPluginManager().callEvent(event);
		}
	}

	public void reload() {
		data.loadConfig();

		getServer().getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);

		Stream.of(sign, ping, new Listeners(this)).forEach(l -> getServer().getPluginManager().registerEvents(l, this));

		anim.resetAnimation();
		anim.stopAnimation();
		anim.startAnimation();

		delayPinging();
	}

	protected void delayPinging() {
		long time = (long) (10.3 * 20L);

		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			Bukkit.getScheduler().runTaskLater(instance, sign, 40L);
			Bukkit.getScheduler().runTaskLaterAsynchronously(instance, ping, 5L);
		}, time);
	}

	public String getMsg(String key, Object... placeholders) {
		String msg = "";

		if (getMessages().getString(key, "").isEmpty())
			return msg;

		msg = colorMsg(getMessages().getString(key));

		if (placeholders != null) {
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
	 * @return {@link TeleportSigns} instance
	 */
	public static TeleportSigns getInstance() {
		return instance;
	}

	public ConfigData getConfigData() {
		return data;
	}

	/**
	 * Gets the server version
	 * @return {@link ServerVersion}
	 */
	public static ServerVersion getServerVersion() {
		return serverVersion;
	}

	public FileConfiguration getMainConf() {
		return data.getConfig(ConfigType.CONFIG);
	}

	public FileConfiguration getMessages() {
		return data.getConfig(ConfigType.MESSAGES);
	}
}