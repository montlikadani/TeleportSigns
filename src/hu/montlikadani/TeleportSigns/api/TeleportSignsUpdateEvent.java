package hu.montlikadani.TeleportSigns.api;

import java.util.Set;

import org.bukkit.event.Cancellable;

import hu.montlikadani.TeleportSigns.sign.TeleportSign;

public class TeleportSignsUpdateEvent extends BaseEvent implements Cancellable {

	private Set<TeleportSign> signs;
	private boolean cancelled = false;

	public TeleportSignsUpdateEvent(Set<TeleportSign> signs) {
		this.signs = signs;
	}

	public Set<TeleportSign> getSigns() {
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
