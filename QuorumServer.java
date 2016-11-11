public class QuorumServer extends Server {

  Server server;
  Deque queue;

  public QuorumServer(String databaseFilePath) {
    this.QuorumServer(databaseFilePath, 0);
  }

  public QuorumServer(String databaseFilePath, int value) {
    server = new Server(databaseFilePath, value);
    queue = new ArrayDeque<Transaction>();
  }

  public void requestWrite() { }

}
