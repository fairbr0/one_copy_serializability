import java.util.LinkedList;
import java.io.IOException;
import java.io.*;

class Site {

  Server server;
  DataManager dm;
  LockManager lm;
  TransactionManager tm;
  int r;
  int w;
  LinkedList<String> transactions;
  Logger logger;
  private boolean isClosing = false;

  public static void main(String[] args) throws IOException {
    Site site = new Site(args);
  }

  public Site(String args[]) throws IOException{

    // get the arguments
    Integer serverNumber = Integer.parseInt(args[0]);
    this.logger = new Logger(serverNumber);
    this.server = new Server(serverNumber, logger);
    int port = 9030 + serverNumber;

    // set quorum values and validate them
    this.r = Integer.parseInt(args[1]);
    this.w = Integer.parseInt(args[2]);
    Boolean correctness = validateQuroumValues(Integer.parseInt(args[3]));

    if(!correctness) {
      throw new MattBradburyException();
    }

    // data manager class performs actions on the database.
    // lock manager handles locking etc.
    this.dm = new DataManager(serverNumber, logger, this.server);
    this.lm = new LockManager(r, w, this.server, 1, serverNumber, logger);
    this.tm = new TransactionManager(logger);

    this.readTransactionFile(serverNumber);

    try {
      this.server.acceptServers(port);
      this.server.connectServers(Server.parseAddresses(args[4]), serverNumber);
    } catch (IOException e) {
      e.printStackTrace();
    }
		try {
      Thread.sleep(3000);
			//this.server.printWrappedServers();
    	this.listenServerMessages();
    	this.processTransactions();
	  } catch (InterruptedException e) {
		  e.printStackTrace();
	  }
  }

  private boolean validateQuroumValues(int numberOfServers) {
    int sumRW = this.r + this.w;
    if (sumRW <= numberOfServers) {
      System.err.println("The values that you have picked for r and w are not valid");
      System.exit(1);

    }
    return true;
  }

  //method to listen to incomming messages from the server
  public void listenServerMessages() throws IOException {
    lm.processRequestMessages();
  }

  //method to process the list of transactions.
  public void processTransactions() throws IOException {
    for (int i = 0; i < this.transactions.size(); i++) {
      String transaction = this.transactions.get(i);
      //set transaction on the manager
      LinkedList<String> queries = tm.setTransaction(transaction);

      //get the information on needed locks
      LinkedList<Lock> locks = tm.getLockInfo();

      //aquire the locks
      boolean gotLocks = lm.getLocks(locks);

      // make the changes
      if (gotLocks) {
        dm.setTransaction(queries);
        boolean release = dm.runTransaction();

        if (release) lm.releaseLocks();
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

  public void readTransactionFile(int serverNumber) throws IOException {
    try {
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
    } catch (FileNotFoundException e) {
      System.out.println("No transactions to process, listening");
      this.transactions = new LinkedList<String>();
    }
  }


}

class MattBradburyException extends RuntimeException {
  public MattBradburyException(){
       super("I'm dissapointed in you");
   }

   public MattBradburyException(String message){
       super(message);
   }
}
