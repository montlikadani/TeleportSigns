package hu.montlikadani.TeleportSigns;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitTask;

public class AnimationTask {

	private TeleportSigns plugin;

	public AnimationTask(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	private BukkitTask task;

	public void startAnimation() {
		this.runFirstAnimation();
	}

	private void runFirstAnimation() {
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int line = 0;
			String[] lines = { "---------------", "TeleportSigns", "Initialize...", "---------------" };
			@Override
			public void run() {
				if (line >= 4) return;
				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					if (s.getLocation().getBlock().getState() instanceof Sign) {
						Sign sign = (Sign)s.getLocation().getBlock().getState();
						sign.setLine(line, lines[line]);
						sign.update(true);
						if (plugin.getConfig().getBoolean("options.background-enable")) {
							if (sign.getType().equals(Material.WALL_SIGN)) {
								if (plugin.getConfig().getString("options.background").equalsIgnoreCase("wool")) {
									s.updateBackground(Material.WOOL, 3);
								} else if (plugin.getConfig().getString("options.background").equalsIgnoreCase("glass")) {
									s.updateBackground(Material.STAINED_GLASS, 11);
								} else if (plugin.getConfig().getString("options.background").equalsIgnoreCase("clay")) {
									s.updateBackground(Material.STAINED_CLAY, 3);
								}
							}
						}
					}
				}
				line++;
			}
		}, 0L, 10L);

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				runSecondAnimation();
			}
		}, 5*20L);
	}

	private void runSecondAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign)s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "TeleportSigns");
				sign.setLine(2, "§lVersion ${version}");
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				runThirdAnimation();
			}
		}, 20L);
	}

	private void runThirdAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign)s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "Loading");
				sign.setLine(2, "Servers");
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		stopAnimation();
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;
			@Override
			public void run() {
				if (pnt >= 3) return;
				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					if (s.getLocation().getBlock().getState() instanceof Sign) {
						Sign sign = (Sign)s.getLocation().getBlock().getState();
						sign.setLine(2, sign.getLine(2) + ".");
						sign.update(true);
					}
				}
				pnt++;
			}
		}, 5L, 5L);

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				runFourthAnimation();
			}
		}, 20L);
	}

	private void runFourthAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign)s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "Loading");
				sign.setLine(2, "Layouts");
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		stopAnimation();
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;
			@Override
			public void run() {
				if (pnt >= 3) return;
				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					if (s.getLocation().getBlock().getState() instanceof Sign) {
						Sign sign = (Sign)s.getLocation().getBlock().getState();
						sign.setLine(2, sign.getLine(2) + ".");
						sign.update(true);
					}
				}
				pnt++;
			}
		}, 5L, 5L);

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				runFifthAnimation();
			}
		}, 20L);
	}

	private void runFifthAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign)s.getLocation().getBlock().getState();
				sign.setLine(0, "---------------");
				sign.setLine(1, "Please wait");
				sign.setLine(2, "Getting data");
				sign.setLine(3, "---------------");
				sign.update(true);
			}
		}

		stopAnimation();
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;
			@Override
			public void run() {
				if (pnt >= 3) {
					for (TeleportSign s : plugin.getConfigData().getSigns()) {
						if (s.getLocation().getBlock().getState() instanceof Sign) {
							Sign sign = (Sign)s.getLocation().getBlock().getState();
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
							Sign sign = (Sign)s.getLocation().getBlock().getState();
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
		}, 5L, 5L);
	}

	public void resetAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			if (s.getLocation().getBlock().getState() instanceof Sign) {
				Sign sign = (Sign)s.getLocation().getBlock().getState();
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
			Bukkit.getScheduler().cancelTask(task.getTaskId());
		}
	}

	public BukkitTask getTask() {
		return task;
	}
}
