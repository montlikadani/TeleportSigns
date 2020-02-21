package hu.montlikadani.TeleportSigns.api;

import hu.montlikadani.TeleportSigns.Server;
import hu.montlikadani.TeleportSigns.Server.SResponse;
import hu.montlikadani.TeleportSigns.ServerInfo;

public class ServerPingResponseEvent extends BaseEvent {

	private ServerInfo server;
	private Server ping;
	private SResponse response;

	public ServerPingResponseEvent(ServerInfo server, Server ping, SResponse response) {
		this.server = server;
		this.ping = ping;
		this.response = response;
	}

	public ServerInfo getServer() {
		return server;
	}

	public Server getPing() {
		return ping;
	}

	public SResponse getResponse() {
		return response;
	}
}
