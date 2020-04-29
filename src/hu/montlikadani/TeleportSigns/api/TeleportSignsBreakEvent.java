package hu.montlikadani.TeleportSigns.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import hu.montlikadani.TeleportSigns.sign.TeleportSign;

public class TeleportSignsBreakEvent extends BaseEvent implements Cancellable {

	private Player player;
	private Block block;
	private TeleportSign sign;
	private boolean cancelled = false;

	public TeleportSignsBreakEvent(Player player, Block block, TeleportSign sign) {
		this.player = player;
		this.block = block;
		this.sign = sign;
	}

	public Player getPlayer() {
		return player;
	}

	public TeleportSign getSign() {
		return sign;
	}

	public Block getBlock() {
		return block;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
}
