public class LockManager {

  private int r;
  private int w;
  private Server server;
  private HashTable<String, Integer> votes;
  private int initialVotes;
  private LinkedList<Lock> locklist;

  public LockManager(int r, int w, Server server, int initalVotes) {
    this.w = w;
    this.r = r;
    this.server = server;
    this.votes = new HashTable<String, Integer>();
    this.initalVotes = initalVotes;
  }

  public boolean getLocks(LinkedList<Lock> locklist) {
    //to implement : the quorum protocol
    this.locklist = locklist;
    // identify the locks needed
    for (Lock lock : locklist) {
      votes.add(lock.getDate(), this.initalVotes);
    }

    //prepare the message to request locks
    Set keys = votes.keySet();

    Flag[] flags = new Flag[1];
    flags[0] = Flag.REQ;

    Message<Set> message = new Message<Set>(flags, keys);

    server.broadcast(message);

    // intercept message responses
    Message response = server.getServerResponseMessage();



  }

  public boolean releaseLocks(VarList values) {
    // values is a list of values which were updated in the transaction.
    // must propogate the update

  }
}
