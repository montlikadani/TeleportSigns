package hu.montlikadani.TeleportSigns.api;

import hu.montlikadani.TeleportSigns.ServerInfo;

public class ServerChangeStatusEvent extends BaseEvent {
	private ServerInfo server;
	private String status;

	public ServerChangeStatusEvent(ServerInfo server, String status) {
		this.server = server;
		this.status = status;
	}

	public ServerInfo getServer() {
		return server;
	}

	public String getStatus() {
		return status;
	}
}
