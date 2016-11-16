import java.io.Serializable;

public class Message<T> implements Serializable {
	private T message;
	Flag[] flags;

	public Message(Flag[] flags, T message) {
		this.flags = flags;
		this.message = message;
	}

	public T getMessage() {
		return message;
	}

	public Flag[] getFlags() {
		return this.flags;
	}

	public String toString() {
		return message.toString();
	}
}
