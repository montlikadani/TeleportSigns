package hu.montlikadani.TeleportSigns.api;

import hu.montlikadani.TeleportSigns.ServerInfo;
import hu.montlikadani.TeleportSigns.ServerPingExternal;
import hu.montlikadani.TeleportSigns.ServerPingInternal;
import hu.montlikadani.TeleportSigns.ServerPingInternal.SResponse;

public class ServerPingResponseEvent extends BaseEvent {
	private ServerInfo server;
	private ServerPingExternal externalPing;
	private ServerPingInternal internalping;
	private hu.montlikadani.TeleportSigns.ServerPingExternal.SResponse response2;
	private SResponse response;

	public ServerPingResponseEvent(ServerInfo server, ServerPingExternal externalPing,
			hu.montlikadani.TeleportSigns.ServerPingExternal.SResponse response2) {
		this.server = server;
		this.externalPing = externalPing;
		this.response2 = response2;
	}

	public ServerPingResponseEvent(ServerInfo server, ServerPingInternal internalping, SResponse response) {
		this.server = server;
		this.internalping = internalping;
		this.response = response;
	}

	public ServerInfo getServer() {
		return server;
	}

	public ServerPingExternal getExternalPing() {
		return externalPing;
	}

	public ServerPingInternal getInternalPing() {
		return internalping;
	}

	/**
	 * @deprecated Use {@link #getInternalPing()}
	 * @return internal ping
	 */
	@Deprecated
	public ServerPingInternal getPing() {
		return internalping;
	}

	public SResponse getResponse() {
		return response;
	}

	public hu.montlikadani.TeleportSigns.ServerPingExternal.SResponse getExternalResponse() {
		return response2;
	}
}
