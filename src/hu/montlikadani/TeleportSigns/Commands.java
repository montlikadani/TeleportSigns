package hu.montlikadani.TeleportSigns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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
						sender.sendMessage(plugin.colorMsg(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.plugininfo").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
						return true;
					}
					sender.sendMessage("§6§l[§2§lTeleport§e§lSigns§b§l Info§e§l]");
					sender.sendMessage("§5Version:§a 1.7");
					sender.sendMessage("§5Author, created by:§a montlikadani");
					sender.sendMessage("§5Commands:§8 /§7" + commandLabel + "§a help");
					sender.sendMessage("§4In case of an error, write here:§e §nhttps://github.com/montlikadani/TeleportSigns/issues");
					return true;
				} else if (args[0].equalsIgnoreCase("help")) {
					if (!sender.hasPermission(Permissions.HELP)) {
						sender.sendMessage(plugin.colorMsg(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.help").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
						return true;
					}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.colorMsg(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
							return true;
						}
					}
					for (String msg : plugin.messages.getStringList("chat-messages")) {
						sender.sendMessage(plugin.colorMsg(msg.replace("%command%", commandLabel).replace("%prefix%", plugin.messages.getString("prefix"))));
					}
					return true;
				} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
					if (!(sender.hasPermission(Permissions.RELOAD) && sender.isOp())) {
						sender.sendMessage(plugin.colorMsg(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.reload + op").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
						return true;
					}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.colorMsg(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
							return true;
						}
					}
					plugin.reload();
					sender.sendMessage(plugin.colorMsg(plugin.messages.getString("reload-config").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
					return true;
				} else if (args[0].equalsIgnoreCase("disable")) {
					if (!(sender.hasPermission(Permissions.PDISABLE) && sender.isOp())) {
						sender.sendMessage(plugin.colorMsg(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.plugindisable + op").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
						return true;
					}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.colorMsg(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
							return true;
						}
					}
					plugin.getServer().getPluginManager().disablePlugin(plugin);
					sender.sendMessage(plugin.colorMsg(plugin.messages.getString("warning-disable-command").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
					return true;
				} else if (args[0].equalsIgnoreCase("checkip")) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.colorMsg(plugin.messages.getString("no-console-check-IP").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
						return true;
					}
		  			if (!(sender.hasPermission(Permissions.CHECKIP) && sender.isOp())) {
		  				sender.sendMessage(plugin.colorMsg(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.checkip + op").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
		  				return true;
		  			}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.colorMsg(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
							return true;
						}
					}
					Player p = (Player)sender;
					sender.sendMessage(plugin.colorMsg(plugin.messages.getString("check-IP-message").replace("%ipaddress%", TeleportSigns.getIP(p) + ":" + TeleportSigns.getPort(p)).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
					return true;
				} else if (args[0].equalsIgnoreCase("listlayouts")) {
					if (!sender.hasPermission(Permissions.LISTLAYOUT)) {
						sender.sendMessage(plugin.colorMsg(plugin.messages.getString("no-permission").replace("%perm%", "teleportsigns.listlayouts").replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
						return true;
					}
					if (args.length > 1) {
						if (plugin.getConfig().getBoolean("unknown-command-enable")) {
							sender.sendMessage(plugin.colorMsg(plugin.getConfig().getString("unknown-command").replace("%command%", commandLabel).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
							return true;
						}
					}
					for (String layouts : plugin.layout.getConfigurationSection("layouts").getKeys(false)) {
						sender.sendMessage(plugin.colorMsg(plugin.messages.getString("list-layouts").replace("%layouts%", layouts).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
					}
					return true;
				} else {
					sender.sendMessage(plugin.colorMsg(plugin.messages.getString("unknown-sub-command").replace("%subcmd%", args[0]).replace("%newline%", "\n").replace("%prefix%", plugin.messages.getString("prefix"))));
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
            list.add("checkip");
        }

        if (args.length == 1) {
            list.add("listlayouts");
        }

        return list;
        }
    	return null;
    }
}
