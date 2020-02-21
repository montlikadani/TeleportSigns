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
import java.nio.charset.Charset;
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

	@Override
	@SuppressWarnings("resource")
	public SResponse fetchData() throws IOException {
		Socket socket = new Socket();

		OutputStream outputStream;
		DataOutputStream dataOutputStream;
		InputStream inputStream;
		InputStreamReader inputStreamReader;

		socket.connect(new InetSocketAddress(host.getAddress(), host.getPort()), timeout);

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
		readVarInt(dataInputStream); // size of packet

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
		String json = new String(in, Charset.forName("utf-8"));

		String version = null;
		if (ServerVersion.Version.isCurrentLower(ServerVersion.Version.v1_9_R1)) {
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

		long pingTime = dataInputStream.readLong();

		SResponse sr = new SResponse();

		if (version.contains("1.9")) {
			StatusResponse_19 stat = this.gson.fromJson(json, StatusResponse_19.class);
			sr.description = stat.getDescription();
			sr.favicon = stat.getFavicon();
			sr.players = stat.getPlayers().getOnline();
			sr.slots = stat.getPlayers().getMax();
			sr.time = stat.getTime();
			sr.protocol = stat.getVersion().getProtocol();
			sr.version = stat.getVersion().getName();
		} else if (version.contains("1.10") || version.contains("1.11") || version.contains("1.12")) {
			StatusResponse_110 stat = this.gson.fromJson(json, StatusResponse_110.class);
			sr.description = stat.getDescription();
			sr.players = stat.getPlayers().getOnline();
			sr.slots = stat.getPlayers().getMax();
			sr.time = stat.getTime();
			sr.protocol = stat.getVersion().getProtocol();
			sr.version = stat.getVersion().getName();
		} else if (version.contains("1.13") || version.contains("1.14") || version.contains("1.15")) {
			StatusResponse_113 stat = this.gson.fromJson(json, StatusResponse_113.class);
			sr.description = stat.getDescription().getText();
			sr.players = stat.getPlayers().getOnline();
			sr.slots = stat.getPlayers().getMax();
			sr.time = -1;
			sr.protocol = stat.getVersion().getProtocol();
			sr.version = stat.getVersion().getName();
		} else {
			StatusResponse stat = this.gson.fromJson(json, StatusResponse.class);
			sr.description = stat.getDescription();
			sr.favicon = stat.getFavicon();
			sr.players = stat.getPlayers().getOnline();
			sr.slots = stat.getPlayers().getMax();
			// sr.time = stat.getTime();
			sr.time = (int) (now - pingTime);
			sr.protocol = stat.getVersion().getProtocol();
			sr.version = stat.getVersion().getName();
		}

		dataOutputStream.close();
		outputStream.close();
		inputStreamReader.close();
		inputStream.close();
		socket.close();

		return sr;
	}
}
