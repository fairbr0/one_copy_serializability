import java.util.LinkedList;
import java.util.Iterator;
import java.io.IOException;

public class TransactionManager {

  String transaction;
  LinkedList<String> queries;
  Logger logger;
	int transactionNumber;

  public TransactionManager(Logger logger) {
    this.logger = logger;
  }

  public LinkedList<String> setTransaction(String transaction){
    this.transaction = transaction;
    this.queries = new LinkedList<String>();

    String[] queries = transaction.split("; ");
    for (String query : queries) {
      this.queries.add(query);
    }

    return this.queries;
  }

	public int getTransactionNumber() {
		return this.transactionNumber;
	}

  public LinkedList<Lock> getLockInfo() {
    LinkedList<Lock> lockInfo = new LinkedList<Lock>();

    log("<TM> Discovering locks to get in 2PL");
    // get the parts of the query;
    for (String query : this.queries) {
      String[] parts = query.split(" ");
      if (parts[0].equals("read")) {
        String data = parts[1];
        LOCK_TYPE lock = LOCK_TYPE.READ;
        updateLocks(lockInfo, data, lock);
      } else if (parts[0].equals("write")) {
        String data = parts[1];
        LOCK_TYPE lock = LOCK_TYPE.WRITE;
        updateLocks(lockInfo, data, lock);
      } else if(parts[0].equals("begin")) {
				String number = parts[1].replace("T", "");
				this.transactionNumber = Integer.parseInt(number);
				log("<TM> The transaction number is " + this.transactionNumber);
			}
    }

    log("<TM> Locks required: " + lockInfo.toString());
    return lockInfo;

  }

  public void updateLocks(LinkedList<Lock> locks, String data, LOCK_TYPE lock) {
    Iterator<Lock> it = locks.iterator();
    Lock l;
    while (it.hasNext()) {
      l = it.next();

      // the data item has already been seen, has a lock
      if (l.getData().equals(data)) {

        // if either existing lock or new is write, set as write.
        if (l.getLock() == LOCK_TYPE.WRITE || lock == LOCK_TYPE.WRITE) {
          l.setLock(LOCK_TYPE.WRITE);
        } else { // neither lock is write, set as read;
          l.setLock(LOCK_TYPE.READ);
        }

        return;
      }
    }

    // data item does not exist in the lock list
    l = new Lock(data, lock);
    locks.add(l);
    return;
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
