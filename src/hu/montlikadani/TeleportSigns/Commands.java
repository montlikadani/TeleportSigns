package hu.montlikadani.TeleportSigns;

import static hu.montlikadani.TeleportSigns.utils.Util.colorMsg;
import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;
import static hu.montlikadani.TeleportSigns.utils.Util.throwMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;

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
					sendMsg(sender, colorMsg("&6&l[&2&lTeleport&e&lSigns&b&l Info&e&l]"));
					sendMsg(sender, colorMsg("&5Version:&a " + plugin.getDescription().getVersion()));
					sendMsg(sender, colorMsg("&5Author, created by:&a montlikadani"));
					sendMsg(sender, colorMsg("&5Commands:&8 /&7" + commandLabel + "&a help"));
					sendMsg(sender, colorMsg(
							"&4If you find a bug, send issue here:&e &nhttps://github.com/montlikadani/TeleportSigns/issues"));
				} else if (args[0].equalsIgnoreCase("help")) {
					if (!sender.hasPermission(Perm.HELP.getPerm())) {
						sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.HELP.getPerm()));
						return true;
					}

					plugin.getMessages().getStringList("chat-messages")
							.forEach(msg -> sender.sendMessage(colorMsg(msg.replace("%command%", commandLabel))));
				} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
					if (!sender.hasPermission(Perm.RELOAD.getPerm())) {
						sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.RELOAD.getPerm()));
						return true;
					}

					plugin.reload();
					sendMsg(sender, plugin.getMsg("reload-config"));
				} else if (args[0].equalsIgnoreCase("listlayouts")) {
					if (!sender.hasPermission(Perm.LISTLAYOUT.getPerm())) {
						sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.LISTLAYOUT.getPerm()));
						return true;
					}

					Set<String> layouts = plugin.getConfigData().getConfig(ConfigType.LAYOUTS)
							.getConfigurationSection("layouts").getKeys(false);
					if (layouts == null || layouts.isEmpty()) {
						sendMsg(sender, plugin.getMsg("no-layouts-found"));
						return true;
					}

					List<String> list = new ArrayList<>();
					list.addAll(layouts);

					sendMsg(sender, plugin.getMsg("list-layouts.header"));
					list.forEach(s -> sendMsg(sender, plugin.getMsg("list-layouts.list", "%layouts%", s)));
				} else if (args[0].equalsIgnoreCase("listservers")) {
					if (!sender.hasPermission(Perm.LISTSERVER.getPerm())) {
						sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.LISTSERVER.getPerm()));
						return true;
					}

					Set<String> servers = plugin.getMainConf().getConfigurationSection("servers").getKeys(false);
					if (servers == null || servers.isEmpty()) {
						sendMsg(sender, plugin.getMsg("no-servers-found"));
						return true;
					}

					List<String> list = new ArrayList<>();
					list.addAll(servers);

					sendMsg(sender, plugin.getMsg("list-servers.header"));
					for (String s : list) {
						sendMsg(sender,
								plugin.getMsg("list-servers.list", "%servers%", s, "%online%",
										plugin.getConfigData().getServer(s).isOnline()
												? plugin.getMsg("list-servers.online")
												: plugin.getMsg("list-servers.offline")));
					}
				} else if (args[0].equalsIgnoreCase("connect")) {
					if (!sender.hasPermission(Perm.CONNECT.getPerm())) {
						sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.CONNECT.getPerm()));
						return true;
					}

					if (!(sender instanceof Player)) {
						sendMsg(sender, plugin.getMsg("no-console", "%command%", commandLabel, "%args%", args[0]));
						return true;
					}

					if (args.length != 2) {
						sendMsg(sender, plugin.getMsg("connect-usage", "%command%", commandLabel));
						return true;
					}

					String serverGroup = args[1];
					Player p = (Player) sender;
					if (plugin.getConfigData().getServer(serverGroup) == null) {
						sendMsg(p, plugin.getMsg("server-group-not-found", "%server%", serverGroup));
						return true;
					}

					ServerInfo server = plugin.getConfigData().getServer(serverGroup);
					if (!server.isOnline()) {
						sendMsg(p, plugin.getMsg("server-offline", "%server%", serverGroup));
						return true;
					}

					server.teleportPlayer(p);
				} else if (args[0].equalsIgnoreCase("editsign")) {
					if (!sender.hasPermission(Perm.EDITSIGN.getPerm())) {
						sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.EDITSIGN.getPerm()));
						return true;
					}

					if (!(sender instanceof Player)) {
						sendMsg(sender, plugin.getMsg("no-console", "%command%", commandLabel, "%args%", args[0]));
						return true;
					}

					if (args.length != 3) {
						sendMsg(sender, plugin.getMsg("editsign.usage", "%command%", commandLabel));
						return true;
					}

					Player p = (Player) sender;
					Block b = p.getTargetBlock(null, 100);
					if (b == null || !(b.getState() instanceof Sign)) {
						sendMsg(p, plugin.getMsg("editsign.look-at-sign"));
						return true;
					}

					String sName = args[1];
					ServerInfo server = plugin.getConfigData().getServer(sName);
					if (server == null) {
						sendMsg(p, plugin.getMsg("unknown-server", "%server%", sName));
						return true;
					}

					String lName = args[2];
					SignLayout layout = plugin.getConfigData().getLayout(lName);
					if (layout == null) {
						sendMsg(p, plugin.getMsg("unknown-layout", "%layout%", lName));
						return true;
					}

					plugin.getConfigData().addSign(b.getLocation(), server, layout);
					sendMsg(p, plugin.getMsg("editsign.created"));
				} else {
					sendMsg(sender, plugin.getMsg("unknown-sub-command", "%subcmd%", args[0]));
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
		for (String cmds : Arrays.asList("help", "reload", "listlayouts", "listservers", "connect", "editsign")) {
			if (!sender.hasPermission("teleportsigns." + cmds)) {
				continue;
			}

			c.add(cmds);
		}
		return c;
	}
}