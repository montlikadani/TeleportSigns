package hu.montlikadani.TeleportSigns.StatusResponse;

public class StatusResponse_113 {
	private Description description;
	private Players players;
	private Version version;

	public Description getDescription() {
		return description;
	}

	public Players getPlayers() {
		return players;
	}

	public Version getVersion() {
		return version;
	}

	public class Description {
		private String text;

		Description() {
		}

		public String getText() {
			return text;
		}
	}

	public class Players {
		private int max;
		private int online;

		Players() {
		}

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

		Version() {
		}

		public String getName() {
			return name;
		}

		public String getProtocol() {
			return protocol;
		}
	}
}
