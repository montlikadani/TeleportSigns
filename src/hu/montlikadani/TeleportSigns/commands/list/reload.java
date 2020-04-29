package hu.montlikadani.TeleportSigns.commands.list;

import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.TeleportSigns.Perm;
import hu.montlikadani.TeleportSigns.TeleportSigns;
import hu.montlikadani.TeleportSigns.commands.ICommand;

public class reload implements ICommand {

	@Override
	public boolean run(TeleportSigns plugin, CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(Perm.RELOAD.getPerm())) {
			sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.RELOAD.getPerm()));
			return false;
		}

		plugin.reload();
		sendMsg(sender, plugin.getMsg("reload-config"));
		return true;
	}
}
