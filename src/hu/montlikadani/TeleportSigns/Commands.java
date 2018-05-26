package hu.montlikadani.TeleportSigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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
					if (!sender.hasPermission(Permissions.PINFO) && plugin.getConfig().getBoolean("default-can-see-plugin-information") != true) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.plugininfo")));
						return true;
					}
					sender.sendMessage("§6§l[§2§lTeleport§e§lSigns§b§l Info§e§l]");
					sender.sendMessage("§5Version:§a ${version}");
					sender.sendMessage("§5Author, created by:§a montlikadani");
					sender.sendMessage("§5Commands:§8 /§7" + commandLabel + "§a help");
					sender.sendMessage("§4In case of an error, write here:§e §nhttps://github.com/montlikadani/TeleportSigns/issues");
					return true;
				} else if (args[0].equalsIgnoreCase("help")) {
					if (!sender.hasPermission(Permissions.HELP)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.help")));
						return true;
					}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.defaults(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel)));
							return true;
						}
					}
					for (String msg : plugin.messages.getStringList("chat-messages")) {
						sender.sendMessage(plugin.colorMsg(msg.replace("%command%", commandLabel).replace("%prefix%", plugin.messages.getString("prefix"))));
					}
					return true;
				} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
					if (!(sender.hasPermission(Permissions.RELOAD) && sender.isOp())) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.reload + op")));
						return true;
					}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.defaults(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel)));
							return true;
						}
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
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.defaults(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel)));
							return true;
						}
					}
					plugin.getServer().getPluginManager().disablePlugin(plugin);
					sender.sendMessage(plugin.defaults(plugin.messages.getString("warning-disable-command")));
					return true;
				} else if (args[0].equalsIgnoreCase("listlayouts")) {
					if (!sender.hasPermission(Permissions.LISTLAYOUT)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.listlayouts")));
						return true;
					}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.defaults(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel)));
							return true;
						}
					}
					try {
						Set<String> layouts = plugin.getConfigData().getConfig(ConfigType.LAYOUTS).getConfigurationSection("layouts").getKeys(false);
						StringBuilder sb = new StringBuilder();
						for (String s : layouts) {
							sb.append(s);
							sb.append(", ");
						}
						sender.sendMessage(plugin.defaults(plugin.messages.getString("list-layouts").replace("%layouts%", sb.toString())));
					} catch (NullPointerException e) {
						plugin.logConsole(Level.WARNING, "There are no layouts in the layouts.yml file!");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("connect")) {
					if (!sender.hasPermission(Permissions.CONNECT)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.connect")));
						return true;
					}
					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("no-console").replace("%command%", commandLabel)));
						return true;
					}
					if (args.length > 2) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.defaults(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel)));
							return true;
						}
					}
					if (args.length != 2) {
						sender.sendMessage(plugin.defaults(plugin.messages.getString("connect-usage").replace("%command%", commandLabel)));
						return true;
					}
					final String servergroup = args[1];
					Player p = (Player)sender;
					if (plugin.getConfigData().getServer(servergroup) != null) {
						ServerInfo server = plugin.getConfigData().getServer(servergroup);
						server.teleportPlayer(p);
					} else {
						p.sendMessage(plugin.defaults(plugin.messages.getString("server-group-not-found").replace("%server%", servergroup)));
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
    		List<String> list = new ArrayList<String>();
    		if (args.length == 1) {
    			list.add("help");
    		}
    		if (args.length == 1) {
    			list.add("disable");
    		}
    		if (args.length == 1) {
    			list.add("reload");
    		}
    		if (args.length == 1) {
    			list.add("listlayouts");
    		}
    		if (args.length == 1) {
    			list.add("connect");
    		}
    		return list;
    	}
    	return null;
    }
}
