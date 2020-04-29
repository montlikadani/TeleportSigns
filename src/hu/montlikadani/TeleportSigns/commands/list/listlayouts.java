package hu.montlikadani.TeleportSigns.commands.list;

import static hu.montlikadani.TeleportSigns.utils.Util.sendMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.TeleportSigns.Perm;
import hu.montlikadani.TeleportSigns.TeleportSigns;
import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;
import hu.montlikadani.TeleportSigns.commands.ICommand;

public class listlayouts implements ICommand {

	@Override
	public boolean run(TeleportSigns plugin, CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(Perm.LISTLAYOUT.getPerm())) {
			sendMsg(sender, plugin.getMsg("no-permission", "%perm%", Perm.LISTLAYOUT.getPerm()));
			return false;
		}

		Set<String> layouts = plugin.getConfigData().getConfig(ConfigType.LAYOUTS).contains("layouts")
				? plugin.getConfigData().getConfig(ConfigType.LAYOUTS).getConfigurationSection("layouts").getKeys(false)
				: null;
		if (layouts == null || layouts.isEmpty()) {
			sendMsg(sender, plugin.getMsg("no-layouts-found"));
			return false;
		}

		List<String> list = new ArrayList<>();
		list.addAll(layouts);

		sendMsg(sender, plugin.getMsg("list-layouts.header"));
		list.forEach(s -> sendMsg(sender, plugin.getMsg("list-layouts.list", "%layouts%", s)));
		return true;
	}
}
