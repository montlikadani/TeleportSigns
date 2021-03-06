package hu.montlikadani.TeleportSigns;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitTask;

import hu.montlikadani.TeleportSigns.server.ServerVersion.Version;
import hu.montlikadani.TeleportSigns.sign.TeleportSign;
import hu.montlikadani.TeleportSigns.utils.SignUtil;

public class AnimationTask {

	private final TeleportSigns plugin;

	private final Map<TeleportSigns, BukkitTask> task = new HashMap<>();

	public AnimationTask(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	public Map<TeleportSigns, BukkitTask> getTask() {
		return task;
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
					BlockState state = s.getLocation().getBlock().getState();
					if (!SignUtil.isSign(state)) {
						continue;
					}

					Sign sign = (Sign) state;
					sign.setLine(line, lines[line]);
					sign.update();

					String type = plugin.getConfigData().getBackgroundType().toLowerCase();
					if (!type.equals("none") && SignUtil.isWallSign(sign.getType())) {
						if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
							if (type.equals("wool")) {
								s.updateBackground(Material.LIGHT_BLUE_WOOL);
							} else if (type.equals("glass")) {
								s.updateBackground(Material.LIGHT_BLUE_STAINED_GLASS);
							} else if (type.equals("clay") || type.equals("terracotta")) {
								s.updateBackground(Material.LIGHT_BLUE_TERRACOTTA);
							}
						} else {
							if (type.equals("wool")) {
								s.updateBackground(Material.getMaterial("WOOL"), 3);
							} else if (type.equals("glass")) {
								s.updateBackground(Material.getMaterial("STAINED_GLASS"), 3);
							} else if (type.equals("clay") || type.equals("terracotta")) {
								s.updateBackground(Material.getMaterial("STAINED_CLAY"), 3);
							}
						}
					}
				}

				line++;
			}
		}, 0L, 10L));

		Bukkit.getScheduler().runTaskLater(plugin, this::runSecondAnimation, 5 * 20L);
	}

	private void runSecondAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			BlockState state = s.getLocation().getBlock().getState();
			if (!SignUtil.isSign(state)) {
				continue;
			}

			Sign sign = (Sign) state;
			sign.setLine(0, "---------------");
			sign.setLine(1, "TeleportSigns");
			sign.setLine(2, "\u00a7lVersion " + plugin.getDescription().getVersion());
			sign.setLine(3, "---------------");
			sign.update();
		}

		Bukkit.getScheduler().runTaskLater(plugin, this::runThirdAnimation, 20L);
	}

	private void runThirdAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			BlockState state = s.getLocation().getBlock().getState();
			if (!SignUtil.isSign(state)) {
				continue;
			}

			Sign sign = (Sign) state;
			sign.setLine(0, "---------------");
			sign.setLine(1, "Loading");
			sign.setLine(2, "Servers");
			sign.setLine(3, "---------------");
			sign.update();
		}

		stopAnimation();

		task.put(plugin, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;

			@Override
			public void run() {
				if (pnt >= 3) return;

				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					BlockState state = s.getLocation().getBlock().getState();
					if (!SignUtil.isSign(state)) {
						continue;
					}

					Sign sign = (Sign) state;
					sign.setLine(2, sign.getLine(2) + ".");
					sign.update();
				}
				pnt++;
			}
		}, 5L, 5L));

		Bukkit.getScheduler().runTaskLater(plugin, this::runFourthAnimation, 20L);
	}

	private void runFourthAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			BlockState state = s.getLocation().getBlock().getState();
			if (!SignUtil.isSign(state)) {
				continue;
			}

			Sign sign = (Sign) state;
			sign.setLine(0, "---------------");
			sign.setLine(1, "Loading");
			sign.setLine(2, "Layouts");
			sign.setLine(3, "---------------");
			sign.update();
		}

		stopAnimation();

		task.put(plugin, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;

			@Override
			public void run() {
				if (pnt >= 3) return;

				for (TeleportSign s : plugin.getConfigData().getSigns()) {
					BlockState state = s.getLocation().getBlock().getState();
					if (!SignUtil.isSign(state)) {
						continue;
					}

					Sign sign = (Sign) state;
					sign.setLine(2, sign.getLine(2) + ".");
					sign.update();
				}
				pnt++;
			}
		}, 5L, 5L));

		Bukkit.getScheduler().runTaskLater(plugin, this::runFifthAnimation, 20L);
	}

	private void runFifthAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			BlockState state = s.getLocation().getBlock().getState();
			if (!SignUtil.isSign(state)) {
				continue;
			}

			Sign sign = (Sign) state;
			sign.setLine(0, "---------------");
			sign.setLine(1, "Please wait");
			sign.setLine(2, "Getting data");
			sign.setLine(3, "---------------");
			sign.update();
		}

		stopAnimation();

		task.put(plugin, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int pnt = 0;

			@Override
			public void run() {
				if (pnt >= 3) {
					for (TeleportSign s : plugin.getConfigData().getSigns()) {
						BlockState state = s.getLocation().getBlock().getState();
						if (!SignUtil.isSign(state)) {
							continue;
						}

						Sign sign = (Sign) state;
						if (sign.getLine(2).contains("Getting data")) {
							sign.setLine(2, "Getting data");
							sign.update();
						} else {
							stopAnimation();
						}
					}
					pnt = 0;
				} else {
					for (TeleportSign s : plugin.getConfigData().getSigns()) {
						BlockState state = s.getLocation().getBlock().getState();
						if (!SignUtil.isSign(state)) {
							continue;
						}

						Sign sign = (Sign) state;
						if (sign.getLine(2).contains("Getting data")) {
							sign.setLine(2, sign.getLine(2) + ".");
							sign.update();
						} else {
							stopAnimation();
						}
					}
					pnt++;
				}
			}
		}, 5L, 5L));
	}

	public void resetAnimation() {
		for (TeleportSign s : plugin.getConfigData().getSigns()) {
			BlockState state = s.getLocation().getBlock().getState();
			if (!SignUtil.isSign(state)) {
				continue;
			}

			Sign sign = (Sign) state;
			SignUtil.signLines(sign);
			sign.update();
		}
	}

	public void stopAnimation() {
		if (task.containsKey(plugin)) {
			task.get(plugin).cancel();
			task.remove(plugin);
		}
	}
}