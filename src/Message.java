import java.io.Serializable;

public class Message<T> implements Serializable {
	private T message;
	Flag[] flags;
	int serverNumber;

	public Message(Flag[] flags, T message, int serverNumber) {
		this.flags = flags;
		this.message = message;
		this.serverNumber = serverNumber;
	}

	public T getMessage() {
		return message;
	}

	public Flag[] getFlags() {
		return this.flags;
	}

	public String toString() {
		return message.toString() + " from server " + Integer.toString(serverNumber);
	}
}
