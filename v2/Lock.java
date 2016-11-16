class Lock {

  private String data;
  private LOCK_TYPE lock;

  public Lock(String data, LOCK_TYPE lock) {
    this.lock = lock;
    this.data = data;
  }

  public String getData() {
    return this.data;
  }

  public LOCK_TYPE getLock() {
    return this.lock;
  }

  public void setLock(LOCK_TYPE lock) {
    this.lock = lock;
  }
}

enum LOCK_TYPE {
  READ,
  WRITE;
}
