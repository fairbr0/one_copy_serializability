public class ClientServer{

  private ArrayList<String> transactions;

  public static void main(String args[]) {
    ClientServer cs = new ClientServer();
    cs.readTransactionFile(args[1]);
    cs.processTransactions();
  }

  private void processTransactions() {
    for (String line = transactions)
  }

  private void readTransactionFile(int serverNumber) {
    BufferedReader br = new BufferedReader(new File("trans" + path + ".txt");
    transactions = new ArrayList<String>();
    String line = br.readLine();
    while (line != null ) {
      transactions.add(line);
      line = br.readLine();
    }
    br.close();
  }

}
