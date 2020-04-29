package hu.montlikadani.TeleportSigns.api;

import java.util.Set;

import org.bukkit.event.Cancellable;

import hu.montlikadani.TeleportSigns.server.ServerInfo;

public class TeleportSignsPingEvent extends BaseEvent implements Cancellable {

	private Set<ServerInfo> servers;
	private boolean cancelled = false;

	public TeleportSignsPingEvent(Set<ServerInfo> servers) {
		this.servers = servers;
	}

	public Set<ServerInfo> getServers() {
		return servers;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
}
