package hu.montlikadani.TeleportSigns;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import hu.montlikadani.TeleportSigns.ServerPing.SResponse;

public class PingScheduler implements Runnable, Listener {
	private final TeleportSigns plugin;

	public PingScheduler(TeleportSigns plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void run() {
		final List<ServerInfo> servers = plugin.getConfigData().getServers();
		TeleportSignsPingEvent event = new TeleportSignsPingEvent(servers);
		Bukkit.getPluginManager().callEvent(event);
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, plugin.getConfigData().getPingInterval() * 20);
	}

	@EventHandler
	public void onEvent(TeleportSignsPingEvent e) {
		if (!e.isCancelled()) {
			for (ServerInfo server : e.getServers()) {
				if (!server.isLocal()) {
					pingAsync(server);
				} else {
					final String status = server.getMotd();
					ServerListPingEvent ping = new ServerListPingEvent(new InetSocketAddress(server.getAddress().getAddress().getHostAddress().toString(), 
							server.getAddress().getPort()).getAddress(), Bukkit.getMotd(), Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers());
					Bukkit.getPluginManager().callEvent(ping);
					server.setProtocol(getBukkitVersion());
					server.setMotd(ping.getMotd());
					server.setMaxPlayers(ping.getMaxPlayers());
					server.setPlayerCount(ping.getNumPlayers());
					server.setPingStart(System.currentTimeMillis());
					server.setPingEnd(System.currentTimeMillis());
					server.setOnline(true);

					if (!server.getMotd().equals(status)) {
						ServerChangeStatusEvent sevent = new ServerChangeStatusEvent(server, server.getMotd());
						plugin.callSyncEvent(sevent);
					}
				}
			}
		}
	}

	private void pingAsync(final ServerInfo server) {
		final ServerPing ping = server.getPing();
		if (!ping.isFetching()) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					long pingStartTime = System.currentTimeMillis();
					ping.setAddress(server.getAddress());
					ping.setTimeout(server.getTimeout());
					ping.setFetching(true);

					try {
						final String status = server.getMotd();
						SResponse response = ping.fetchData();
						server.setVersion(formatVersion(response.version));
						server.setProtocol(response.protocol);
						server.setMotd(response.description);
						server.setPlayerCount(response.players);
						server.setMaxPlayers(response.slots);
						server.setPingStart(pingStartTime);
						server.setOnline(true);

						ServerPingResponseEvent revent = new ServerPingResponseEvent(server, ping, response);
						plugin.callSyncEvent(revent);

						if (!server.getMotd().equals(status)) {
							ServerChangeStatusEvent sevent = new ServerChangeStatusEvent(server, server.getMotd());
							plugin.callSyncEvent(sevent);
						}
					} catch (Exception e) {
						server.setOnline(false);
						if (!(e instanceof ConnectException)) {
							plugin.logConsole(Level.WARNING, "Error fetching data from server '" + String.valueOf(server.getAddress().getAddress().getHostAddress().toString())
							+ ":" + server.getAddress().getPort() + "'. - Check the config file!");
						}
					}
					finally {
						ping.setFetching(false);
						server.setPingEnd(System.currentTimeMillis());
					}
				}
			});
		}
	}

	private String getBukkitVersion() {
		String version = Bukkit.getVersion();
		version = version.replace("(", "");
		version = version.replace(")", "");
		version = version.split(" ")[2];
		return version;
	}

	private String formatVersion(String version) {
		char[] numbers = "0123456789".toCharArray();
		for (int i = 0; i < version.length(); i++) {
			char c = version.charAt(i);
			for (char ch : numbers) {
				if (ch == c) {
					version = version.substring(i);
					break;
				}
			}
		}
		return version;
	}
}