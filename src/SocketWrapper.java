import java.net.*;
import java.io.*;

public class SocketWrapper {

	private final Socket socket;
	private final int serverNumber;
	private  ObjectOutputStream outputStream;

	public SocketWrapper(Socket socket, int serverNumber, ObjectOutputStream out) {
		this.socket = socket;
		this.serverNumber = serverNumber;
		this.outputStream = out;
	}

	public ObjectOutputStream getObjectOutputStream() {
		try {
			if (this.outputStream == null) {
				this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
			}
			return this.outputStream;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getServerNumber() {
		return this.serverNumber;
	}

	public Socket getSocket() {
		return this.socket;
	}

}
