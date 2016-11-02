import java.io.BufferedReader;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.io.FileNotFoundException;

public class Server {

	public final String databaseFilePath;
	public final String logFilePath;
	private ServerSocket clientSocket;
	private ServerSocket serverSocket;
	private Thread clientListenerThread;
	private Connection[] connections;
	private ExecutorService threadpool;
	private CompletionService<Message> completionService;

	public Server(String databaseFilePath) throws IOException {
		this(databaseFilePath, 0);
	}

	public Server(String databaseFilePath, int value) throws IOException {
		this.databaseFilePath = databaseFilePath;
		int i = Integer.parseInt(this.databaseFilePath.replaceAll("\\D", ""));
		this.logFilePath = "log"+Integer.toString(i)+".txt";
		if (!this.logExists()) {
			this.createLog();
		}
		if (!this.databaseExists()) {
			this.writeDatabase(value);
		}
		this.threadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.completionService = new ExecutorCompletionService<Message>(this.threadpool);
	}

	public final boolean databaseExists() {
		File f = new File(this.databaseFilePath);
		return f.exists() && !f.isDirectory();
	}

	public final boolean logExists() {
		File f = new File(this.logFilePath);
		return f.exists() && !f.isDirectory();
	}

	public final int queryDatabase() throws IOException {
		int value = 0;
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

	public final void writeLog(String logMessage) {
		if (logExists()) {
			try (
				Writer fw = new FileWriter(logFilePath, true);
				Writer bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)
			) {
				out.println(logMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Tried to write log but log file does not exist");
		}
	}

	public final void createLog() throws IOException {
		File f = new File(logFilePath);
		f.createNewFile();
	}

	public final void writeDatabase(int value) throws IOException {
		FileWriter f = new FileWriter(databaseFilePath, false);
		f.write(value);
		System.err.println("Wrote to database file");
		f.close();
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
		writeLog("<server> Connecting to all other servers");

		connections = new Connection[servers.length];

		int serverNumber = Integer.parseInt(this.databaseFilePath.replaceAll("\\D", ""));

		for (int i = 0; i < connections.length; i++) {
			//Create a socket for each connection necessary
			if (i < serverNumber) {
				//listen
				connections[i] = new Connection(this.serverSocket.accept());
				connections[i].setMessage(new Message<Integer>(0));
			} else {
				int port = servers[0].getPort();
				InetAddress address = servers[0].getAddress();

				boolean connected = false;
				int count = 0;
				while (count < 10 && !connected) {
					try {
						connections[i] = new Connection(new Socket(address, port));
						connections[i].setMessage(new Message<Integer>(1));
						connected = true;
					} catch (ConnectException e) {
						System.err.println("Connection refused");
						count += 1;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
					}
				}
 			}
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
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	public boolean handleClientRequest(int value) throws IOException {
		return true;
	}

	public void testSend(String s) {
		for (Connection c : connections) {
			completionService.submit(c);
		}
		System.err.println("Submitted to completion service; taking");
		for (Connection c : connections) {
			try {
				Future<Message> f = completionService.take();
				Message m = f.get(10, TimeUnit.SECONDS);
				writeLog("Found message " + m.toString());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}
}
