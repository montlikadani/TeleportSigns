package hu.montlikadani.TeleportSigns.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import hu.montlikadani.TeleportSigns.server.ServerInfo;
import hu.montlikadani.TeleportSigns.sign.TeleportSign;

public class TeleportSignsInteractEvent extends BaseEvent implements Cancellable {
	private Player player;
	private TeleportSign sign;
	private ServerInfo server;
	private boolean cancelled = false;

	public TeleportSignsInteractEvent(Player player, TeleportSign sign, ServerInfo server) {
		this.player = player;
		this.sign = sign;
		this.server = server;
	}

	public Player getPlayer() {
		return player;
	}

	public TeleportSign getSign() {
		return sign;
	}

	public ServerInfo getServer() {
		return server;
	}

	public void setServer(ServerInfo server) {
		this.server = server;
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
