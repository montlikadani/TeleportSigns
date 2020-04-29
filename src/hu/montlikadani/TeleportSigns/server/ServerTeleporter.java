package hu.montlikadani.TeleportSigns.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import hu.montlikadani.TeleportSigns.TeleportSigns;
import hu.montlikadani.TeleportSigns.utils.Util;

public class ServerTeleporter implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] bytes) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
	}

	private static String server = null;

	public static boolean teleportPlayer(Player p, String name) {
		if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TeleportSigns.getInstance(), "BungeeCord")) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(TeleportSigns.getInstance(), "BungeeCord");
		}

		// TODO: How to check from bungee when the player is already connected to the
		// server?
		if (name.equals(server)) {
			Util.sendMsg(p, TeleportSigns.getInstance().getMsg("already-in-server"));
			return false;
		}

		server = name;

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Connect");
			out.writeUTF(name);
		} catch (IOException e) {
			e.printStackTrace();
			Util.logConsole(Level.WARNING, p.getName() + ": You'll never see me!");
			return false;
		}

		p.sendPluginMessage(TeleportSigns.getInstance(), "BungeeCord", b.toByteArray());

		try {
			b.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
