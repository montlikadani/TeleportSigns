package hu.montlikadani.TeleportSigns;

import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import java.util.HashMap;

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

import hu.montlikadani.TeleportSigns.Perm;
import hu.montlikadani.TeleportSigns.api.TeleportSignsBreakEvent;
import hu.montlikadani.TeleportSigns.api.TeleportSignsInteractEvent;
import hu.montlikadani.TeleportSigns.api.TeleportSignsPlaceEvent;

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
				sendMsg(p, plugin.getMsg("no-create-sign", "%perm%", Perm.CREATE.getPerm()));
				return;
			}

			String sname = event.getLine(1);
			ServerInfo server = plugin.getConfigData().getServer(sname);
			if (server == null) {
				sendMsg(p, plugin.getMsg("unknown-server", "%server%", sname));
				return;
			}

			String lname = event.getLine(2);
			if (lname.equalsIgnoreCase(""))
				lname = "default";

			SignLayout layout = plugin.getConfigData().getLayout(lname);
			if (layout == null) {
				sendMsg(p, plugin.getMsg("unknown-layout", "%layout%", lname));
				return;
			}

			TeleportSignsPlaceEvent e = new TeleportSignsPlaceEvent(p, b, server, layout);
			plugin.callEvent(e);

			plugin.getConfigData().addSign(b.getLocation(), server, layout);
			sendMsg(p, plugin.getMsg("sign-created", "%server%", sname, "%layout%", lname));
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBreakTeleportSign(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();
		if (b == null) {
			return;
		}

		if (!(b.getState() instanceof Sign)) {
			return;
		}

		if (!plugin.getConfigData().containsSign(b)) {
			return;
		}

		if (!p.hasPermission(Perm.DESTROY.getPerm())) {
			sendMsg(p, plugin.getMsg("no-sign-destroy", "%perm%", Perm.DESTROY.getPerm()));
			event.setCancelled(true);
			return;
		}

		TeleportSignsBreakEvent e = new TeleportSignsBreakEvent(p, b,
				plugin.getConfigData().getSignFromLocation(b.getLocation()));
		plugin.callEvent(e);
		if (e.isCancelled()) {
			return;
		}

		plugin.getConfigData().removeSign(b.getLocation());
		sendMsg(p, plugin.getMsg("sign-destroyed"));
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Block b = event.getClickedBlock();
		if (b == null) {
			return;
		}

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (!(b.getState() instanceof Sign)) {
			return;
		}

		if (!plugin.getConfigData().getBlocks().contains(b)) {
			return;
		}

		if (!p.hasPermission(Perm.USE.getPerm())) {
			sendMsg(p, plugin.getMsg("no-permission", "%perm%", Perm.USE.getPerm()));
			event.setCancelled(true);
			return;
		}

		for (TeleportSign sign : plugin.getConfigData().getSigns()) {
			if (sign != null && !sign.isBroken() && sign.getLocation().equals(b.getLocation())) {
				ServerInfo server = sign.getServer();
				if (server == null) {
					continue;
				}

				if (p.isSneaking() && !plugin.getConfigData().isIgnoringSneak()) {
					sendMsg(p, plugin.getMsg("player-sneaking"));
					return;
				}

				TeleportSignsInteractEvent e = new TeleportSignsInteractEvent(p, sign, server);
				plugin.callEvent(e);
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTeleportSignInteract(TeleportSignsInteractEvent event) {
		Player p = event.getPlayer();
		ServerInfo server = event.getServer();
		SignLayout layout = event.getSign().getLayout();

		if (!layout.isTeleport()) {
			return;
		}

		if (!server.isOnline()) {
			sendMsg(p, layout.parseOfflineMessage(server));
			return;
		}

		if (hasCooldown(p)) {
			sendMsg(p, layout.parseCooldownMessage(getCooldown(p)));
			return;
		}

		addCooldown(p);

		if (server.getPlayerCount() == server.getMaxPlayers()) {
			sendMsg(p, layout.parseFullMessage(server));
			event.setCancelled(true);
			return;
		}

		server.teleportPlayer(p);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (plugin.getMainConf().getBoolean("check-update") && p.isOp()) {
			p.sendMessage(plugin.checkVersion("player"));
		}
	}

	private boolean hasCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() < 0) {
			return false;
		}

		if (player.hasPermission(Perm.NOCOOLDOWN.getPerm())) {
			return false;
		}

		if (!cooldown.containsKey(player)) {
			return false;
		}

		long time = System.currentTimeMillis();
		long cooldown = this.cooldown.get(player);
		long result = (time - cooldown);

		if (result >= plugin.getConfigData().getCooldown()) {
			this.cooldown.remove(player);
			return false;
		}

		return true;
	}

	private void addCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() > 0 && !player.hasPermission(Perm.NOCOOLDOWN.getPerm())
				&& !cooldown.containsKey(player)) {
			cooldown.put(player, System.currentTimeMillis());
		}
	}

	private int getCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() < 0) {
			return 0;
		}

		if (player.hasPermission(Perm.NOCOOLDOWN.getPerm())) {
			return 0;
		}

		if (!cooldown.containsKey(player)) {
			return 0;
		}

		long time = System.currentTimeMillis();
		long cooldown = this.cooldown.get(player);
		long result = (cooldown - time);
		int wait = (int) (result / 1000);
		int towait = (int) ((plugin.getConfigData().getCooldown() / 1000) + wait);

		return towait;
	}
}