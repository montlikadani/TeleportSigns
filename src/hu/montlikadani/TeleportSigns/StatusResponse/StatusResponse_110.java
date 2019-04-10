package hu.montlikadani.TeleportSigns.StatusResponse;

public class StatusResponse_110 {
	private Players players;
	private Version version;
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

		public int getMax() {
			return max;
		}

		public int getOnline() {
			return online;
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
