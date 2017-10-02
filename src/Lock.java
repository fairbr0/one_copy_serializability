import java.io.Serializable;

class Lock implements Serializable {

  private String data;
  private LOCK_TYPE lock;
	private Flag flag;

  public Lock(String data, LOCK_TYPE lock) {
    this.lock = lock;
    this.data = data;
  }

  public String getData() {
    return this.data;
  }

	public Flag getFlag() {
		return this.flag;
	}

  public LOCK_TYPE getLock() {
    return this.lock;
  }

  public void setLock(LOCK_TYPE lock) {
    this.lock = lock;
  }

  public String toString() {
    return data + " " +lock;
  }
}

enum LOCK_TYPE {
  READ,
  WRITE;
}
