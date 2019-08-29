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

import static hu.montlikadani.TeleportSigns.Messager.replaceColor;
import static hu.montlikadani.TeleportSigns.Messager.defaults;
import static hu.montlikadani.TeleportSigns.Messager.colorMsg;
import static hu.montlikadani.TeleportSigns.Messager.throwMsg;
import static hu.montlikadani.TeleportSigns.Messager.sendMsg;

public class Commands implements CommandExecutor, TabCompleter {

	private TeleportSigns plugin;

	Commands(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		try {
			if (cmd.getName().equalsIgnoreCase("teleportsigns")) {
				if (args.length == 0) {
					sender.sendMessage(replaceColor("&6&l[&2&lTeleport&e&lSigns&b&l Info&e&l]"));
					sender.sendMessage(replaceColor("&5Version:&a " + plugin.getDescription().getVersion()));
					sender.sendMessage(replaceColor("&5Author, created by:&a montlikadani"));
					sender.sendMessage(replaceColor("&5Commands:&8 /&7" + commandLabel + "&a help"));
					sender.sendMessage(replaceColor("&4If you find a bug, send issue here:&e &nhttps://github.com/montlikadani/TeleportSigns/issues"));
				} else if (args[0].equalsIgnoreCase("help")) {
					if (!sender.hasPermission(Perm.HELP.getPerm())) {
						sendMsg(sender, defaults(plugin.getMsg("no-permission", "%perm%", Perm.HELP.getPerm())));
						return true;
					}

					if (args.length > 1) {
						sendMsg(sender, defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}

					plugin.getMessages().getStringList("chat-messages").forEach(
							msg -> sender.sendMessage(colorMsg(msg.replace("%command%", commandLabel))));
				} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
					if (!sender.hasPermission(Perm.RELOAD.getPerm())) {
						sendMsg(sender, defaults(plugin.getMsg("no-permission", "%perm%", Perm.RELOAD.getPerm())));
						return true;
					}

					if (args.length > 1) {
						sendMsg(sender, defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}

					plugin.reload();

					sendMsg(sender, defaults(plugin.getMsg("reload-config")));
				} else if (args[0].equalsIgnoreCase("listlayouts")) {
					if (!sender.hasPermission(Perm.LISTLAYOUT.getPerm())) {
						sendMsg(sender, defaults(plugin.getMsg("no-permission", "%perm%", Perm.LISTLAYOUT.getPerm())));
						return true;
					}

					if (args.length > 1) {
						sendMsg(sender, defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}

					List<String> list = new ArrayList<>();
					Set<String> layouts = plugin.getConfigData().getConfig(ConfigType.LAYOUTS)
							.getConfigurationSection("layouts").getKeys(false);

					if (layouts == null || layouts.isEmpty()) {
						sendMsg(sender, defaults(plugin.getMsg("no-layouts-found")));
						return true;
					}

					list.addAll(layouts);

					sendMsg(sender, plugin.getMsg("list-layouts.header"));
					list.forEach(s -> sendMsg(sender,
							defaults(plugin.getMsg("list-layouts.list", "%layouts%", s))));
				} else if (args[0].equalsIgnoreCase("listservers")) {
					if (!sender.hasPermission(Perm.LISTSERVER.getPerm())) {
						sendMsg(sender, defaults(plugin.getMsg("no-permission", "%perm%", Perm.LISTSERVER.getPerm())));
						return true;
					}

					if (args.length > 1) {
						sendMsg(sender, defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}

					List<String> list = new ArrayList<>();
					Set<String> servers = plugin.getMainConf().getConfigurationSection("servers").getKeys(false);

					if (servers == null || servers.isEmpty()) {
						sendMsg(sender, defaults(plugin.getMsg("no-servers-found")));
						return true;
					}

					list.addAll(servers);

					sendMsg(sender, plugin.getMsg("list-servers.header"));
					for (String s : list) {
						sendMsg(sender,
								defaults(plugin.getMsg("list-servers.list", "%servers%", s, "%online%",
										plugin.getConfigData().getServer(s).isOnline()
												? plugin.getMsg("list-servers.online")
												: plugin.getMsg("list-servers.offline"))));
					}
				} else if (args[0].equalsIgnoreCase("connect")) {
					if (!sender.hasPermission(Perm.CONNECT.getPerm())) {
						sendMsg(sender, defaults(plugin.getMsg("no-permission", "%perm%", Perm.CONNECT.getPerm())));
						return true;
					}

					if (!(sender instanceof Player)) {
						sendMsg(sender, defaults(plugin.getMsg("no-console", "%command%", commandLabel, "%args%", args[0])));
						return true;
					}

					if (args.length > 2) {
						sendMsg(sender, defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
						return true;
					}

					if (args.length != 2) {
						sendMsg(sender, defaults(plugin.getMsg("connect-usage", "%command%", commandLabel)));
						return true;
					}

					String serverGroup = args[1];
					Player p = (Player) sender;
					if (plugin.getConfigData().getServer(serverGroup) != null) {
						ServerInfo server = plugin.getConfigData().getServer(serverGroup);
						if (!server.isOnline()) {
							sendMsg(p, defaults(plugin.getMsg("server-offline", "%server%", serverGroup)));
							return true;
						}

						server.teleportPlayer(p);
					} else {
						sendMsg(p, defaults(plugin.getMsg("server-group-not-found", "%server%", serverGroup)));
					}
				} else if (args[0].equalsIgnoreCase("editsign")) {
					if (!sender.hasPermission(Perm.EDITSIGN.getPerm())) {
						sendMsg(sender, defaults(plugin.getMsg("no-permission", "%perm%", Perm.EDITSIGN.getPerm())));
						return true;
					}

					if (!(sender instanceof Player)) {
						sendMsg(sender, defaults(plugin.getMsg("no-console", "%command%", commandLabel, "%args%", args[0])));
						return true;
					}

					if (args.length != 3) {
						sendMsg(sender, defaults(plugin.getMsg("editsign.usage", "%command%", commandLabel)));
						return true;
					}

					if (args.length > 3) {
						sendMsg(sender, defaults(plugin.getMsg("unknown-command", "%command%", commandLabel)));
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
							sendMsg(p, defaults(plugin.getMsg("unknown-server", "%server%", sName)));
							return true;
						}

						SignLayout layout = plugin.getConfigData().getLayout(lName);
						if (layout == null) {
							sendMsg(p, defaults(plugin.getMsg("unknown-layout", "%layout%", lName)));
							return true;
						}

						plugin.getConfigData().addSign(b.getLocation(), server, layout);
						sendMsg(p, defaults(plugin.getMsg("editsign.created")));
					} else {
						sendMsg(p, defaults(plugin.getMsg("editsign.look-at-sign")));
					}
				} else {
					sendMsg(sender, defaults(plugin.getMsg("unknown-sub-command", "%subcmd%", args[0])));
					return true;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throwMsg();
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 1) {
			List<String> completionList = new ArrayList<>();
			List<String> cmds = new ArrayList<>();
			String partOfCommand = "";

			if (args.length < 2) {
				partOfCommand = args[0];
				getCmds(sender).forEach(cmds::add);
			} else if (args.length < 3 && args[0].equalsIgnoreCase("connect")) {
				plugin.getConfigData().getServers().forEach(com -> cmds.add(com.getName()));
				partOfCommand = args[1];
			}

			StringUtil.copyPartialMatches(partOfCommand, cmds, completionList);
			Collections.sort(completionList);

			return completionList;
		}

		return null;
	}

	private List<String> getCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "help", "reload", "listlayouts", "listservers", "connect", "editsign" }) {
			if (!sender.hasPermission("teleportsigns." + cmds)) {
				continue;
			}

			c.add(cmds);
		}
		return c;
	}
}