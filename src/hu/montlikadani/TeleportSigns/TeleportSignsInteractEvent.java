package hu.montlikadani.TeleportSigns;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeleportSignsInteractEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private TeleportSign sign;
	private ServerInfo server;
	private boolean cancelled;

	public TeleportSignsInteractEvent(Player player, TeleportSign sign, ServerInfo server) {
		this.player = player;
		this.sign = sign;
		this.server = server;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
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

	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public boolean isCancelled() {
		return cancelled;
	}
}
