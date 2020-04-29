package hu.montlikadani.TeleportSigns.commands.list;

import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.TeleportSigns.Perm;
import hu.montlikadani.TeleportSigns.TeleportSigns;
import hu.montlikadani.TeleportSigns.commands.ICommand;

public class listservers implements ICommand {

	@Override
	public boolean run(TeleportSigns plugin, CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(Perm.LISTSERVER.getPerm())) {
			sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.LISTSERVER.getPerm()));
			return false;
		}

		Set<String> servers = plugin.getMainConf().contains("servers")
				? plugin.getMainConf().getConfigurationSection("servers").getKeys(false)
				: null;
		if (servers == null || servers.isEmpty()) {
			sendMsg(sender, plugin.getMsg("no-servers-found"));
			return false;
		}

		List<String> list = new ArrayList<>();
		list.addAll(servers);

		sendMsg(sender, plugin.getMsg("list-servers.header"));
		for (String s : list) {
			sendMsg(sender,
					plugin.getMsg("list-servers.list", "%servers%", s, "%online%",
							plugin.getConfigData().getServer(s).isOnline() ? plugin.getMsg("list-servers.online")
									: plugin.getMsg("list-servers.offline")));
		}
		return true;
	}
}
