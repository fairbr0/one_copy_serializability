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
	private static int lol = 1;

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
			System.err.println("writing {" + this.outgoingMessage.toString() + "}");
			os.writeObject(this.outgoingMessage);
			os.flush();
			socket.setSoTimeout(5000);
			try {
				int time = (Integer)this.outgoingMessage.getMessage() * 4000;
				System.err.println("Sleeping for " + time + " ms");
				Thread.sleep(time);
				System.err.println("Awake");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
