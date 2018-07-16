package hu.montlikadani.TeleportSigns;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.material.Directional;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.TeleportSigns.ConfigData.ConfigType;

public class TeleportSign {

	private TeleportSigns plugin = JavaPlugin.getPlugin(TeleportSigns.class);

	private String world;
	private int x;
	private int y;
	private int z;

	private String id;
	private ServerInfo server;
	private SignLayout layout;
	private boolean broken;

	public TeleportSign(ServerInfo server, Location location, SignLayout layout) {
		this.world = location.getWorld().getName();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();

		this.server = server;
		this.layout = layout;
		this.broken = false;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setServer(ServerInfo server) {
		this.server = server;
	}

	public ServerInfo getServer() {
		return server;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public String getWorld() {
		return world;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getZ() {
		return z;
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(getWorld()), getX(), getY(), getZ());
	}

	public void setLocation(Location loc) {
		setWorld(loc.getWorld().getName());
		setX(loc.getBlockX());
		setY(loc.getBlockY());
		setZ(loc.getBlockZ());
	}

	public void setLayout(SignLayout layout) {
		this.layout = layout;
	}

	public SignLayout getLayout() {
		return layout;
	}

	public boolean isBroken()  {
		return broken;
	}

	public void updateSign() {
		Configuration c = plugin.getConfigData().getConfig(ConfigType.CONFIG);
		if (!isBroken()) {
			Location location = getLocation();

			if (location.getWorld().getChunkAt(location).isLoaded()) {
				Block b = location.getBlock();
				if (b.getState() instanceof Sign) {
					if (server != null) {
						if (layout != null) {
							Sign sign = (Sign) b.getState();
							List<String> lines = layout.parseLayout(server);
							for (int i = 0; i < 4; i++) {
								sign.setLine(i, (String) lines.get(i));
								if (c.getBoolean("options.background.enable")) {
									if (server.isOnline()) {
										if (sign.getType().equals(Material.WALL_SIGN)) {
											if (c.getString("options.background.type").equalsIgnoreCase("wool")) {
												updateBackground(Material.WOOL,
														c.getInt("options.background.block-colors.online.wool"));
											} else if (c.getString("options.background.type").equalsIgnoreCase("glass")) {
												updateBackground(Material.STAINED_GLASS,
														c.getInt("options.background.block-colors.online.glass"));
											} else if (c.getString("options.background.type").equalsIgnoreCase("clay")) {
												updateBackground(Material.STAINED_CLAY,
														c.getInt("options.background.block-colors.online.clay"));
											}
										}
									} else {
										if (sign.getType().equals(Material.WALL_SIGN)) {
											if (c.getString("options.background.type").equalsIgnoreCase("wool")) {
												updateBackground(Material.WOOL,
														c.getInt("options.background.block-colors.offline.wool"));
											} else if (c.getString("options.background.type").equalsIgnoreCase("glass")) {
												updateBackground(Material.STAINED_GLASS,
														c.getInt("options.background.block-colors.offline.glass"));
											} else if (c.getString("options.background.type").equalsIgnoreCase("clay")) {
												updateBackground(Material.STAINED_CLAY,
														c.getInt("options.background.block-colors.offline.clay"));
											}
										}
									}
								}
							}
							sign.update(true);
						} else {
							Sign sign = (Sign) b.getState();
							plugin.logConsole(Level.WARNING, "Can't find layout '" + this.layout + "'.");
							String[] error = { "§4ERROR:", "§6Layout", "§e" + this.layout.getName(), "§6not found!" };
							signError(sign, error);
							if (c.getBoolean("options.drop-sign")) {
								sign.getLocation().getBlock().breakNaturally();
							}
							this.broken = true;
						}
					} else {
						Sign sign = (Sign) b.getState();
						plugin.logConsole(Level.WARNING, "Can't find server '" + this.server + "'.");
						String[] error = { "§4ERROR:", "§6Server", "§e" + this.server.getName(), "§6not found!" };
						signError(sign, error);
						if (c.getBoolean("options.drop-sign")) {
							sign.getLocation().getBlock().breakNaturally();
						}
						this.broken = true;
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void updateBackground(Material mat, int color) {
		Location loc3 = this.getLocation();
		BlockState s = (Sign) loc3.getBlock().getState();
		if (s.getType() == Material.WALL_SIGN) {
			BlockFace bf = ((Directional) s.getData()).getFacing();
			Location loc2 = new Location(loc3.getWorld(), loc3.getBlockX() - bf.getModX(), loc3.getBlockY() - bf.getModY(), loc3.getBlockZ() - bf.getModZ());
			Block wall = loc2.getBlock();
			wall.setType(mat);
			wall.setData((byte) color);
		}
	}

	private void signError(Sign sign, String[] exception) {
		if (sign != null) {
			for (int i = 0; i < 4; i++) {
				String line = editLine(exception[i], i);
				sign.setLine(i, line);
			}

			sign.update(true);
		}
	}

	private String editLine(String text, int num) {
		int length = text.length();

		if (num == 2) {
			if (length > 15) {
				text = text.substring(0, 11);
				text = text + "...";
				return text;
			}
		}

		if (length > 16) {
			text = text.substring(0, 16);
		}

		return text;
	}
}