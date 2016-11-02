//package cs347;

import java.io.IOException;
import java.net.ConnectException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.TimeUnit;

public class runServer{

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
        	String program = args[0];
        	switch (program) {
            		case "server": serverMain(args); break;
            		default: System.err.println("Unknown program type '" + program + "'"); break;
        	}
    	}
    


	private static void serverMain(String[] args) throws IOException, ClassNotFoundException {
        	if (args.length != 5) {
            		help();
        	}
        
        	int serverListenPort = Integer.parseInt(args[1]); // The port that this server should listen on to accept connections from other servers
        	int clientListenPort = Integer.parseInt(args[2]); // The port that this server should listen on to accept connections from clients
        	InetSocketAddress[] otherServerAddresses = Server.parseAddresses(args[3]); // The addresses of the other servers
        	String databasePath = args[4]; // The path to the database file
        	Server server = new Server(databasePath);	//Each server needs to create an instance of a server with a database for each.

		try {
			//Create a new server socket that we can then use to connect to other servers
			server.acceptServers(serverListenPort);
			System.out.println("<LOG Coordinator> Accepting servers on " + serverListenPort);
			server.acceptClients(clientListenPort);
			server.connectServers(otherServerAddresses);
			//while(true){

			//}


		} catch (IOException e) {
            		System.err.println(e);
            		e.printStackTrace(System.err);
        	} finally {
	    		System.out.println("System Closed");
            		server.close();
        	}
	}
}
