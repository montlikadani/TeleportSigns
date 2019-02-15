package hu.montlikadani.TeleportSigns;

import java.util.ArrayList;
import java.util.Collections;
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
import org.bukkit.util.StringUtil;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;
import hu.montlikadani.TeleportSigns.Permissions.Perm;

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
					if (!sender.hasPermission(Perm.PINFO.getPerm())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.plugininfo")));
						return true;
					}
					sender.sendMessage(plugin.replaceColor("&6&l[&2&lTeleport&e&lSigns&b&l Info&e&l]"));
					sender.sendMessage(plugin.replaceColor("&5Version:&a " + plugin.getDescription().getVersion()));
					sender.sendMessage(plugin.replaceColor("&5Author, created by:&a montlikadani"));
					sender.sendMessage(plugin.replaceColor("&5Commands:&8 /&7" + commandLabel + "&a help"));
					sender.sendMessage(plugin.replaceColor("&4If you find a bug, send issue here:&e &nhttps://github.com/montlikadani/TeleportSigns/issues"));
				} else if (args[0].equalsIgnoreCase("help")) {
					if (!sender.hasPermission(Perm.HELP.getPerm())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.help")));
						return true;
					}
					if (args.length > 1) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}
					for (String msg : plugin.messages.getStringList("chat-messages")) {
						sender.sendMessage(plugin.colorMsg(msg.replace("%command%", commandLabel).replace("%prefix%", plugin.getMsg("prefix"))));
					}
				} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
					if (!sender.hasPermission(Perm.RELOAD.getPerm())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.reload")));
						return true;
					}
					if (args.length > 1) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}
					plugin.reload();
					plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("reload-config")));
				} else if (args[0].equalsIgnoreCase("disable")) {
					if (!(sender.hasPermission(Perm.PDISABLE.getPerm()) && sender.isOp())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.disable + op")));
						return true;
					}
					if (args.length > 1) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}
					plugin.getServer().getPluginManager().disablePlugin(plugin);
				} else if (args[0].equalsIgnoreCase("listlayouts")) {
					if (!sender.hasPermission(Perm.LISTLAYOUT.getPerm())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.listlayouts")));
						return true;
					}
					if (args.length > 1) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}
					Set<String> layouts = plugin.getConfigData().getConfig(ConfigType.LAYOUTS).getConfigurationSection("layouts").getKeys(false);
					if (layouts == null || layouts.isEmpty()) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-layouts-found")));
						return true;
					}
					plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("list-layouts", "%layouts%", layouts.toString().replace("[", "").replace("]", ""))));
				} else if (args[0].equalsIgnoreCase("listservers")) {
					if (!sender.hasPermission(Perm.LISTSERVER.getPerm())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.listservers")));
						return true;
					}
					if (args.length > 1) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}
					Set<String> servers = plugin.getMainConf().getConfigurationSection("servers").getKeys(false);
					if (servers == null || servers.isEmpty()) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-servers-found")));
						return true;
					}
					plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("list-servers", "%servers%", servers.toString().replace("[", "").replace("]", ""))));
				} else if (args[0].equalsIgnoreCase("connect")) {
					if (!sender.hasPermission(Perm.CONNECT.getPerm())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.connect")));
						return true;
					}
					if (!(sender instanceof Player)) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-console", "%command%", commandLabel, "%args%", args[0])));
						return true;
					}
					if (args.length > 2) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}
					if (args.length != 2) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("connect-usage", "%command%", commandLabel)));
						return true;
					}
					final String serverGroup = args[1];
					Player p = (Player) sender;
					if (plugin.getConfigData().getServer(serverGroup) != null) {
						ServerInfo server = plugin.getConfigData().getServer(serverGroup);
						if (!server.isOnline()) {
							plugin.sendMsg(p, plugin.defaults(plugin.getMsg("server-offline", "%server%", serverGroup)));
							return true;
						}
						server.teleportPlayer(p);
					} else {
						plugin.sendMsg(p, plugin.defaults(plugin.getMsg("server-group-not-found", "%server%", serverGroup)));
					}
				} else if (args[0].equalsIgnoreCase("editsign")) {
					if (!sender.hasPermission(Perm.EDITSIGN.getPerm())) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-permission", "%perm%", "teleportsigns.editsign")));
						return true;
					}
					if (!(sender instanceof Player)) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("no-console", "%command%", commandLabel, "%args%", args[0])));
						return true;
					}
					if (args.length != 3) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("editsign.usage", "%command%", commandLabel)));
						return true;
					}
					if (args.length > 3) {
						plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}
					Player p = (Player) sender;
					HashSet<Material> mat = null;
					Block b = p.getTargetBlock(mat, 100);
					if (b.getState() instanceof Sign) {
						String sName = args[1];
						String lName = args[2];
						ServerInfo server = plugin.getConfigData().getServer(sName);
						if (server == null) {
							plugin.sendMsg(p, plugin.defaults(plugin.getMsg("unknown-server", "%server%", sName)));
							return true;
						}
						SignLayout layout = plugin.getConfigData().getLayout(lName);
						if (layout == null) {
							plugin.sendMsg(p, plugin.defaults(plugin.getMsg("unknown-layout", "%layout%", lName)));
							return true;
						}
						plugin.getConfigData().addSign(b.getLocation(), server, layout);
						plugin.sendMsg(p, plugin.defaults(plugin.getMsg("editsign.created")));
					} else {
						plugin.sendMsg(p, plugin.defaults(plugin.getMsg("editsign.look-at-sign")));
					}
				} else {
					plugin.sendMsg(sender, plugin.defaults(plugin.getMsg("unknown-sub-command", "%subcmd%", args[0])));
					return true;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			plugin.throwMsg();
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> completionList = new ArrayList<>();
		if (cmd.getName().equalsIgnoreCase("teleportsigns") || cmd.getName().equalsIgnoreCase("ts")) {
			List<String> cmds = new ArrayList<>();
			if (args.length < 2) {
				for (String com : getCmds(sender)) {
					cmds.add(com);
				}
			}
			if (args.length < 3 && args[0].equalsIgnoreCase("connect")) {
				for (ServerInfo com : plugin.getConfigData().getServers()) {
					cmds.add(com.getName());
				}
			}
			StringUtil.copyPartialMatches(args[0], cmds, completionList);
		}
		Collections.sort(completionList);
		return completionList;
	}

	private List<String> getCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "help", "reload", "disable", "listlayouts", "listservers", "connect", "editsign" }) {
			if (!sender.hasPermission("teleportsigns." + cmds)) {
				continue;
			}
			c.add(cmds);
		}
		return c;
	}
}