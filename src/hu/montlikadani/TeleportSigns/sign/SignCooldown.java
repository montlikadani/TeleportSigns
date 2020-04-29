package hu.montlikadani.TeleportSigns.sign;

import org.bukkit.entity.Player;

import hu.montlikadani.TeleportSigns.Perm;

public class SignCooldown {

	private Player player;
	private long cooldownTime = 0L;
	private TeleportSign sign;

	private long cooldown = 0L;

	public SignCooldown(Player player, long cooldownTime, TeleportSign sign) {
		this.player = player;
		this.cooldownTime = cooldownTime;
		this.sign = sign;
	}

	public Player getPlayer() {
		return player;
	}

	public long getCooldownTime() {
		return cooldownTime;
	}

	public TeleportSign getSign() {
		return sign;
	}

	public boolean hasCooldown() {
		if (player.hasPermission(Perm.NOCOOLDOWN.getPerm()) || cooldown == 0L) {
			return false;
		}

		long time = System.currentTimeMillis();
		long result = (time - cooldown);

		if (result >= cooldownTime) {
			cooldown = 0L;
			return false;
		}

		return true;
	}

	public void addCooldown() {
		if (cooldownTime > 0 && !player.hasPermission(Perm.NOCOOLDOWN.getPerm()) && cooldown == 0L) {
			cooldown = System.currentTimeMillis();
		}
	}

	public int getCooldown() {
		if (player.hasPermission(Perm.NOCOOLDOWN.getPerm()) || cooldown == 0L) {
			return 0;
		}

		long time = System.currentTimeMillis();
		long result = (cooldown - time);
		int wait = (int) (result / 1000);
		int towait = (int) ((cooldownTime / 1000) + wait);

		return towait;
	}
}
