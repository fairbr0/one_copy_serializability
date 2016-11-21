import java.net.*;
import java.io.*;

public class SocketWrapper {

	private final Socket socket;
	private final int serverNumber;

	public SocketWrapper(Socket socket, int serverNumber) {
		this.socket = socket;
		this.serverNumber = serverNumber;
	}

	public int getServerNumber() {
		return this.serverNumber;
	}

	public Socket getSocket() {
		return this.socket;
	}
}
