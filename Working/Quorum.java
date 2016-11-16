import java.io.IOException;
import java.net.InetSocketAddress;
import java.lang.Math;
import java.util.Iterator;
import java.util.LinkedList;

public class Quorum {

	private int read = 10;
	private int write = 91;
	private int numberOfVotes;
	private int numberOfServers;
	private Server server;
	private boolean isClosing = false;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if (args.length < 1) {
			help();
		}
		Quorum qs = new Quorum();
		String program = args[0];
		switch (program) {
			case "server": qs.serverMain(args); break;
			default: System.err.println("Unknown program type '" + program + "'"); break;
		}
	}

	private void serverMain(String[] args) throws IOException, ClassNotFoundException {
		if (args.length != 5) {
			help();
		}
		String databasePath = args[4]; // The path to the database file
		server = new Server(databasePath);	//Each server needs to create an instance of a server with a database for each.
		int serverListenPort = Integer.parseInt(args[1]); // The port that this server should listen on to accept connections from other servers
		int clientListenPort = Integer.parseInt(args[2]); // The port that this server should listen on to accept connections from clients
		InetSocketAddress[] otherServerAddresses = Server.parseAddresses(args[3]); // The addresses of the other servers
		numberOfServers = otherServerAddresses.length +1;
		server.log("The number of other servers in the network is " + numberOfServers);
		numberOfVotes = Math.round(100 / numberOfServers);
		server.log("The number of votes that this server has is " + numberOfVotes);

		//updateVotes(numberOfVotes);

		try {
			//Create a new server socket that we can then use to connect to other servers
			server.acceptServers(serverListenPort);
			server.log("Accepting servers on " + serverListenPort);
			server.acceptClients(clientListenPort);
			server.connectServers(otherServerAddresses, Integer.parseInt(args[4]));

			//Main Loop for protocol:
			runLoop();

		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace(System.err);
    } catch (Exception x) {
      x.printStackTrace();

		} finally {
			server.log("System Closed");
			this.close();
		}
	}

	public void close() {
		this.isClosing = true;
		server.close();
	}

	public boolean isClosing(){
		return this.isClosing;
	}

	public void runLoop(){
		try {
			System.out.println(isClosing());
			while(!isClosing()){
				Message serverMessage = server.getServerMessage();
				if (serverMessage != null) {
					server.log("<quorum> message recieved from a server " + serverMessage.toString());
					processServerMessage(serverMessage);
				}
				Message clientMessage = server.getClientMessage();
				if (clientMessage != null) {
					server.log("<quorum> message recieved from a client " + clientMessage.toString());
					processClientMessage(clientMessage);

				}
				Thread.sleep(100);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void processServerMessage(Message serverMessage){
		//System.out.println("We have got a message from a client!!! Woop Woop - This shit works");
		String message = (String) serverMessage.getMessage();
		String[] messageParts = message.split(" ");
		System.out.println("The message received is: " + message);
		if(messageParts[0] == "Send votes") {
			//The server must have which server it is in the message to send a response back to
		} else if(messageParts[0] == "Return votes") {
			//The server must have which server it is in the message to send a response back to
		} else if(messageParts[0] == "Write Acknoledge") {

		} else {
			//ABORT
		}

		System.out.println(serverMessage);
	}

	public void processClientMessage(Message clientMessage){
		String message = (String) clientMessage.getMessage();
		System.out.println(clientMessage);
		//begin T1; read x; write x; commit T1;
		String[] trans = message.split("; ");
		String transactionNumber = trans[0].split(" ")[1];
		System.out.println("The transaction Number is "+ transactionNumber);

		//Processing the transactionNumber
		LinkedList requests = new LinkedList();
		if (((trans[0].split(" "))[0]).equals("begin")){
			for(int i=1; i<trans.length; i++){
				//System.out.println(trans[i]);
				//System.out.println(trans[i].split(" ")[0]);
				if (trans[i] == "" || trans[i] == " "){
										continue;
				} else if ((trans[i].split(" "))[0].equals("commit")) {
					System.out.println("Commit");
					System.out.println("The final thing should be commit maybe??" + trans[i]);
				} else {
					System.out.println("Send this process this command " + trans[i]);
					requests.add(trans[i]);
				}
			}

			int maxVotes = processRequestsList(requests);
			System.out.println("The max number of votes needed for this transaction is " + maxVotes);
			getVotes(maxVotes);
		}
	}

	private void getVotes(int noVotesRequired) {
		System.out.println("Getting votes from dem bitches");
		server.transmitMessage("Send Votes");
	}

	private int processRequestsList(LinkedList<String> requests) {
		int maxVotes = 0;
		Iterator<String> it = requests.iterator();
		System.out.println("Processing the list of requests");
		while (it.hasNext()) {
			String r = it.next();
			String[] rsplit = r.split(" ");
			if(rsplit[0].equals("read")){
				maxVotes = java.lang.Math.max(maxVotes, this.read);
				System.out.println("A read has been read");
			} else if(rsplit[0].equals("write")){
				maxVotes = java.lang.Math.max(maxVotes, this.write);
				System.out.println("A write has been read");
			}
		}

		return maxVotes;
	}


	public static void help() {
		System.out.println("Usage:");
		System.out.println("\tjava Node server <server port> <client port> <other servers> <database path>");
		System.out.println("\t\t<server port>: The port that the server listens on for other servers");
		System.out.println("\t\t<client port>: The port that the server listens on for clients");
		System.out.println("\t\t<other servers>: A comma separated list of the other server's addresses and ports");
		System.out.println("\t\t\tExamples:");
		System.out.println("\t\t\t - 'localhost:9001'");
		System.out.println("\t\t\t - 'localhost:9001,127.0.0.1:9002'");
		System.out.println("\t\t<database path>: The path to the database file, this needs to be unique per server");
		System.out.println("\t\t\tExamples:");
		System.out.println("\t\t\t - 'db1.txt'");
		System.exit(1);
	}
}
