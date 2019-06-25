package hu.montlikadani.TeleportSigns.api;

import java.util.List;

import org.bukkit.event.Cancellable;

import hu.montlikadani.TeleportSigns.ServerInfo;

public class TeleportSignsPingEvent extends BaseEvent implements Cancellable {
	private List<ServerInfo> servers;
	private boolean cancelled = false;

	public TeleportSignsPingEvent(List<ServerInfo> servers) {
		this.servers = servers;
	}

	public List<ServerInfo> getServers() {
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
