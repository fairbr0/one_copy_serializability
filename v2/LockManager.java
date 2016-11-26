import java.io.IOException;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.lang.Object.*;

public class LockManager {

  private final int r;
  private final int w;
  private Server server;
  private final int initalVotes;
  private HashMap<String, Integer> votes;
  private LinkedList<Lock> locklist;
  private int serverNumber;
  private Logger logger;
  private boolean softlock;
  private boolean hardlock;
  private int transactionNumber = -1;

  public LockManager(int r, int w, Server server, int initalVotes, int serverNumber, Logger logger) {
    this.w = w;
    this.r = r;
    this.server = server;
    this.initalVotes = initalVotes;
    this.serverNumber = serverNumber;
    this.votes = new HashMap<String, Integer>();
    this.logger = logger;
		this.softlock = false;
    this.hardlock = false;
  }

  public void setInitialVoteList() {
    for (Lock l : this.locklist) {
			if (!this.votes.containsKey(l.getData())) {
				this.votes.put(l.getData(), this.initalVotes);
			}
    }
  }

  public LinkedList<Lock> createServerMessageLockList() {
    LinkedList<Lock> tempLockList = new LinkedList<Lock>();
    log("The size of the tempLockList is " + Integer.toString(locklist.size()));
    Iterator<Lock> it = this.locklist.iterator();
    while (it.hasNext()){
      //log("At position " + k);
      Lock item = it.next();
      String itemName = item.getData();		// x or y
      LOCK_TYPE lockType = item.getLock();	//Lock Type
      if(lockType == LOCK_TYPE.READ) {
        if (this.votes.get(itemName) < r) {
          tempLockList.add(item);
          log("item added");
          log(Integer.toString(locklist.size()));
        }
      } else if (lockType == LOCK_TYPE.WRITE) {
        if (this.votes.get(itemName) < w) {
          tempLockList.add(item);
          log("item added");
          log(Integer.toString(locklist.size()));
        }
      } else {
        throw new MattBradburyException();
      }
    }
    log("List of locks to aquire completed");
    return tempLockList;
  }

  public boolean getLocks(LinkedList<Lock> locklist, int transactionNumber) {
    this.locklist = locklist;
    this.transactionNumber = transactionNumber;
    setInitialVoteList();
    aquireLock();
    this.hardlock = true;
    return true;
  }

  //method with all the aquisition logic
  private void aquireLock() {
    this.softlock = true;
    while (!checkVotes) {
      for (int i = 0; i < server.numOfServers; i++) {

        LinkedList<Lock> newLockList = createServerMessageLockList();
        //get the actual number of server your talking too
        int k = (i >= this.serverNumber) ? i + 1 : i;


      }
    }
  }

  private void log(String message) {
    try {
      System.out.println(message);
		  logger.writeLog(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
