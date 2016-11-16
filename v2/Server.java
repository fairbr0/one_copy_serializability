import java.net.*;
import java.io.*;
import java.util.Queue;
import java.util.LinkedList;

class Server {

	private ServerSocket serverSocket;
  private Thread serverListenerThread;
  private boolean isClosing;
  private Socket servers[];
	private Logger logger;
	private LinkedList<Message> requestQueue;
  private LinkedList<Message> responseQueue;

	public Server(String databaseFilePath) throws IOException {
		this.logger = new Logger(databaseFilePath, "Server"); //Here we want to document what type of server this is
		this.isClosing = false;
		this.logger.writeDatabase("10");
		this.responseQueue = new LinkedList<Message>();
    this.requestQueue = new LinkedList<Message>();
	}

  public void acceptServers(int port) throws IOException {
    serverSocket = new ServerSocket(port);
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

  public void broadcast(Message m) {

    try {
      log("Broadcasting to servers");
      for (int i = 0; i < servers.length; i++) {
        requestToServer(m, i);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //listens to the server sockets for a message
  public void handleServerRequest(Message message, int i) throws IOException {
		log("Server message recieved " + message.toString());
    if (containsFlag(message.getFlags(), Flag.RSP)) {
      responseQueue.add(message);
    } else if (containsFlag(message.getFlags(), Flag.REQ)) {
      requestQueue.add(message);
    }

  }


  //send message to server;
  public void requestToServer(Message message, int i) {
    try {
      log("Sending request to server " + i);
      Socket socket = servers[i];
      ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
      os.writeObject(message);
      os.flush();
      //os.close();
      log("Sent test");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

	public Message getServerResponseMessage() throws IOException {
		if(responseQueue.size() == 0){
			log("Server queue is empty so null is returned");
			return null;
		} else{
			log("Message popped off the server queue");
			return responseQueue.pop();
		}
	}

  public Message getServerRequestMessage() throws IOException {
		if(requestQueue.size() == 0){
			log("Server queue is empty so null is returned");
			return null;
		} else{
			log("Message popped off the server queue");
			return requestQueue.pop();
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

  private boolean containsFlag(Flag[] flags, Flag f) {
    for (Flag s : flags) {
      if (s == f) return true;
    }
    return false;
  }

	public void log(String message) throws IOException {
		logger.writeLog(message);
	}
}
