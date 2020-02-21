package hu.montlikadani.TeleportSigns;

import static hu.montlikadani.TeleportSigns.utils.Util.logConsole;

import java.net.ConnectException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.TeleportSigns.Server.SResponse;
import hu.montlikadani.TeleportSigns.api.ServerChangeStatusEvent;
import hu.montlikadani.TeleportSigns.api.ServerPingResponseEvent;
import hu.montlikadani.TeleportSigns.api.TeleportSignsPingEvent;

public class PingScheduler implements Runnable, Listener {

	private final TeleportSigns plugin;

	PingScheduler(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		List<ServerInfo> servers = plugin.getConfigData().getServers();
		TeleportSignsPingEvent event = new TeleportSignsPingEvent(servers);
		plugin.callEvent(event);
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, plugin.getConfigData().getPingInterval() * 20);
	}

	@EventHandler
	public void onEvent(TeleportSignsPingEvent e) {
		if (e.isCancelled()) {
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			for (ServerInfo server : e.getServers()) {
				if (!server.isLocal()) {
					pingAsync(server);
					continue;
				}

				String status = server.getMotd();
				String version = getBukkitVersion();

				server.setProtocol(version);
				server.setVersion(formatVersion(version));
				server.setMotd(Bukkit.getMotd());
				server.setMaxPlayers(Bukkit.getMaxPlayers());
				server.setPlayerCount(Bukkit.getOnlinePlayers().size());
				server.setPingStart(System.currentTimeMillis());
				server.setPingEnd(System.currentTimeMillis());
				server.setOnline(true);

				if (!server.getMotd().equals(status)) {
					ServerChangeStatusEvent sevent = new ServerChangeStatusEvent(server, server.getMotd());
					plugin.callEvent(sevent);
				}
			}
		}, 20);
	}

	private void pingAsync(final ServerInfo server) {
		final Server ping = plugin.getConfigData().isExternal() ? server.getExternalPing() : server.getInternalPing();
		if (ping.isFetching()) {
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			long pingStartTime = System.currentTimeMillis();

			ping.setAddress(server.getAddress());
			ping.setTimeout(server.getTimeout());
			ping.setFetching(true);

			try {
				String status = server.getMotd();
				SResponse response = ping.fetchData();

				if (!plugin.getConfigData().isExternal()) {
					server.setVersion(formatVersion(response.version));
					server.setProtocol(response.protocol);
				}
				server.setMotd(response.description);
				server.setPlayerCount(response.players);
				server.setMaxPlayers(response.slots);
				server.setPingStart(pingStartTime);
				server.setOnline(true);

				ServerPingResponseEvent revent = new ServerPingResponseEvent(server, ping, response);
				plugin.callEvent(revent);

				if (!server.getMotd().equals(status)) {
					ServerChangeStatusEvent sevent = new ServerChangeStatusEvent(server, server.getMotd());
					plugin.callEvent(sevent);
				}
			} catch (Throwable e) {
				server.setOnline(false);

				if (!(e instanceof ConnectException)) {
					logConsole(java.util.logging.Level.WARNING,
							"Error fetching data from server '" + server.getAddress().getAddress().getHostAddress()
									+ ":" + server.getAddress().getPort() + "'",
							false);
				}
			} finally {
				ping.setFetching(false);
				server.setPingEnd(System.currentTimeMillis());
			}
		});
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