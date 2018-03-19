package hu.montlikadani.TeleportSigns;

import java.util.List;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeleportSignsUpdateEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private List<TeleportSign> signs;
	private boolean cancelled;

	public TeleportSignsUpdateEvent(List<TeleportSign> signs) {
		this.signs = signs;
	}

	@Override
	public HandlerList getHandlers() {
        return handlers;
    }

	public static HandlerList getHandlerList() {
        return handlers;
    }

	public List<TeleportSign> getSigns() {
		return signs;
	}

	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public boolean isCancelled() {
		return cancelled;
	}
}