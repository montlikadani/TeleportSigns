package hu.montlikadani.TeleportSigns.sign;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.TeleportSigns.TeleportSigns;
import hu.montlikadani.TeleportSigns.api.TeleportSignsUpdateEvent;

public class SignScheduler implements Runnable, Listener {

	private final TeleportSigns plugin;

	public SignScheduler(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		Set<TeleportSign> signs = plugin.getConfigData().getSigns();
		TeleportSignsUpdateEvent event = new TeleportSignsUpdateEvent(signs);
		plugin.callEvent(event);
		Bukkit.getScheduler().runTaskLater(plugin, this, plugin.getConfigData().getUpdateInterval());
	}

	@EventHandler
	public void onEvent(TeleportSignsUpdateEvent event) {
		if (!event.isCancelled()) {
			for (TeleportSign ts : event.getSigns()) {
				if (ts != null) {
					ts.updateSign();
				}
			}
		}
	}
}