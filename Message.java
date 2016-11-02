import java.io.Serializable;

public class Message<T> implements Serializable {
	private T message;

	public Message(T message) {
		this.message = message;
	}

	public T getMessage() {
		return message;
	}

	public String toString() {
		return message.toString();
	}
}
