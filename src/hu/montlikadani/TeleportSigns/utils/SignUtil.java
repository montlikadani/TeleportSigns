package hu.montlikadani.TeleportSigns.utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

import hu.montlikadani.TeleportSigns.MinecraftVersion.Version;

public class SignUtil {

	private static Set<Material> WALL_SIGNS = new HashSet<>();

	static {
		WALL_SIGNS.clear();

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

	public static String editLine(String text, int num) {
		int length = text.length();

		if (num == 2) {
			if (length > 15) {
				text = text.substring(0, 11);
				text = text + "...";
				return text;
			}
		}

		return editText(text);
	}

	public static String editText(String text) {
		int length = text.length();
		if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && length > 25) {
			return text = text.substring(0, 25);
		}

		if (length > 16) {
			text = text.substring(0, 16);
		}

		return text;
	}
}
