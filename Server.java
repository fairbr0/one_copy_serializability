//package cs347;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Server {

	public final String databaseFilePath;
	public final String logFilePath;
	private ServerSocket clientSocket;
   	private ServerSocket serverSocket;
	private Thread clientListenerThread;
    	private Connection[] connections;


    	private static int defaultValue() {
		return 0;
    	}

    	public Server(String databaseFilePath) throws IOException {
        	this(databaseFilePath, Server.defaultValue());
    	}

    	public Server(String databaseFilePath, int value) throws IOException {
        	this.databaseFilePath = databaseFilePath;
        	if (!this.databaseExists()) {
            		this.writeDatabase(value);
        	}
		//Statement to get the database number out of the database path
		int i = Integer.parseInt(this.databaseFilePath.replaceAll("\\D", ""));
		this.logFilePath = "log"+Integer.toString(i)+".txt";
		writeLog(this.logFilePath);
                if (!this.logExists()) {
                        this.createLog();
                }
                this.writeLog("Jarred is a cunt");
                this.writeLog("Jake is amazing");
    	}

    	public final boolean databaseExists() {
        	File f = new File(this.databaseFilePath);
		writeLog(f.exists());
        	return f.exists() && !f.isDirectory();
    	}

	public final boolean logExists(){
                File f = new File(this.logFilePath);
                return f.exists() && !f.isDirectory();
	}

	public final int queryDatabase() throws IOException {
        	int value=0;
        	Path path = Paths.get(this.databaseFilePath, new String[0]);
        	BufferedReader reader = Files.newBufferedReader(path);
        	Throwable throwable = null;
        	try {
            		String line = reader.readLine();
            		while (line != null) {
                		String[] parts = line.split("=");
				value=Integer.parseInt(parts[1].replaceAll("\\s", ""));
                		line = reader.readLine();
            		}
        	} catch (Throwable line) {
            		throwable = line;
            		throw line;
        	} finally {
            		if (reader != null) {
                		if (throwable != null) {
                    			try {
                        			reader.close();
                    			} catch (Throwable line) {
                        			throwable.addSuppressed(line);
                    			}
                		} else {
                    			reader.close();
                		}
            		}
        	}
        	writeLog("<server> <query database response = " + (Object)value + ">\n");
        	return value;
    	}

	public final void writeLog(String logMessage) throws IOException {
		Writer output = new BufferedWriter(new FileWriter(this.logFilePath, true));
		output.append("\r\n");
		output.append(logMessage);
		System.out.print(logMessage);
		output.close();
	}

        public final void createLog() throws IOException {
                Path path = Paths.get(this.logFilePath, new String[0]);
                BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                Throwable throwable = null;
                try {
                        writer.append("This is the log file server/database " + Integer.toString(Integer.parseInt(this.databaseFilePath.replaceAll("\\D", ""))));
                        writer.flush();
                } catch (Throwable var5_7) {
                        System.out.println("Log write error");
                        throwable = var5_7;
                        throw var5_7;
                } finally {
                        if (writer != null) {
                                if (throwable != null) {
                                        try {
                                                writer.close();
                                        } catch (Throwable var5_6) {
                                                throwable.addSuppressed(var5_6);
                                        }
                                } else {
                                        writer.close();
                                }
                        }
                }
        }

	public final void writeDatabase(int value) throws IOException {
        	Path path = Paths.get(this.databaseFilePath, new String[0]);
        	BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        	Throwable throwable = null;
        	try {
			writeLog("attempting to write " + value);
            		writer.write(Integer.toString(value));
            		writer.flush();
        	} catch (Throwable var5_7) {
			writeLog("Database write error");
            		throwable = var5_7;
            		throw var5_7;
        	} finally {
            		if (writer != null) {
                		if (throwable != null) {
                    			try {
                        			writer.close();
                    			} catch (Throwable var5_6) {
                        			throwable.addSuppressed(var5_6);
                    			}
                		} else {
                    			writer.close();
                		}
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

	public final void acceptClients(int port) throws IOException {
	        clientSocket = new ServerSocket(port);
		//finish this
	}

	private void processClient(Socket socket) {
		//finish this
	}

	public void acceptServers(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		writeLog("<server> Now accepting clients");
	}

	public void connectServers(InetSocketAddress[] servers) throws IOException {
		writeLog("<LOG> Connecting to all slave servers");

		connections = new Connection[servers.length];

		int serverNumber = Integer.parseInt(this.databaseFilePath.replaceAll("\\D", ""));

		for (int i = 0; i < connections.length; i++) {
			//Create a socket for each connection necessary
			if (i<serverNumber) {
				connections[i] = new Connection(this.serverSocket.accept());
			} else {
			        int port = servers[0].getPort();
			        InetAddress address = servers[0].getAddress();
				connections[i] = new Connection(new Socket(address, port));
			}

			//QUESTIONS?!?!?!??!?!?!?!?!?!?!
			sockets[i].accept();
			sockets[i].connect(servers[i]);
			writeLog("<server> socket created for server " + i);
		}

	}

	public void close() throws IOException {
        	writeLog("<server> <closing>\n");
        	if (this.clientSocket != null) {
            		this.clientSocket.close();
        	}
        	if (this.clientListenerThread != null) {
            		try {
                		this.clientListenerThread.interrupt();
                		this.clientListenerThread.join(1000);
            		} catch (InterruptedException e) {
                		System.err.println(e);
                		e.printStackTrace(System.err);
            		}
        	}
		if (serverSocket != null){
            		serverSocket.close();
		}
    	}

	public boolean handleClientRequest(int value) throws IOException {
		return true;
	}

}

class Connection{

	private Socket socket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public Connection(Socket socket){
		this.socket = socket;
		outputStream = new ObjectOutputStream();
                inputStream = new ObjectInputStream();
	}

	public void sendMessage(){
	
	}

	public Message recieveMessage(){

	}
}

class Message {
	private String message;

	public Message(String message) {
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

}
