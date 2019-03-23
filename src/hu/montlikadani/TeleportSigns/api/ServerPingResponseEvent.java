package hu.montlikadani.TeleportSigns.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import hu.montlikadani.TeleportSigns.ServerInfo;
import hu.montlikadani.TeleportSigns.ServerPing;
import hu.montlikadani.TeleportSigns.ServerPing.SResponse;

public class ServerPingResponseEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private ServerInfo server;
	private ServerPing ping;
	private SResponse response;

	public ServerPingResponseEvent(ServerInfo server, ServerPing ping, SResponse response) {
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

	public SResponse getResponse() {
		return response;
	}
}
