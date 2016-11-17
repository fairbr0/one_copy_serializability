import java.util.LinkedList;
import java.util.Iterator;

public class TransactionManager {

  String transaction;
  LinkedList<String> queries;

  public TransactionManager() { }

  public LinkedList<String> setTransaction(String transaction){
    this.transaction = transaction;
    this.queries = new LinkedList<String>();

    String[] queries = transaction.split("; ");
    for (String query : queries) {
      this.queries.add(query);
    }

    return this.queries;
  }

  public LinkedList<Lock> getLockInfo() {
    LinkedList<Lock> lockInfo = new LinkedList<Lock>();

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
      }
    }

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

}
