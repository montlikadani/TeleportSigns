package hu.montlikadani.TeleportSigns.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.TeleportSigns.TeleportSigns;

public interface ICommand {

	boolean run(final TeleportSigns plugin, final CommandSender sender, final Command cmd, final String label, final String[] args);
}
