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
	private Boolean flag;

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
			while (true) {

			}
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
				connectServer(serverInformation[portNumber-1], portNumber+9000);
				sendTransaction(transactions);
				flag=false;
				System.out.println(transactions);
				System.out.println("Transaction COMPLETE");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
  }

	private void sendTransaction(String transaction) {
		while (flag) {
			try {
				if (this.socket == null) {
					System.out.println("Why the fuck is the socket null");
				}

				ObjectOutputStream os = new ObjectOutputStream(this.socket.getOutputStream());
				Message send = new Message<String>(transaction);
				//Message send = new Message<String>("Jarred is a cunt");
				os.writeObject(send);
				os.flush();
				flag=false;
				System.out.println("Transaction sent to the server for processing");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void connectServer(InetSocketAddress server, int portNumber) throws IOException, InterruptedException {
			InetAddress address = server.getAddress();
			System.out.println("Port Number " + portNumber);
			System.out.println("Making a connection on the port");
      int count = 0;
      while (count < 10) {
				System.out.println("Before the try catch block");
        try {
          this.socket = new Socket(address, portNumber);
					System.out.println("connection made");
					break;
        } catch (Exception e) {
					System.out.println(count);
          count += 1;
					Thread.sleep(1000);
        } finally {
					System.out.println("Finally");
					flag=true;
				}
      }
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
