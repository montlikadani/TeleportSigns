package hu.montlikadani.TeleportSigns;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import hu.montlikadani.TeleportSigns.Permissions.Perm;
import hu.montlikadani.TeleportSigns.api.TeleportSignsBreakEvent;
import hu.montlikadani.TeleportSigns.api.TeleportSignsInteractEvent;
import hu.montlikadani.TeleportSigns.api.TeleportSignsPlaceEvent;

import static hu.montlikadani.TeleportSigns.Messager.defaults;
import static hu.montlikadani.TeleportSigns.Messager.sendMsg;

public class Listeners implements Listener {

	private TeleportSigns plugin;

	Listeners(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	private HashMap<Player, Long> cooldown = new HashMap<>();

	@EventHandler(ignoreCancelled = true)
	public void onCreateTeleportSign(SignChangeEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();

		if (event.getLine(0).contains("[tsigns]") || event.getLine(0).contains("[teleportsigns]")) {
			if (!p.hasPermission(Perm.CREATE.getPerm())) {
				sendMsg(p, defaults(plugin.getMsg("no-create-sign", "%perm%", Perm.CREATE.getPerm())));
				return;
			}

			String sname = event.getLine(1);
			String lname = event.getLine(2);
			if (lname.equalsIgnoreCase(""))
				lname = "default";

			Location location = b.getLocation();
			ServerInfo server = plugin.getConfigData().getServer(sname);
			SignLayout layout = plugin.getConfigData().getLayout(lname);
			if (server != null) {
				if (layout != null) {
					TeleportSignsPlaceEvent e = new TeleportSignsPlaceEvent(p, b, server, layout);
					plugin.callEvent(e);

					plugin.getConfigData().addSign(location, server, layout);
					sendMsg(p, defaults(plugin.getMsg("sign-created", "%server%", sname, "%layout%", lname)));
				} else {
					sendMsg(p, defaults(plugin.getMsg("unknown-layout", "%layout%", lname)));
				}
			} else {
				sendMsg(p, defaults(plugin.getMsg("unknown-server", "%server%", sname)));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onRemoveTeleportSign(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();

		if (b.getState() instanceof Sign) {
			if (plugin.getConfigData().containsSign(b)) {
				if (p.hasPermission(Perm.DESTROY.getPerm())) {
					TeleportSignsBreakEvent e = new TeleportSignsBreakEvent(p, b,
							plugin.getConfigData().getSignFromLocation(b.getLocation()));
					plugin.callEvent(e);
					if (e.isCancelled()) {
						return;
					}

					plugin.getConfigData().removeSign(b.getLocation());
					sendMsg(p, defaults(plugin.getMsg("sign-destroyed")));
				} else {
					sendMsg(p, defaults(plugin.getMsg("no-sign-destroy", "%perm%", Perm.DESTROY.getPerm())));
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign) {
				if (plugin.getConfigData().getBlocks().contains(event.getClickedBlock())) {
					if (p.hasPermission(Perm.USE.getPerm())) {
						for (TeleportSign sign : plugin.getConfigData().getSigns()) {
							if (sign != null && !sign.isBroken() && sign.getLocation().equals(event.getClickedBlock().getLocation())) {
								ServerInfo server = sign.getServer();
								if (server != null) {
									if (p.isSneaking() && !plugin.getConfigData().isIgnoringSneak()) {
										sendMsg(p, defaults(plugin.getMsg("player-sneaking")));
										return;
									}

									TeleportSignsInteractEvent e = new TeleportSignsInteractEvent(p, sign, server);
									plugin.callEvent(e);
									event.setCancelled(true);
								}
							}
						}
					} else {
						sendMsg(p, defaults(plugin.getMsg("no-permission", "%perm%", Perm.USE.getPerm())));
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTeleportSignInteract(TeleportSignsInteractEvent event) {
		Player p = event.getPlayer();
		ServerInfo server = event.getServer();
		SignLayout layout = event.getSign().getLayout();

		if (layout.isTeleport()) {
			if (server.isOnline()) {
				if (!hasCooldown(p)) {
					addCooldown(p);

					if (server.getPlayerCount() == server.getMaxPlayers()) {
						String msg = layout.getFullMessage();
						if (msg != null && !msg.equals("")) {
							sendMsg(p, defaults(layout.parseFullMessage(server)));
						}

						event.setCancelled(true);
						return;
					}

					server.teleportPlayer(p);
				} else {
					String msg = layout.getCooldownMessage();
					if (msg != null && !msg.equals("")) {
						sendMsg(p, layout.parseCooldownMessage(getCooldown(p)));
					}
				}
			} else {
				String msg = layout.getOfflineMessage();
				if (msg != null && !msg.equals("")) {
					sendMsg(p, layout.parseOfflineMessage(server));
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (plugin.getMainConf().getBoolean("check-update") && p.isOp()) {
			p.sendMessage(plugin.checkVersion("player"));
		}
	}

	private boolean hasCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() > 0) {
			if (!player.hasPermission(Perm.NOCOOLDOWN.getPerm())) {
				if (cooldown.containsKey(player)) {
					long time = System.currentTimeMillis();
					long cooldown = this.cooldown.get(player);
					long result = (time - cooldown);

					if (result >= plugin.getConfigData().getCooldown()) {
						this.cooldown.remove(player);
						return false;
					}
					return true;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	private void addCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() > 0) {
			if (!player.hasPermission(Perm.NOCOOLDOWN.getPerm())) {
				if (!cooldown.containsKey(player)) {
					cooldown.put(player, System.currentTimeMillis());
				}
			}
		}
	}

	private int getCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() > 0) {
			if (!player.hasPermission(Perm.NOCOOLDOWN.getPerm())) {
				if (cooldown.containsKey(player)) {
					long time = System.currentTimeMillis();
					long cooldown = this.cooldown.get(player);
					long result = (cooldown - time);
					int wait = Integer.parseInt(result / 1000 + "");
					int towait = (int) ((plugin.getConfigData().getCooldown() / 1000) + wait);

					return towait;
				}
			}
		}
		return 0;
	}
}