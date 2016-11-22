import java.util.LinkedList;
import java.io.IOException;

public class TransactionManagerTest {

  public static void main(String args[]) throws IOException  {
    TransactionManagerTest tests = new TransactionManagerTest();
    tests.setTransactionTest();
    tests.getLockInfoReadTest();
    tests.getLockInfoWriteTest();
    tests.updateLocksReadReadTest();
    tests.updateLocksReadWriteTest();
    tests.updateLocksWriteReadTest();
  }

  void setTransactionTest() throws IOException {
    String s = "Begin T1; read x; x = 20; write x; commit T1";
    TransactionManager tm = new TransactionManager(new Logger(0));
    tm.setTransaction(s);
    LinkedList<String> queries = tm.queries;

    for (String query : queries) {
      System.out.println(query);
    }
  }

  void getLockInfoWriteTest() throws IOException ccc{
    String s = "Begin T1; read x; x = 20; write x; commit T1";
    TransactionManager tm = new TransactionManager(new Logger(0));
    tm.setTransaction(s);
    LinkedList<Lock> locks = tm.getLockInfo();

    for (Lock lock : locks) {
      System.out.println(lock.getData() + " " + lock.getLock());
    }
  }

  void getLockInfoReadTest() throws IOException {
    String s = "Begin T1; read x; x = 20; commit T1";
    TransactionManager tm = new TransactionManager(new Logger(0));
    tm.setTransaction(s);
    LinkedList<Lock> locks = tm.getLockInfo();

    for (Lock lock : locks) {
      System.out.println(lock.getData() + " " + lock.getLock());
    }
  }

  void updateLocksReadReadTest() throws IOException {
    LinkedList<Lock> list = new LinkedList<Lock>();
    Lock l = new Lock("X", LOCK_TYPE.READ);
    list.add(l);

    TransactionManager tm = new TransactionManager(new Logger(0));
    tm.updateLocks(list, "X", LOCK_TYPE.READ);

    for (Lock m : list) {
      System.out.println("Data Item: " + m.getData() + " Lock: " + m.getLock());
    }
  }

  void updateLocksReadWriteTest() throws IOException {
    LinkedList<Lock> list = new LinkedList<Lock>();
    Lock l = new Lock("X", LOCK_TYPE.READ);
    list.add(l);

    TransactionManager tm = new TransactionManager(new Logger(0));
    tm.updateLocks(list, "X", LOCK_TYPE.WRITE);

    for (Lock m : list) {
      System.out.println("Data Item: " + m.getData() + " Lock: " + m.getLock());
    }
  }

  void updateLocksWriteReadTest() throws IOException {
    LinkedList<Lock> list = new LinkedList<Lock>();
    Lock l = new Lock("X", LOCK_TYPE.WRITE);
    list.add(l);

    TransactionManager tm = new TransactionManager(new Logger(0));
    tm.updateLocks(list, "X", LOCK_TYPE.READ);

    for (Lock m : list) {
      System.out.println("Data Item: " + m.getData() + " Lock: " + m.getLock());
    }
  }
}
