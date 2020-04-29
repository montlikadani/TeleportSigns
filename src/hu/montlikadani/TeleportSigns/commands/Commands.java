package hu.montlikadani.TeleportSigns.commands;

import static hu.montlikadani.TeleportSigns.utils.Util.colorMsg;
import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import hu.montlikadani.TeleportSigns.Perm;
import hu.montlikadani.TeleportSigns.TeleportSigns;

public class Commands implements CommandExecutor, TabCompleter {

	private TeleportSigns plugin;

	public Commands(TeleportSigns plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
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
		}

		String path = "hu.montlikadani.TeleportSigns.commands.list";
		ICommand command = null;
		try {
			command = (ICommand) TeleportSigns.class.getClassLoader().loadClass(path + "." + args[0].toLowerCase())
					.newInstance();
		} catch (ClassNotFoundException e) {
			sendMsg(sender, plugin.getMsg("unknown-sub-command", "%subcmd%", args[0]));
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}

		if (command != null) {
			command.run(plugin, sender, cmd, commandLabel, args);
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