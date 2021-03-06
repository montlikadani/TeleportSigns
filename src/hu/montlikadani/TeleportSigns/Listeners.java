package hu.montlikadani.TeleportSigns;

import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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

import hu.montlikadani.TeleportSigns.api.TeleportSignsBreakEvent;
import hu.montlikadani.TeleportSigns.api.TeleportSignsInteractEvent;
import hu.montlikadani.TeleportSigns.api.TeleportSignsPlaceEvent;
import hu.montlikadani.TeleportSigns.server.ServerInfo;
import hu.montlikadani.TeleportSigns.sign.SignCooldown;
import hu.montlikadani.TeleportSigns.sign.SignLayout;
import hu.montlikadani.TeleportSigns.sign.TeleportSign;

public class Listeners implements Listener {

	private TeleportSigns plugin;

	private final Map<Player, SignCooldown> cooldowns = new HashMap<>();

	Listeners(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	public Map<Player, SignCooldown> getCooldowns() {
		return cooldowns;
	}

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
		Block b = event.getBlock();
		if (!(b.getState() instanceof Sign)) {
			return;
		}

		if (!plugin.getConfigData().containsSign(b)) {
			return;
		}

		Player p = event.getPlayer();

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

		for (Iterator<Entry<Player, SignCooldown>> it = cooldowns.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().getSign().getLocation().equals(b.getLocation())) {
				it.remove();
			}
		}

		plugin.getConfigData().removeSign(b.getLocation());
		sendMsg(p, plugin.getMsg("sign-destroyed"));
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
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

		if (!plugin.getConfigData().containsSign(b)) {
			return;
		}

		Player p = event.getPlayer();

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
			sendMsg(p, layout.parseCantTeleportMessage(server));
			return;
		}

		if (!server.isOnline()) {
			sendMsg(p, layout.parseOfflineMessage(server));
			return;
		}

		if (cooldowns.containsKey(p) && cooldowns.get(p).hasCooldown()) {
			sendMsg(p, layout.parseCooldownMessage(cooldowns.get(p).getCooldown()));
			return;
		}

		cooldowns.remove(p);
		cooldowns.put(p, new SignCooldown(p, plugin.getConfigData().getCooldown(), event.getSign()));

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
}