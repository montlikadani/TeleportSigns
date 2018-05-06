package hu.montlikadani.TeleportSigns;

import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.Bukkit;
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

public class Listeners implements Listener {

	private TeleportSigns plugin;

	public Listeners(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	private HashMap<Player, Long> cooldown = new HashMap<>();

	@EventHandler(ignoreCancelled = true)
	public void onCreateTeleportSign(SignChangeEvent event) {
		if (!event.isCancelled()) {
			Player p = event.getPlayer();
			Block b = event.getBlock();
			if (event.getLine(0).equalsIgnoreCase("[tsigns]") || event.getLine(0).equalsIgnoreCase("[teleportsigns]")) {
				if (p.hasPermission(Permissions.CREATE)) {
					String sname = event.getLine(1);
					String lname = event.getLine(2);
					if (lname.equalsIgnoreCase("")) {
						lname = "default";
					}

					Location location = b.getLocation();
					ServerInfo server = plugin.getConfigData().getServer(sname);
					SignLayout layout = plugin.getConfigData().getLayout(lname);
					if (server != null) {
						if (layout != null) {
							plugin.getConfigData().addSign(location, server, layout);
							p.sendMessage(plugin.defaults(plugin.messages.getString("sign-created").replace("%server%", sname).replace("%layout%", lname)));
						} else {
							p.sendMessage(plugin.defaults(plugin.messages.getString("unknown-layout").replace("%layout%", lname)));
							if (plugin.getConfig().getBoolean("sign-break-drop")) {
								b.breakNaturally();
							}
						}
					} else {
						p.sendMessage(plugin.defaults(plugin.messages.getString("unknown-server").replace("%server%", sname)));
						if (plugin.getConfig().getBoolean("sign-break-drop")) {
							b.breakNaturally();
						}
					}
				} else {
					p.sendMessage(plugin.defaults(plugin.messages.getString("no-create-sign").replace("%perm%", "teleportsigns.create")));
					if (plugin.getConfig().getBoolean("sign-break-drop")) {
						b.breakNaturally();
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onRemoveTeleportSign(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			Player p = event.getPlayer();
			Block b = event.getBlock();
			if (b.getState() instanceof Sign) {
				if (plugin.getConfigData().containsSign(b)) {
					if (p.hasPermission(Permissions.DESTROY)) {
						plugin.getConfigData().removeSign(b.getLocation());
						p.sendMessage(plugin.defaults(plugin.messages.getString("sign-destroyed")));
					} else {
						p.sendMessage(plugin.defaults(plugin.messages.getString("no-sign-destroy").replace("%perm%", "teleportsigns.destroy")));
						event.setCancelled(true);
					}
				}
			}
		} else {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			Player p = event.getPlayer();
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (event.getClickedBlock().getState() instanceof Sign) {
					if (plugin.getConfigData().getBlocks().contains(event.getClickedBlock())) {
						if (p.hasPermission(Permissions.USE)) {
							for (TeleportSign sign : plugin.getConfigData().getSigns()) {
								if (sign != null && !sign.isBroken() && sign.getLocation().equals(event.getClickedBlock().getLocation())) {
									ServerInfo server = sign.getServer();
									if (server != null) {
										TeleportSignsInteractEvent e = new TeleportSignsInteractEvent(p, sign, server);
										Bukkit.getPluginManager().callEvent(e);
										event.setCancelled(true);
									}
								}
							}
						} else {
							p.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.use")));
						}
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTeleportSignInteract(TeleportSignsInteractEvent event) {
		if (!event.isCancelled()) {
			Player p = event.getPlayer();
			if (event.getSign().getLayout().isTeleport()) {
				if (event.getServer().isOnline()) {
					if (!hasCooldown(p)) {
						addCooldown(p);
						event.getServer().teleportPlayer(p);
					} else {
						p.sendMessage(event.getSign().getLayout().parseCooldownMessage(getCooldown(p)));
					}
				} else {
					p.sendMessage(event.getSign().getLayout().parseOfflineMessage(event.getServer()));
				}
			}
		}
	}

	private boolean hasCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() != 0) {
			if (!player.hasPermission(Permissions.NOCOOLDOWN)) {
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
		if (plugin.getConfigData().getCooldown() != 0) {
			if (!player.hasPermission(Permissions.NOCOOLDOWN)) {
				if (!cooldown.containsKey(player)) {
					cooldown.put(player, System.currentTimeMillis());
				}
			}
		}
	}

	private int getCooldown(Player player) {
		if (plugin.getConfigData().getCooldown() != 0) {
			if (!player.hasPermission(Permissions.NOCOOLDOWN)) {
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

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (plugin.getConfig().getBoolean("check-update")) {
			if (p.isOp()) {
				if (p.hasPermission("teleportsigns.checkupdate")) {
					TeleportSigns plu = TeleportSigns.getPlugin(TeleportSigns.class);
					String[] nVersion;
					String[] cVersion;
					String lineWithVersion;
					URL githubUrl;
					try {
						githubUrl = new URL("https://raw.githubusercontent.com/montlikadani/TeleportSigns/master/plugin.yml");
						lineWithVersion = "";
						Scanner websiteScanner = new Scanner(githubUrl.openStream());
						while (websiteScanner.hasNextLine()) {
							String line = websiteScanner.nextLine();
							if (line.toLowerCase().contains("version")) {
								lineWithVersion = line;
								break;
							}
						}
						String versionString = lineWithVersion.split(": ")[1];
						nVersion = versionString.split("\\.");
						double newestVersionNumber = Double.parseDouble(nVersion[0] + "." + nVersion[1]);
						cVersion = plu.getDescription().getVersion().split("\\.");
						double currentVersionNumber = Double.parseDouble(cVersion[0] + "." + cVersion[1]);
						if (newestVersionNumber > currentVersionNumber) {
							p.sendMessage(plugin.colorMsg("&8&m&l--------------------------------------------------\n" + 
							plugin.messages.getString("prefix") + "&a A new update is available!&4 Version:&7 " + versionString + 
							"\n&6Download:&c &nhttps://www.spigotmc.org/resources/teleport-signs.37446/" + 
							"\n&8&m&l--------------------------------------------------"));
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						plugin.logConsole(Level.WARNING, "Failed to compare versions. " + ex + " Please report it here:\nhttps://github.com/montlikadani/TeleportSigns/issues");
					}
				}
			}
		}
	}
}
