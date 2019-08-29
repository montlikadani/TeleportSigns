package hu.montlikadani.TeleportSigns;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static hu.montlikadani.TeleportSigns.Messager.sendMsg;
import static hu.montlikadani.TeleportSigns.Messager.logConsole;

public class ServerInfo {

	private ServerPingInternal internalPing;
	private ServerPingExternal externalPing;
	private InetSocketAddress address;

	private boolean local;
	private boolean online;

	private int playercount;
	private int maxplayers;
	private int timeout;

	private String motd;
	private String name;
	private String displayname;
	private String version;
	private String protocol;

	private long pingStartTime;
	private long pingEndTime;

	public ServerInfo(String name, String displayname, String address, int port, int timeout) {
		if (TeleportSigns.getInstance().getConfigData().isExternal()) {
			this.externalPing = new ServerPingExternal();
		} else {
			this.internalPing = new ServerPingInternal();
		}
		this.online = false;
		this.name = name;
		this.displayname = displayname;
		this.address = new InetSocketAddress(address, port);
		this.timeout = timeout;
		this.pingStartTime = System.currentTimeMillis();
		this.pingEndTime = System.currentTimeMillis();

		if (Bukkit.getServer().getIp().equals(address) && Bukkit.getServer().getPort() == Integer.valueOf(port)) {
			this.local = true;
		}
	}

	public ServerPingExternal getExternalPing() {
		return externalPing;
	}

	public void setExternalPing(ServerPingExternal externalPing) {
		this.externalPing = externalPing;
	}

	public ServerPingInternal getInternalPing() {
		return internalPing;
	}

	public void setInternalPing(ServerPingInternal internalPing) {
		this.internalPing = internalPing;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getPlayerCount() {
		return playercount;
	}

	public void setPlayerCount(int playercount) {
		this.playercount = playercount;
	}

	public int getMaxPlayers() {
		return maxplayers;
	}

	public void setMaxPlayers(int maxplayers) {
		this.maxplayers = maxplayers;
	}

	public String getMotd() {
		return motd;
	}

	public void setMotd(String motd) {
		this.motd = motd;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	public long getPingDelay() {
		return calculatePingDelay();
	}

	public void setPingStart(long pingStartTime) {
		this.pingStartTime = pingStartTime;
	}

	public void setPingEnd(long pingEndTime) {
		this.pingEndTime = pingEndTime;
	}

	public void resetPingDelay() {
		this.pingStartTime = System.currentTimeMillis();
	}

	private long calculatePingDelay() {
		long result = (pingEndTime - pingStartTime);
		return result;
	}

	public void teleportPlayer(Player p) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("Connect");
			out.writeUTF(name);
		} catch (IOException e) {
			e.printStackTrace();
			logConsole(Level.WARNING, p.getName() + ": You'll never see me!");
		}
		p.sendPluginMessage(TeleportSigns.getInstance(), "BungeeCord", b.toByteArray());

		try {
			b.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sendMsg(p, TeleportSigns.getInstance().getMsg("enter-message", "%server%", name));
	}
}
