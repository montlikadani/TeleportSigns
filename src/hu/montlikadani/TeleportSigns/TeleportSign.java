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

public class TeleportSign {

	private TeleportSigns plugin = TeleportSigns.getPlugin(TeleportSigns.class);

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
		return new Location(Bukkit.getWorld(world), x, y, z);
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

	public boolean isBroken() {
		return broken;
	}

	public void updateSign() {
		Configuration c = plugin.getMainConf();
		if (!isBroken()) {
			Location location = getLocation();

			if (location.getWorld().getChunkAt(location).isLoaded()) {
				Block b = location.getBlock();
				if (b.getState() instanceof Sign) {
					Sign sign = (Sign) b.getState();
					if (server != null) {
						if (layout != null) {
							List<String> lines = layout.parseLayout(server);
							if (lines.size() >= 5 || lines.size() <= 3) {
								plugin.logConsole("In the configuration the signs lines not more/less than 4.");
								return;
							}

							for (int i = 0; i < 4; i++) {
								sign.setLine(i, lines.get(i).toString());

								if (c.getBoolean("options.background.enable")) {
									if (Bukkit.getVersion().contains("1.14")) {
										if (org.bukkit.Tag.WALL_SIGNS.isTagged(sign.getType())) {
											chooseFromType();
										}
									} else if (b.getType() == Material.getMaterial("WALL_SIGN")) {
										chooseFromType();
									}
								}
							}
							sign.update();
						} else {
							plugin.logConsole(Level.WARNING, "Can't find layout '" + layout + "'.");
							String[] error = { "\u00a74ERROR:", "\u00a76Layout", "\u00a7e" + layout.getName(), "\u00a76not found!" };
							signError(sign, error);
							broken = true;
						}
					} else {
						plugin.logConsole(Level.WARNING, "Can't find server '" + server + "'.");
						String[] error = { "\u00a74ERROR:", "\u00a76Server", "\u00a7e" + server.getName(), "\u00a76not found!" };
						signError(sign, error);
						broken = true;
					}
				}
			}
		}
	}

	public void updateBackground(Material mat) {
		updateBackground(mat, 0);
	}

	public void updateBackground(Material mat, int color) {
		Location loc = getLocation();
		BlockState s = (Sign) loc.getBlock().getState();
		BlockFace bf = null;
		try {
			bf = ((Directional) s.getData()).getFacing();
		} catch (ClassCastException e) {
			org.bukkit.block.data.type.WallSign data = (org.bukkit.block.data.type.WallSign) s.getBlockData();
			bf = data.getFacing();
		}
		Location loc2 = new Location(loc.getWorld(), loc.getBlockX() - bf.getModX(), loc.getBlockY() - bf.getModY(),
				loc.getBlockZ() - bf.getModZ());
		Block wall = loc2.getBlock();
		wall.setType(mat);
		if (!(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14"))) {
			try {
				Block.class.getMethod("setData", byte.class).invoke(wall, (byte) color);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		if (Bukkit.getVersion().contains("1.14")) {
			if (length > 25) {
				text = text.substring(0, 25);
			}
		} else {
			if (length > 16) {
				text = text.substring(0, 16);
			}
		}

		return text;
	}

	private void chooseFromType() {
		if (Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.13")) {
			if (server.isOnline()) {
				if (server.getPlayerCount() == 0) {
					if (plugin.getBackgroundType().equals("wool")) {
						updateBackground(Material.WHITE_WOOL);
					} else if (plugin.getBackgroundType().equals("glass")) {
						updateBackground(Material.WHITE_STAINED_GLASS);
					} else if (plugin.getBackgroundType().equals("clay")
							|| plugin.getBackgroundType().equals("terracotta")) {
						updateBackground(Material.WHITE_TERRACOTTA);
					}
				} else if (server.getPlayerCount() == server.getMaxPlayers()) {
					if (plugin.getBackgroundType().equals("wool")) {
						updateBackground(Material.BLUE_WOOL);
					} else if (plugin.getBackgroundType().equals("glass")) {
						updateBackground(Material.BLUE_STAINED_GLASS);
					} else if (plugin.getBackgroundType().equals("clay")
							|| plugin.getBackgroundType().equals("terracotta")) {
						updateBackground(Material.BLUE_TERRACOTTA);
					}
				} else {
					if (plugin.getBackgroundType().equals("wool")) {
						updateBackground(Material.LIME_WOOL);
					} else if (plugin.getBackgroundType().equals("glass")) {
						updateBackground(Material.LIME_STAINED_GLASS);
					} else if (plugin.getBackgroundType().equals("clay")
							|| plugin.getBackgroundType().equals("terracotta")) {
						updateBackground(Material.LIME_TERRACOTTA);
					}
				}
			} else {
				if (plugin.getBackgroundType().equals("wool")) {
					updateBackground(Material.RED_WOOL);
				} else if (plugin.getBackgroundType().equals("glass")) {
					updateBackground(Material.RED_STAINED_GLASS);
				} else if (plugin.getBackgroundType().equals("clay")
						|| plugin.getBackgroundType().equals("terracotta")) {
					updateBackground(Material.RED_TERRACOTTA);
				}
			}
		} else {
			if (server.isOnline()) {
				if (server.getPlayerCount() == 0) {
					if (plugin.getBackgroundType().equals("wool")) {
						updateBackground(Material.getMaterial("WOOL"), 0);
					} else if (plugin.getBackgroundType().equals("glass")) {
						updateBackground(Material.getMaterial("STAINED_GLASS"), 0);
					} else if (plugin.getBackgroundType().equals("clay")
							|| plugin.getBackgroundType().equals("terracotta")) {
						updateBackground(Material.getMaterial("STAINED_CLAY"), 0);
					}
				} else if (server.getPlayerCount() == server.getMaxPlayers()) {
					if (plugin.getBackgroundType().equals("wool")) {
						updateBackground(Material.getMaterial("WOOL"), 11);
					} else if (plugin.getBackgroundType().equals("glass")) {
						updateBackground(Material.getMaterial("STAINED_GLASS"), 11);
					} else if (plugin.getBackgroundType().equals("clay")
							|| plugin.getBackgroundType().equals("terracotta")) {
						updateBackground(Material.getMaterial("STAINED_CLAY"), 11);
					}
				} else {
					if (plugin.getBackgroundType().equals("wool")) {
						updateBackground(Material.getMaterial("WOOL"), 5);
					} else if (plugin.getBackgroundType().equals("glass")) {
						updateBackground(Material.getMaterial("STAINED_GLASS"), 5);
					} else if (plugin.getBackgroundType().equals("clay")
							|| plugin.getBackgroundType().equals("terracotta")) {
						updateBackground(Material.getMaterial("STAINED_CLAY"), 5);
					}
				}
			} else {
				if (plugin.getBackgroundType().equals("wool")) {
					updateBackground(Material.getMaterial("WOOL"), 14);
				} else if (plugin.getBackgroundType().equals("glass")) {
					updateBackground(Material.getMaterial("STAINED_GLASS"), 14);
				} else if (plugin.getBackgroundType().equals("clay")
						|| plugin.getBackgroundType().equals("terracotta")) {
					updateBackground(Material.getMaterial("STAINED_CLAY"), 14);
				}
			}
		}
	}
}