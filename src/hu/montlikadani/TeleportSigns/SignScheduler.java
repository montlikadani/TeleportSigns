package hu.montlikadani.TeleportSigns;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SignScheduler implements Runnable, Listener {
	private final TeleportSigns plugin;

	public SignScheduler(TeleportSigns plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void run() {
		final List<TeleportSign> signs = plugin.getConfigData().getSigns();
		TeleportSignsUpdateEvent event = new TeleportSignsUpdateEvent(signs);
		Bukkit.getPluginManager().callEvent(event);
		Bukkit.getScheduler().runTaskLater(plugin, this, plugin.getConfigData().getUpdateInterval());
	}

	@EventHandler
	public void onEvent(TeleportSignsUpdateEvent event) {
		if (!event.isCancelled()) {
			Iterator<TeleportSign> list = event.getSigns().iterator();
			while (list.hasNext()) {
				TeleportSign sign = list.next();
				if (sign != null) {
					sign.updateSign();
				}
			}
		}
	}
}