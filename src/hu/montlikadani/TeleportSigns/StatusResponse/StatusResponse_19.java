package hu.montlikadani.TeleportSigns.StatusResponse;

import java.util.List;

public class StatusResponse_19 {
	private Players players;
	private Version version;
	private String favicon;
	private Description description;
	private int time;

	public Players getPlayers() {
		return players;
	}

	public String getDescription() {
		return description.getText();
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

	public class Description {
		private String text;

		public String getText() {
			return text;
		}
	}

	public class Players {
		private int max;
		private int online;
		private List<StatusResponse_19.Player> sample;

		public int getMax() {
			return max;
		}

		public int getOnline() {
			return online;
		}

		public List<StatusResponse_19.Player> getSample() {
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
