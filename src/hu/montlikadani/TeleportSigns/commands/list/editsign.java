package hu.montlikadani.TeleportSigns.commands.list;

import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.TeleportSigns.Perm;
import hu.montlikadani.TeleportSigns.TeleportSigns;
import hu.montlikadani.TeleportSigns.commands.ICommand;
import hu.montlikadani.TeleportSigns.server.ServerInfo;
import hu.montlikadani.TeleportSigns.sign.SignLayout;

public class editsign implements ICommand {

	@Override
	public boolean run(TeleportSigns plugin, CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(Perm.EDITSIGN.getPerm())) {
			sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.EDITSIGN.getPerm()));
			return false;
		}

		if (!(sender instanceof Player)) {
			sendMsg(sender, plugin.getMsg("no-console", "%command%", label, "%args%", args[0]));
			return false;
		}

		if (args.length != 3) {
			sendMsg(sender, plugin.getMsg("editsign.usage", "%command%", label));
			return false;
		}

		Player p = (Player) sender;
		Block b = p.getTargetBlock(null, 100);
		if (b == null || !(b.getState() instanceof Sign)) {
			sendMsg(p, plugin.getMsg("editsign.look-at-sign"));
			return false;
		}

		String sName = args[1];
		ServerInfo server = plugin.getConfigData().getServer(sName);
		if (server == null) {
			sendMsg(p, plugin.getMsg("unknown-server", "%server%", sName));
			return false;
		}

		String lName = args[2];
		SignLayout layout = plugin.getConfigData().getLayout(lName);
		if (layout == null) {
			sendMsg(p, plugin.getMsg("unknown-layout", "%layout%", lName));
			return false;
		}

		plugin.getConfigData().addSign(b.getLocation(), server, layout);
		sendMsg(p, plugin.getMsg("editsign.created"));
		return true;
	}
}
