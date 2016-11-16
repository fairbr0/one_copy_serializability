class Site {

  Server server;
  DataManager dm;
  LockManager lm;
  TransactionManager tm;
  int r;
  int w;
  LinkedList<String> transactions;
  private boolean isClosing = false;

  public static void main(String[] args) {
    Site site = new Site(args);
  }
  public Site(String args[]) {

    // get the arguments
    String databasePath;
    Integer serverNumber;
    Integer port;
    this.server = new Server(*argument*);

    // set quorum values and validate them
    this.r = Integer.parseInt(*argument*);
    this.w = Integer.parseInt(*argument*);
    this.validateQuroumValues(*number of servers*);

    // data manager class performs actions on the database.
    // lock manager handles locking etc.
    this.dm = new DataManager(databasePath);
    this.lm = new LockManager(r, w);
    this.tm = new TransactionManager();

    this.readTransactionFile(*argument*);

    try {
      this.server.connectServers();
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.listenServerMessages();
    this.processTransactions();

  }

  //method to listen to incomming messages from the server
  public void listenServerMessages();

  //method to process the list of transactions.
  public void processTransactions() {
    for (int i = 0; i < this.transactions.length; i++) {
      String transaction = this.transactions[i];
      //set transaction on the manager
      tm.setTransaction(transaction);

      //get the information on needed locks
      LinkedList<Lock> locks = tm.getLockInfo();

      //aquire the locks
      boolean gotLocks = lm.getLocks(locks);

      // make the changes
      if (gotLocks) {
        dm.setTransaction(transaction);
        VarList values = dm.runTransaction();

        lm.releaseLocks(values);
      } else {
        //super nasty for loop ahhaha
        i--;
        continue;
      }
    }
  }

  public void close() {
    this.isClosing = true;
  }

  public boolean isClosing() {
    return isClosing;
  }

  public void readTransactionFile(int serverNumber) {
    String filePath = "trans" + serverNumber + ".txt";
    BufferedReader br = new BufferedReader(new FileReader(filePath));
    LinkedList<String> transactions = new LinkedList<String>();
    String line = br.readLine();
    while (line != null) {
      transactions.add(line);
      line = br.readLine();
    }
    br.close();
    this.transactions = transactions;
  }


}
