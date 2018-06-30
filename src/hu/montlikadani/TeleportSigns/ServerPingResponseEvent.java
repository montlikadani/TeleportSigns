package hu.montlikadani.TeleportSigns;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import hu.montlikadani.TeleportSigns.ServerPing.StatusResponse;

public class ServerPingResponseEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private ServerInfo server;
	private ServerPing ping;
	private StatusResponse response;
	private boolean cancelled = false;

	public ServerPingResponseEvent(ServerInfo server, ServerPing ping, StatusResponse response) {
		this.server = server;
		this.ping = ping;
		this.response = response;
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

	public ServerPing getPing() {
		return ping;
	}

	public StatusResponse getResponse() {
		return response;
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
