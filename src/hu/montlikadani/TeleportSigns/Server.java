package hu.montlikadani.TeleportSigns;

import java.net.InetSocketAddress;

public interface Server {

	public void setAddress(InetSocketAddress host);

	public InetSocketAddress getAddress();

	public void setTimeout(int timeout);

	public int getTimeout();

	public boolean isFetching();

	public void setFetching(boolean fetching);
}
