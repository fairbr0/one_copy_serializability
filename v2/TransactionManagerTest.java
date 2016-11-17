import java.util.LinkedList;

public class TransactionManagerTest {

  public static void main(String args[]) {
    TransactionManagerTest tests = new TransactionManagerTest();
    tests.setTransactionTest();
    tests.getLockInfoReadTest();
    tests.getLockInfoWriteTest();
    tests.updateLocksReadReadTest();
    tests.updateLocksReadWriteTest();
    tests.updateLocksWriteReadTest();
  }

  void setTransactionTest() {
    String s = "Begin T1; read x; x = 20; write x; commit T1";
    TransactionManager tm = new TransactionManager();
    tm.setTransaction(s);
    LinkedList<String> queries = tm.queries;

    for (String query : queries) {
      System.out.println(query);
    }
  }

  void getLockInfoWriteTest() {
    String s = "Begin T1; read x; x = 20; write x; commit T1";
    TransactionManager tm = new TransactionManager();
    tm.setTransaction(s);
    LinkedList<Lock> locks = tm.getLockInfo();

    for (Lock lock : locks) {
      System.out.println(lock.getData() + " " + lock.getLock());
    }
  }

  void getLockInfoReadTest() {
    String s = "Begin T1; read x; x = 20; commit T1";
    TransactionManager tm = new TransactionManager();
    tm.setTransaction(s);
    LinkedList<Lock> locks = tm.getLockInfo();

    for (Lock lock : locks) {
      System.out.println(lock.getData() + " " + lock.getLock());
    }
  }

  void updateLocksReadReadTest() {
    LinkedList<Lock> list = new LinkedList<Lock>();
    Lock l = new Lock("X", LOCK_TYPE.READ);
    list.add(l);

    TransactionManager tm = new TransactionManager();
    tm.updateLocks(list, "X", LOCK_TYPE.READ);

    for (Lock m : list) {
      System.out.println("Data Item: " + m.getData() + " Lock: " + m.getLock());
    }
  }

  void updateLocksReadWriteTest() {
    LinkedList<Lock> list = new LinkedList<Lock>();
    Lock l = new Lock("X", LOCK_TYPE.READ);
    list.add(l);

    TransactionManager tm = new TransactionManager();
    tm.updateLocks(list, "X", LOCK_TYPE.WRITE);

    for (Lock m : list) {
      System.out.println("Data Item: " + m.getData() + " Lock: " + m.getLock());
    }
  }

  void updateLocksWriteReadTest() {
    LinkedList<Lock> list = new LinkedList<Lock>();
    Lock l = new Lock("X", LOCK_TYPE.WRITE);
    list.add(l);

    TransactionManager tm = new TransactionManager();
    tm.updateLocks(list, "X", LOCK_TYPE.READ);

    for (Lock m : list) {
      System.out.println("Data Item: " + m.getData() + " Lock: " + m.getLock());
    }
  }
}
