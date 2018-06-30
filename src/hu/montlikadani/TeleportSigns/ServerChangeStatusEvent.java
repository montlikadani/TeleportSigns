package hu.montlikadani.TeleportSigns;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerChangeStatusEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private ServerInfo server;
	private String status;
	private boolean cancelled = false;

	public ServerChangeStatusEvent(ServerInfo server, String status) {
		this.server = server;
		this.status = status;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ServerInfo getServer() {
		return server;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
