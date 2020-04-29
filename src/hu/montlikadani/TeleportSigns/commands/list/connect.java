package hu.montlikadani.TeleportSigns.commands.list;

import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.TeleportSigns.Perm;
import hu.montlikadani.TeleportSigns.TeleportSigns;
import hu.montlikadani.TeleportSigns.commands.ICommand;
import hu.montlikadani.TeleportSigns.server.ServerInfo;

public class connect implements ICommand {

	@Override
	public boolean run(TeleportSigns plugin, CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(Perm.CONNECT.getPerm())) {
			sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.CONNECT.getPerm()));
			return false;
		}

		if (!(sender instanceof Player)) {
			sendMsg(sender, plugin.getMsg("no-console", "%command%", label, "%args%", args[0]));
			return false;
		}

		if (args.length != 2) {
			sendMsg(sender, plugin.getMsg("connect-usage", "%command%", label));
			return false;
		}

		Player p = (Player) sender;

		String serverGroup = args[1];
		ServerInfo server = plugin.getConfigData().getServer(serverGroup);
		if (server == null) {
			sendMsg(p, plugin.getMsg("server-group-not-found", "%server%", serverGroup));
			return false;
		}

		if (!server.isOnline()) {
			sendMsg(p, plugin.getMsg("server-offline", "%server%", serverGroup));
			return false;
		}

		server.teleportPlayer(p);
		return true;
	}
}
