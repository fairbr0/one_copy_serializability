import java.net.*;
import java.io.*;
import java.util.Queue;
import java.util.LinkedList;

class Server {
  //will only 1 client ever be connected? Yes Jake

  private ServerSocket clientSocket;
	private ServerSocket serverSocket;
  private Thread clientListenerThread;
  private Thread serverListenerThread;
  private boolean isClosing;
  private Socket servers[];
	private Logger logger;
	private LinkedList<Message> clientQueue;
	private LinkedList<Message> serverQueue;

	public Server(String databaseFilePath) throws IOException {
		this.logger = new Logger(databaseFilePath, "Server"); //Here we want to document what type of server this is
		this.isClosing = false;
		this.logger.writeDatabase("10");
		this.clientQueue = new LinkedList<Message>();
		this.serverQueue = new LinkedList<Message>();
	}

  public void acceptServers(int port) throws IOException {
    serverSocket = new ServerSocket(port);
  }

  public void acceptClients(int port) throws IOException {
    this.clientSocket = new ServerSocket(port);
    log("Listening for client connections");
    this.clientListenerThread = new Thread(() -> {
      while (!this.isClosing()) {
        try {
          Socket socket = clientSocket.accept();
          log("Client connected on socket " + socket.toString() + ">");
          socket.setTcpNoDelay(true);
          socket.setSoTimeout(300000);
          this.listenClient(socket);
        } catch (Exception e) {
          if (this.isClosing()) continue;
          System.err.println(e);
          e.printStackTrace(System.err);
        }
      }
    });
    this.clientListenerThread.start();
  }


  public void connectServers(InetSocketAddress[] servers, int serverNumber) throws IOException{
    this.servers = new Socket[servers.length];

		//int serverNumber = Integer.parseInt(this.databaseFilePath.replaceAll("\\D", ""));
    this.serverListenerThread = new Thread(() -> {
      int k = 0;
      while (!this.isClosing()) {
        try {
          //add socket to array
          log("Listening for server connections" + k);
          Socket socket = serverSocket.accept();
          log("Server " + k + " connected");
          socket.setTcpNoDelay(true);
          socket.setSoTimeout(300000);
          this.servers[k] = socket;
          this.listenServer(k++);
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
  }

  //listen on given socket for client activity
  public void listenClient(Socket socket) {
    Thread listenThread = new Thread(() -> {
      try {
        do {
					InputStream s = socket.getInputStream();
          ObjectInputStream is = new ObjectInputStream(s);
          log("listening for client messages");
          Object sent = is.readObject();
          handleClientRequest((Message) sent);
        } while (!isClosing());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    listenThread.start();
  }

  //listen on given socket for client activity
  public void listenServer(int i) {
    Thread listenThread = new Thread(() -> {
      try {
        do {
          Socket s = servers[i];
          ObjectInputStream is = new ObjectInputStream(s.getInputStream());
          log("Listening for server messages");
          Message sent = (Message)is.readObject();
          handleServerRequest(sent, i);
        } while (!isClosing());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    listenThread.start();
  }

  public void testSend() {

    try {
      Thread.sleep(4000);
      log("Sending test");
      for (int i = 0; i < servers.length; i++) {
        requestToServer("hello", i);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Listens to the clients sockets for a message;
  public void handleClientRequest(Message message) throws IOException{
    //logic to process the clients request object
		log("Client message recieved " + message.toString());
		clientQueue.add(message);
  }

  //listens to the server sockets for a message
  public void handleServerRequest(Message message, int i) throws IOException {
		log("Server message recieved " + message.toString());
		serverQueue.add(message);
  }

  //send message to server;
  public void requestToServer(String message, int i) {
    try {
      log("Sending request to server " + i);
      Socket socket = servers[i];
      ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
			Message send = new Message<String>(message);
      os.writeObject(send);
      os.flush();
      //os.close();
      log("Sent test");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

	public Message getServerMessage() throws IOException {
		if(serverQueue.size() == 0){
			log("Server queue is empty so null is returned");
			return null;
		} else{
			log("Message popped off the server queue");
			return serverQueue.pop();
		}
	}

	public Message getClientMessage()throws IOException {
		if(clientQueue.size() == 0){
			log("Client queue is empty so null is returned");
			return null;
		} else{
			log("Message popped off the client queue");
			return clientQueue.pop();

		}
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
