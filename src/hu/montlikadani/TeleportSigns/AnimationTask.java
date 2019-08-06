package hu.montlikadani.TeleportSigns;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitTask;

public class AnimationTask {

	private final TeleportSigns plugin;

	private Map<TeleportSigns, BukkitTask> task = new HashMap<>();

	public AnimationTask(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	public void startAnimation() {
		runFirstAnimation();
	}

	private synchronized void runFirstAnimation() {
		task.put(plugin, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int line = 0;
			String[] lines = { "---------------", "TeleportSigns", "Initialize...", "---------------" };

			@Override
			public void run() {
				if (line >= 4) return;

				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					if (s.getLocation().getBlock().getState() instanceof Sign) {
						Sign sign = (Sign) s.getLocation().getBlock().getState();
						sign.setLine(line, lines[line]);
						sign.update(true);

						if (plugin.getMainConf().getBoolean("options.background.enable")) {
							if (Bukkit.getVersion().contains("1.14")) {
								if (Tag.WALL_SIGNS.isTagged(s.getLocation().getBlock().getType())) {
									if (plugin.getBackgroundType().equals("wool")) {
										s.updateBackground(Material.LIGHT_BLUE_WOOL);
									} else if (plugin.getBackgroundType().equals("glass")) {
										s.updateBackground(Material.LIGHT_BLUE_STAINED_GLASS);
									} else if (plugin.getBackgroundType().equals("clay")
											|| plugin.getBackgroundType().equals("terracotta")) {
										s.updateBackground(Material.LIGHT_BLUE_TERRACOTTA);
									}
								}
							} else if (s.getLocation().getBlock().getType() == Material.getMaterial("WALL_SIGN")) {
								if (Bukkit.getVersion().contains("1.13")) {
									if (plugin.getBackgroundType().equals("wool")) {
										s.updateBackground(Material.LIGHT_BLUE_WOOL);
									} else if (plugin.getBackgroundType().equals("glass")) {
										s.updateBackground(Material.LIGHT_BLUE_STAINED_GLASS);
									} else if (plugin.getBackgroundType().equals("clay")
											|| plugin.getBackgroundType().equals("terracotta")) {
										s.updateBackground(Material.LIGHT_BLUE_TERRACOTTA);
									}
								} else {
									if (plugin.getBackgroundType().equals("wool")) {
										s.updateBackground(Material.getMaterial("WOOL"), 3);
									} else if (plugin.getBackgroundType().equals("glass")) {
										s.updateBackground(Material.getMaterial("STAINED_GLASS"), 3);
									} else if (plugin.getBackgroundType().equals("clay")
											|| plugin.getBackgroundType().equals("terracotta")) {
										s.updateBackground(Material.getMaterial("STAINED_CLAY"), 3);
									}
								}
							}
						}
					}
				}
				line++;
			}
		}, 0L, 10L));

		Bukkit.getScheduler().runTaskLater(plugin, this::runSecondAnimation, 5*20L);
	}

	private void runSecondAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign) s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "TeleportSigns");
				sign.setLine(2, "\u00a7lVersion " + plugin.getDescription().getVersion());
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		Bukkit.getScheduler().runTaskLater(plugin, this::runThirdAnimation, 20L);
	}

	private void runThirdAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign) s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "Loading");
				sign.setLine(2, "Servers");
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		stopAnimation();
		task.put(plugin, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;

			@Override
			public void run() {
				if (pnt >= 3) return;

				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					if (s.getLocation().getBlock().getState() instanceof Sign) {
						Sign sign = (Sign) s.getLocation().getBlock().getState();
						sign.setLine(2, sign.getLine(2) + ".");
						sign.update(true);
					}
				}
				pnt++;
			}
		}, 5L, 5L));

		Bukkit.getScheduler().runTaskLater(plugin, this::runFourthAnimation, 20L);
	}

	private void runFourthAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign) s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "Loading");
				sign.setLine(2, "Layouts");
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		stopAnimation();
		task.put(plugin, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;

			@Override
			public void run() {
				if (pnt >= 3) return;

				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					if (s.getLocation().getBlock().getState() instanceof Sign) {
						Sign sign = (Sign) s.getLocation().getBlock().getState();
						sign.setLine(2, sign.getLine(2) + ".");
						sign.update(true);
					}
				}
				pnt++;
			}
		}, 5L, 5L));

		Bukkit.getScheduler().runTaskLater(plugin, this::runFifthAnimation, 20L);
	}

	private void runFifthAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign) s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "Please wait");
				sign.setLine(2, "Getting data");
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		stopAnimation();
		task.put(plugin, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;

			@Override
			public void run() {
				if (pnt >= 3) {
					for (TeleportSign s : plugin.getConfigData().getSigns()) {
						if (s.getLocation().getBlock().getState() instanceof Sign) {
							Sign sign = (Sign) s.getLocation().getBlock().getState();
							if (sign.getLine(2).contains("Getting data")) {
								sign.setLine(2, "Getting data");
								sign.update(true);
							} else {
								stopAnimation();
							}
						}
					}
					pnt = 0;
				} else {
					for (TeleportSign s : plugin.getConfigData().getSigns()) {
						if (s.getLocation().getBlock().getState() instanceof Sign) {
							Sign sign = (Sign) s.getLocation().getBlock().getState();
							if (sign.getLine(2).contains("Getting data")) {
								sign.setLine(2, sign.getLine(2) + ".");
								sign.update(true);
							} else {
								stopAnimation();
							}
						}
					}
					pnt++;
				}
			}
		}, 5L, 5L));
	}

	public void resetAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign) s.getLocation().getBlock().getState();
				sign.setLine(0, "");
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				sign.update(true);
			}
		}
	}

	public void stopAnimation() {
		if (task != null) {
			Bukkit.getScheduler().cancelTask(task.get(plugin).getTaskId());
		}
	}
}