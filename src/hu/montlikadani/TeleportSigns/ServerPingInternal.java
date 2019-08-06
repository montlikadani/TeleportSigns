package hu.montlikadani.TeleportSigns;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import hu.montlikadani.TeleportSigns.StatusResponse.StatusResponse_110;
import hu.montlikadani.TeleportSigns.StatusResponse.StatusResponse_113;
import hu.montlikadani.TeleportSigns.StatusResponse.StatusResponse_19;

public class ServerPingInternal implements Server {
	private boolean fetching;
	private InetSocketAddress host;
	private int timeout = 2000;
	private Gson gson = new Gson();
	private Socket socket = new Socket();

	@Override
	public void setAddress(InetSocketAddress host) {
		this.host = host;
		this.fetching = false;
	}

	@Override
	public InetSocketAddress getAddress() {
		return host;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public boolean isFetching() {
		return fetching;
	}

	@Override
	public void setFetching(boolean fetching) {
		this.fetching = fetching;
	}

	private int readVarInt(DataInputStream in) throws IOException {
		int i = 0;
		int j = 0;

		byte k;
		do {
			k = in.readByte();
			i |= (k & 127) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
		} while ((k & 128) == 128);

		return i;
	}

	private void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
		while ((paramInt & -128) != 0) {
			out.writeByte(paramInt & 127 | 128);
			paramInt >>>= 7;
		}

		out.writeByte(paramInt);
	}

	public SResponse fetchData() throws IOException {
		socket.close();
		socket = new Socket();
		OutputStream outputStream;
		DataOutputStream dataOutputStream;
		InputStream inputStream;
		InputStreamReader inputStreamReader;

		socket.setSoTimeout(timeout);
		socket.connect(new InetSocketAddress(host.getAddress(), host.getPort()), timeout);
		socket.shutdownInput();

		outputStream = socket.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);

		inputStream = socket.getInputStream();
		inputStreamReader = new InputStreamReader(inputStream);

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream handshake = new DataOutputStream(b);
		handshake.writeByte(0x00);
		writeVarInt(handshake, 47); // old 4
		writeVarInt(handshake, host.getHostString().length());
		handshake.writeBytes(host.getHostString());
		handshake.writeShort(host.getPort());
		writeVarInt(handshake, 1);

		writeVarInt(dataOutputStream, b.size());
		dataOutputStream.write(b.toByteArray());

		dataOutputStream.writeByte(0x01);
		dataOutputStream.writeByte(0x00);
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		//int size = readVarInt(dataInputStream);
		int id = readVarInt(dataInputStream);

		if (id == -1) {
			throw new IOException("Premature end of stream.");
		}

		if (id != 0x00) {
			throw new IOException("Invalid packetID.");
		}

		int length = readVarInt(dataInputStream);

		if (length == -1) {
			throw new IOException("Premature end of stream.");
		}

		if (length == 0) {
			throw new IOException("Invalid string length.");
		}

		byte[] in = new byte[length];
		dataInputStream.readFully(in);
		String json = new String(in);

		String version = null;
		if (org.bukkit.Bukkit.getVersion().contains("1.8")) {
			JSONObject jsonMother = new JSONObject();
			JSONParser parser = new JSONParser();

			try {
				jsonMother = (JSONObject) parser.parse(json);
			} catch (ParseException e) {
				Logger.getLogger(ServerPingInternal.class.getName()).log(Level.SEVERE, (String) null, e);
			}

			JSONObject jsonVersion = (JSONObject) jsonMother.get("version");
			version = (String) jsonVersion.get("name");
		} else {
			JsonObject jsonMother = new JsonObject();

			try {
				jsonMother = gson.fromJson(json, JsonObject.class);
			} catch (JsonSyntaxException e) {
				Logger.getLogger(ServerPingInternal.class.getName()).log(Level.SEVERE, (String) null, e);
			}

			JsonObject jsonVersion = (JsonObject) jsonMother.get("version");
			version = jsonVersion.get("name").toString();
		}
		SResponse ret = new SResponse();

		long now = System.currentTimeMillis();
		dataOutputStream.writeByte(0x09);
		dataOutputStream.writeByte(0x01);
		dataOutputStream.writeLong(now);

		readVarInt(dataInputStream);
		id = readVarInt(dataInputStream);
		if (id == -1) {
			throw new IOException("Premature end of stream.");
		}

		if (id != 0x01) {
			throw new IOException("Invalid packetID.");
		}
		//long pingtime = dataInputStream.readLong();

		if (version.contains("1.9")) {
			StatusResponse_19 res = this.gson.fromJson(json, StatusResponse_19.class);
			ret.description = res.getDescription();
			ret.favicon = res.getFavicon();
			ret.players = res.getPlayers().getOnline();
			ret.slots = res.getPlayers().getMax();
			ret.time = res.getTime();
			ret.protocol = res.getVersion().getProtocol();
			ret.version = res.getVersion().getName();
		} else if (!version.contains("1.10") && !version.contains("1.11") && !version.contains("1.12")) {
			if (version.contains("1.13") || version.contains("1.14")) {
				StatusResponse_113 res = this.gson.fromJson(json, StatusResponse_113.class);
				ret.description = res.getDescription().getText();
				ret.players = res.getPlayers().getOnline();
				ret.slots = res.getPlayers().getMax();
				ret.time = -1;
				ret.protocol = res.getVersion().getProtocol();
				ret.version = res.getVersion().getName();
			} else {
				StatusResponse res = this.gson.fromJson(json, StatusResponse.class);
				ret.description = res.getDescription();
				ret.favicon = res.getFavicon();
				ret.players = res.getPlayers().getOnline();
				ret.slots = res.getPlayers().getMax();
				ret.time = res.getTime();
				ret.protocol = res.getVersion().getProtocol();
				ret.version = res.getVersion().getName();
			}
		} else {
			StatusResponse_110 res = this.gson.fromJson(json, StatusResponse_110.class);
			ret.description = res.getDescription();
			ret.players = res.getPlayers().getOnline();
			ret.slots = res.getPlayers().getMax();
			ret.time = res.getTime();
			ret.protocol = res.getVersion().getProtocol();
			ret.version = res.getVersion().getName();
		}

		dataOutputStream.close();
		outputStream.close();
		inputStreamReader.close();
		inputStream.close();
		socket.close();

		return ret;
	}

	public class SResponse {
		public String version;
		public String protocol;
		public String favicon;
		public String description;
		public int players;
		public int slots;
		public int time;
	}

	public class StatusResponse {
		private String description;
		private Players players;
		private Version version;
		private String favicon;
		private int time;

		public String getDescription() {
			return description;
		}

		public Players getPlayers() {
			return players;
		}

		public Version getVersion() {
			return version;
		}

		public String getFavicon() {
			return favicon;
		}

		public int getTime() {
			return time;
		}

		public void setTime(int time) {
			this.time = time;
		}
	}

	public class Players {
		private int max;
		private int online;
		private List<Player> sample;

		public int getMax() {
			return max;
		}

		public int getOnline() {
			return online;
		}

		public List<Player> getSample() {
			return sample;
		}
	}

	public class Player {
		private String name;
		private String id;

		public String getName() {
			return name;
		}

		public String getId() {
			return id;
		}
	}

	public class Version {
		private String name;
		private String protocol;

		public String getName() {
			return name;
		}

		public String getProtocol() {
			return protocol;
		}
	}
}
