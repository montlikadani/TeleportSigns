package hu.montlikadani.TeleportSigns.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import hu.montlikadani.TeleportSigns.ServerInfo;
import hu.montlikadani.TeleportSigns.SignLayout;

public class TeleportSignsPlaceEvent extends BaseEvent {

	private Player player;
	private Block block;
	private ServerInfo server;
	private SignLayout layout;

	public TeleportSignsPlaceEvent(Player player, Block block, ServerInfo server, SignLayout layout) {
		this.player = player;
		this.block = block;
		this.server = server;
		this.layout = layout;
	}

	public Player getPlayer() {
		return player;
	}

	public Block getBlock() {
		return block;
	}

	public ServerInfo getServer() {
		return server;
	}

	public SignLayout getLayout() {
		return layout;
	}
}
