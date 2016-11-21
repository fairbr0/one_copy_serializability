import java.net.*;
import java.io.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

class Server {

	private ServerSocket serverSocket;
  private Thread serverListenerThread;
  private boolean isClosing;
  private Socket servers[];
	private SocketWrapper wrappedServers[];
	private Logger logger;
	private LinkedList<Message> requestQueue;
  private LinkedList<Message> responseQueue;
	private int portNumber;
	private int serverNumber;

	public Server(int serverNumber) throws IOException {
		this.logger = new Logger(serverNumber); //Here we want to document what type of server this is
		this.isClosing = false;
		this.responseQueue = new LinkedList<Message>();
    this.requestQueue = new LinkedList<Message>();
		this.serverNumber = serverNumber;
	}

  public void acceptServers(int port) throws IOException {
    serverSocket = new ServerSocket(port);
		this.portNumber = port;
  }

	public void connectServers(InetSocketAddress[] servers, int serverNumber) throws IOException{
		System.out.println("Connecting to servers method called");
    this.servers = new Socket[servers.length];
		this.wrappedServers = new SocketWrapper[servers.length];

		AtomicInteger counter = new AtomicInteger(0);

		//int serverNumber = Integer.parseInt(this.databaseFilePath.replaceAll("\\D", ""));
    this.serverListenerThread = new Thread(() -> {
      int k = 0;
      while (!this.isClosing()) {
        try {
          //add socket to array
          log("Listening for server connections" + k + " on " + portNumber);
          Socket socket = serverSocket.accept();
          log("Server " + k + " connected");
          socket.setTcpNoDelay(true);
          socket.setSoTimeout(300000);
          this.servers[k] = socket;
          this.listenServer(k++);
					counter.getAndIncrement();
        } catch (Exception e) {
          if (this.isClosing()) continue;
          System.err.println(e);
          e.printStackTrace(System.err);
        }
      }
    });
    this.serverListenerThread.start();

    log("Trying to connect to servers ");
		for (int i = serverNumber; i < servers.length; i++) {
			int port = servers[i].getPort();
			InetAddress address = servers[i].getAddress();

			boolean connected = false;
			int count = 0;
			while (count < 10 && !connected) {
        //add socket to array
				try {
          log("Trying to connect to server " + i);
					this.servers[i] = new Socket(address, port);
          this.listenServer(i);
					connected = true;
					counter.getAndIncrement();
				} catch (ConnectException e) {
					log("Connection refused");
					count += 1;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
					}
				} catch (IOException e1) {
          e1.printStackTrace();
        }
 			}
		}

		System.out.println("Connected to all servers");

		while (counter.get() < servers.length) {
			//Hacky as fuck
		}

		configureServers();
  }

	public void configureServers() {
			System.out.println("Configure servers called");
			Flag[] flags = new Flag[1];
			flags[0] = Flag.HAND;
			Message<Integer> m = new Message<Integer>(flags, this.serverNumber, this.serverNumber);
			initialRequestToServer(m);
	}

	public void printWrappedServers() {
		for (int i=0; i<wrappedServers.length; i++) {
			System.out.println(wrappedServers[i].getSocket().getPort() +  " " + wrappedServers[i].getSocket().getLocalPort() + " " + wrappedServers[i].getServerNumber());
		}
	}

  //listen on given socket for client activity
  public void listenServer(int i) {
    Thread listenThread = new Thread(() -> {
      try {
        do {
          Socket s = servers[i];
          ObjectInputStream is = new ObjectInputStream(s.getInputStream());
          log("Listening for server messages");
          Message recieved = (Message)is.readObject();
					if(FlagChecker.containsFlag(recieved.getFlags(), Flag.HAND)) {
							Object serverNumber = recieved.getMessage();
							int serverNum = (Integer) serverNumber;
							System.out.println("We have got a message from some server whose server number is " + serverNum);
							wrappedServers[i] = new SocketWrapper(servers[i], serverNum);
					}
          handleServerRequest(recieved, i);
        } while (!isClosing());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    listenThread.start();
  }

  public int broadcast(Message m) {

    try {
      log("Broadcasting to servers");
      for (int i = 0; i < servers.length+1; i++) {
				if(i!= serverNumber){
        	requestToServer(m, i);
				}
      }
			return servers.length;
    } catch (Exception e) {
      e.printStackTrace();
    }
		return 0;
  }

  //listens to the server sockets for a message
  public void handleServerRequest(Message message, int i) throws IOException {
		System.out.println("Server message recieved " + message.toString());
		log("Server message recieved " + message.toString());
		if (FlagChecker.containsFlag(message.getFlags(), Flag.RSP)) {
      responseQueue.add(message);
			System.out.println("Message added to response queue");
    } else if (FlagChecker.containsFlag(message.getFlags(), Flag.REQ)) {
      requestQueue.add(message);
			System.out.println("Server message recieved " + message.toString());
			System.out.println("Message added to request queue");
		}

  }

	public void initialRequestToServer(Message message) {
		try {
			System.out.println("Broadcasting to servers");
      log("Broadcasting to servers");
      for (int i = 0; i < servers.length; i++) {
				if(servers[i] != null){
        	log("Sending request to server " + i);
		      Socket socket = servers[i];
		      ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
		      os.writeObject(message);
		      os.flush();
				}
      }
			System.out.println("Connection broadcast complete");
    } catch (Exception e) {
      e.printStackTrace();
    }
	}

  //send message to server;
  public void requestToServer(Message message, int i) {
    try {
      log("Sending request to server " + i);
      Socket socket = getCorrectSocket(i);
      ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
      os.writeObject(message);
      os.flush();
      log("Sent test");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

	public Socket getCorrectSocket(int i) {
		System.out.println(i);
		for (int j=0; j<wrappedServers.length; j++) {
			if(wrappedServers[j].getServerNumber() == i) {
				return wrappedServers[j].getSocket();
			}
		}
		return null;
	}

	public Message getServerResponseMessage() throws IOException {
		while (responseQueue.size() == 0){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log("Message popped off the server queue");
		System.out.println("Message popped off the server queue");
		return responseQueue.pop();

	}

  public Message getServerRequestMessage() throws IOException {
		while (requestQueue.size() == 0){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log("Message popped off the server queue");
		System.out.println("Message popped off the server queue");
		return requestQueue.pop();

	}

  public boolean isClosing() {
    return this.isClosing;
  }

  public void close() {
    this.isClosing = true;
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


	public void log(String message) throws IOException {
		logger.writeLog(message);
	}
}
