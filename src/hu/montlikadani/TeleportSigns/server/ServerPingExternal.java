package hu.montlikadani.TeleportSigns.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServerPingExternal implements Server {

	private boolean fetching;
	private InetSocketAddress host;
	private int timeout = 2000;

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

	@Override
	public SResponse fetchData() throws IOException {
		String jsonString = null;

		try {
			String url = String.format("https://api.minetools.eu/ping/%s/%s", host.getAddress().getHostAddress(), host.getPort());

			HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

			connection.setRequestProperty("User-Agent", "Mozilla/5.0");

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			in.close();

			if (connection.getResponseCode() != 200) {
				throw new IOException("Error while pinging API, HTTP error code " + connection.getResponseCode());
			}

			jsonString = response.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(jsonString).getAsJsonObject();

		if (json.has("error")) {
			return null;
		}

		SResponse ret = new SResponse();

		ret.description = json.get("description").getAsString();
		ret.time = (int) json.get("latency").getAsDouble();

		JsonObject players = json.get("players").getAsJsonObject();
		ret.players = players.get("online").getAsInt();
		ret.slots = players.get("max").getAsInt();

		JsonObject version = json.get("version").getAsJsonObject();
		ret.version = version.get("name").getAsString();
		ret.protocol = version.get("protocol").getAsString();
		return ret;
	}
}
