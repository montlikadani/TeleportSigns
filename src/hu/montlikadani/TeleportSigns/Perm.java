package hu.montlikadani.TeleportSigns;

public enum Perm {
	HELP("teleportsigns.help"),
	RELOAD("teleportsigns.reload"),
	DESTROY("teleportsigns.destroy"),
	CREATE("teleportsigns.create"),
	NOCOOLDOWN("teleportsigns.use.nocooldown"),
	USE("teleportsigns.use"),
	LISTLAYOUT("teleportsigns.listlayouts"),
	LISTSERVER("teleportsigns.listservers"),
	CONNECT("teleportsigns.connect"),
	EDITSIGN("teleportsigns.editsign");

	private String perm;

	Perm(String perm) {
		this.perm = perm;
	}

	public String getPerm() {
		return perm;
	}
}
