import java.io.IOException;
import java.net.InetSocketAddress;
import java.lang.Math;

public class Quorum {

	private int x = 55;
	private int y = 80;
	private int numberOfVotes;
	private int numberOfServers;
	private Server server;
	private boolean isClosing = false;

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
					if(){
						
					}
				}

				server.testSend();
				server.testSend();
				Thread.sleep(4000);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void processServerMessage(Message serverMessage){
		String message = (String) serverMessage.getMessage();
		System.out.println(serverMessage);
	}

	public void processClientMessage(Message clientMessage){
		String message = (String) clientMessage.getMessage();
		System.out.println(clientMessage);
	}

}
