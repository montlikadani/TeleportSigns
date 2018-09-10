package hu.montlikadani.TeleportSigns;

import java.util.List;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeleportSignsPingEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private List<ServerInfo> servers;
	private boolean cancelled = false;

	public TeleportSignsPingEvent(List<ServerInfo> servers) {
		this.servers = servers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
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
