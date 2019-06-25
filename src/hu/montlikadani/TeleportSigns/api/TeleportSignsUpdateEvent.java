package hu.montlikadani.TeleportSigns.api;

import java.util.List;

import org.bukkit.event.Cancellable;

import hu.montlikadani.TeleportSigns.TeleportSign;

public class TeleportSignsUpdateEvent extends BaseEvent implements Cancellable {
	private List<TeleportSign> signs;
	private boolean cancelled = false;

	public TeleportSignsUpdateEvent(List<TeleportSign> signs) {
		this.signs = signs;
	}

	public List<TeleportSign> getSigns() {
		return signs;
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
