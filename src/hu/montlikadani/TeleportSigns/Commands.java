package hu.montlikadani.TeleportSigns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;

public class Commands implements CommandExecutor, TabCompleter {

	private TeleportSigns plugin;

	public Commands(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		try {
			if (cmd.getName().equalsIgnoreCase("teleportsigns")) {
				if (args.length == 0) {
					if (!sender.hasPermission(Permissions.PINFO)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.plugininfo")));
						return true;
					}
					sender.sendMessage("§6§l[§2§lTeleport§e§lSigns§b§l Info§e§l]");
					sender.sendMessage("§5Version:§a " + plugin.getDescription().getVersion());
					sender.sendMessage("§5Author, created by:§a montlikadani");
					sender.sendMessage("§5Commands:§8 /§7" + commandLabel + "§a help");
					sender.sendMessage("§4If you find a bug, send issue here:§e §nhttps://github.com/montlikadani/TeleportSigns/issues");
					return true;
				} else if (args[0].equalsIgnoreCase("help")) {
					if (!sender.hasPermission(Permissions.HELP)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.help")));
						return true;
					}
					if (args.length > 1) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-command").replace("%command%", commandLabel)));
						return true;
					}
					for (String msg : plugin.messages.getStringList("chat-messages")) {
						sender.sendMessage(plugin.colorMsg(msg.replace("%command%", commandLabel).replace("%prefix%", plugin.messages.getString("prefix"))));
					}
					return true;
				} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
					if (!sender.hasPermission(Permissions.RELOAD)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.reload")));
						return true;
					}
					if (args.length > 1) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-command").replace("%command%", commandLabel)));
						return true;
					}
					plugin.reload();
					sender.sendMessage(plugin.defaults(plugin.messages.getString("reload-config")));
					return true;
				} else if (args[0].equalsIgnoreCase("disable")) {
					if (!(sender.hasPermission(Permissions.PDISABLE) && sender.isOp())) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.plugindisable + op")));
						return true;
					}
					if (args.length > 1) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-command").replace("%command%", commandLabel)));
						return true;
					}
					plugin.getServer().getPluginManager().disablePlugin(plugin);
					return true;
				} else if (args[0].equalsIgnoreCase("listlayouts")) {
					if (!sender.hasPermission(Permissions.LISTLAYOUT)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.listlayouts")));
						return true;
					}
					if (args.length > 1) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-command").replace("%command%", commandLabel)));
						return true;
					}
					Set<String> layouts = plugin.getConfigData().getConfig(ConfigType.LAYOUTS).getConfigurationSection("layouts").getKeys(false);
					if (layouts.isEmpty()) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-layouts-found")));
						return true;
					}
					sender.sendMessage(plugin.defaults(plugin.messages.getString("list-layouts").replace("%layouts%", layouts.toString().replace("[", "").replace("]", ""))));
					return true;
				} else if (args[0].equalsIgnoreCase("listservers")) {
					if (!sender.hasPermission(Permissions.LISTSERVER)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.listservers")));
						return true;
					}
					if (args.length > 1) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-command").replace("%command%", commandLabel)));
						return true;
					}
					Set<String> servers = plugin.getMainConf().getConfigurationSection("servers").getKeys(false);
					if (servers.isEmpty()) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-servers-found")));
						return true;
					}
					sender.sendMessage(plugin.defaults(plugin.messages.getString("list-servers").replace("%servers%", servers.toString().replace("[", "").replace("]", ""))));
					return true;
				} else if (args[0].equalsIgnoreCase("connect")) {
					if (!sender.hasPermission(Permissions.CONNECT)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.connect")));
						return true;
					}
					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-console").replace("%command%", commandLabel).replace("%args%", args[0])));
						return true;
					}
					if (args.length > 2) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-command").replace("%command%", commandLabel)));
						return true;
					}
					if (args.length != 2) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("connect-usage").replace("%command%", commandLabel)));
						return true;
					}
					final String serverGroup = args[1];
					Player p = (Player) sender;
					if (!plugin.getConfigData().getServer(serverGroup).isOnline()) {
						p.sendMessage(plugin.defaults(plugin.messages.getString("server-offline").replace("%server%", serverGroup)));
						return true;
					}
					if (plugin.getConfigData().getServer(serverGroup) != null) {
						ServerInfo server = plugin.getConfigData().getServer(serverGroup);
						server.teleportPlayer(p);
					} else {
						p.sendMessage(plugin.defaults(plugin.messages.getString("server-group-not-found").replace("%server%", serverGroup)));
						return true;
					}
				} else if (args[0].equalsIgnoreCase("editsign")) {
					if (!sender.hasPermission(Permissions.EDITSIGN)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.editsign")));
						return true;
					}
					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-console").replace("%command%", commandLabel).replace("%args%", args[0])));
						return true;
					}
					if (args.length != 3) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("editsign.usage").replace("%command%", commandLabel)));
						return true;
					}
					if (args.length > 3) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-command").replace("%command%", commandLabel)));
						return true;
					}
					Player p = (Player) sender;
					HashSet<Material> mat = null;
					Block b = p.getTargetBlock(mat, 100);
					if ((b.getState() instanceof Sign)) {
						String sName = args[1];
						String lName = args[2];
						ServerInfo server = plugin.getConfigData().getServer(sName);
						if (server == null) {
							p.sendMessage(plugin.defaults(plugin.messages.getString("unknown-server").replace("%server%", sName)));
							return true;
						}
						SignLayout layout = plugin.getConfigData().getLayout(lName);
						if (layout == null) {
							p.sendMessage(plugin.defaults(plugin.messages.getString("unknown-layout").replace("%layout%", lName)));
							return true;
						}
						plugin.getConfigData().addSign(b.getLocation(), server, layout);
						p.sendMessage(plugin.defaults(plugin.messages.getString("editsign.created")));
					} else {
						p.sendMessage(plugin.defaults(plugin.messages.getString("editsign.look-at-sign")));
						return true;
					}
				} else {
					sender.sendMessage(plugin.defaults(plugin.messages.getString("unknown-sub-command").replace("%subcmd%", args[0])));
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			plugin.throwMsg();
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender.hasPermission(Permissions.TABCOMP)) {
			List<String> cmds = new java.util.ArrayList<>();
			if (cmd.getName().equalsIgnoreCase("teleportsigns") || cmd.getName().equalsIgnoreCase("ts")) {
				if (args.length < 2) {
					for (String com : new String[] { "help", "reload", "disable", "listlayouts", "listservers", "connect", "editsign" }) {
						cmds.add(com);
					}
				}
				if (args.length < 3 && args[0].equalsIgnoreCase("connect")) {
					for (ServerInfo com : plugin.getConfigData().getServers()) {
						cmds.add(com.getName());
					}
				}
			}
			return cmds;
		}
		return null;
	}
}