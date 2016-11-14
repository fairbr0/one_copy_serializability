import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.ListIterator;

public class ClientServer{

  private ServerSocket serverSocket;
	private Socket socket;
  private LinkedList<String> transactions;
	private Thread serverConnectionThread;
	private Logger logger;

	public ClientServer(String loggerFilePath) throws IOException {
		//Do we need a logger for the client server?
		this.logger = new Logger(loggerFilePath, "Client"); //Here we want to document what type of server this is
		this.serverSocket = new ServerSocket();
	}

  public static void main(String args[]) {
		try {
			ClientServer cs = new ClientServer(args[0]);
	    cs.readTransactionFile(args[0]);
	    cs.processTransactions(args[1]);
		} catch(Exception e){
			e.printStackTrace();
		}
  }

  private void processTransactions(String serverAddresses) {
		try {
			System.out.println("Printing transaction");
			ListIterator<String> listIterator = transactions.listIterator();

			InetSocketAddress[] serverInformation = parseAddresses(serverAddresses);

			while(listIterator.hasNext()){
				String transactions = listIterator.next();
				String[] commands = transactions.split(" ");
				int portNumber = Integer.parseInt(commands[1].replace(";", ""));
				System.out.println("1st port number " + portNumber);
				System.out.println("Connecting to port");
				connectServer(serverInformation[portNumber], portNumber+9030);
				sendTransaction(transactions);
				System.out.println(transactions);
				System.out.println("Transaction COMPLETE");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
  }

	private void sendTransaction(String transaction) {
		try {
			ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
			Message send = new Message<String>(transaction);
			os.writeObject(send);
			os.flush();
			System.out.println("Transaction sent to the server for processing");
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public void connectServer(InetSocketAddress server, int portNumber) throws IOException {
			InetAddress address = server.getAddress();
			System.out.println("Port Number " + portNumber);
			System.out.println("Making a connection on the port");
			this.socket = new Socket(address, portNumber);
	}

	public static InetSocketAddress[] parseAddresses(String input) {
		if (input.equals("NULL")) {
			return new InetSocketAddress[0];
		}
		String[] rawAddresses = input.split(",");
		InetSocketAddress[] addresses = new InetSocketAddress[rawAddresses.length];
		for (int i = 0; i != rawAddresses.length; ++i) {
			String[] addrParts = rawAddresses[i].split(":");
			addresses[i] = new InetSocketAddress(addrParts[0], Integer.parseInt(addrParts[1]));
		}
		return addresses;
	}

  private void readTransactionFile(String serverNumber) throws FileNotFoundException, IOException {
		int fileNumber = Integer.parseInt(serverNumber);
		fileNumber-=9000;
		System.out.println(fileNumber);
		String fileName = "trans" + fileNumber + ".txt";
		System.out.println("The location for this server is: " + fileName);
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    transactions = new LinkedList<String>();
    String line = br.readLine();
    while (line != null ) {
			System.out.println("Printing line " + line);
      transactions.add(line);
      line = br.readLine();
    }
    br.close();
  }
}
