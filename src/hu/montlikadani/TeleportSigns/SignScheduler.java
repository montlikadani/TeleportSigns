package hu.montlikadani.TeleportSigns;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.TeleportSigns.api.TeleportSignsUpdateEvent;

public class SignScheduler implements Runnable, Listener {

	private final TeleportSigns plugin;

	SignScheduler(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		List<TeleportSign> signs = plugin.getConfigData().getSigns();
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