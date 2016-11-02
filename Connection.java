import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;
import java.net.Socket;

public class Connection implements Callable<Message> {
	private Socket socket;
	private ObjectOutputStream os;
	private ObjectInputStream is;
	private Message outgoingMessage;

	public Connection(Socket socket) throws IOException {
		this(socket, null);
	}

	public Connection(Socket socket, Message message) throws IOException {
		this.socket = socket;
		this.os = new ObjectOutputStream(socket.getOutputStream());
		this.is = new ObjectInputStream(socket.getInputStream());
		this.outgoingMessage = message;
	}

	public Message call() {
		Message incoming = null;
		try {
			System.err.println("writing object " + this.outgoingMessage.toString());
			os.writeObject(this.outgoingMessage);
			os.flush();
			System.err.println("Written, flushed");
			socket.setSoTimeout(5000);
			System.err.println("Have set so timeout, now reading");
			incoming = (Message)is.readObject();
			System.err.println("Successfully read " + incoming.toString());
			socket.setSoTimeout(0);
			return incoming;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return incoming;
	}

	public void setMessage(Message m) {
		this.outgoingMessage = m;
	}
}
