package hu.montlikadani.TeleportSigns.api;

import hu.montlikadani.TeleportSigns.ServerInfo;
import hu.montlikadani.TeleportSigns.ServerPing;
import hu.montlikadani.TeleportSigns.ServerPing.SResponse;

public class ServerPingResponseEvent extends BaseEvent {
	private ServerInfo server;
	private ServerPing ping;
	private SResponse response;

	public ServerPingResponseEvent(ServerInfo server, ServerPing ping, SResponse response) {
		this.server = server;
		this.ping = ping;
		this.response = response;
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
