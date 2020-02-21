package hu.montlikadani.TeleportSigns.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;

public class SignUtil {

	private static Set<Material> WALL_SIGNS = new HashSet<>();

	static {
		WALL_SIGNS.add(Material.getMaterial("WALL_SIGN"));

		WALL_SIGNS.add(Material.getMaterial("ACACIA_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("BIRCH_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("DARK_OAK_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("JUNGLE_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("OAK_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("SPRUCE_WALL_SIGN"));
	}

	public static boolean isWallSign(Material mat) {
		return WALL_SIGNS.contains(mat);
	}

	public static boolean isSign(BlockState state) {
		return state instanceof Sign || state instanceof WallSign;
	}

	public static void signLines(Sign sign) {
		for (int i = 0; i < 4; i++) {
			sign.setLine(i, "");
		}
	}

	public static void signLines(Sign sign, List<String> lines) {
		for (int i = 0; i < 4; i++) {
			sign.setLine(i, lines.get(i));
		}
	}

	public static void signLines(Sign sign, String[] lines) {
		for (int i = 0; i < 4; i++) {
			sign.setLine(i, lines[i]);
		}
	}
}
