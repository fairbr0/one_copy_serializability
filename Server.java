import java.io.*;
import java.net.*;

/*
  Server class for the CS346 coursework.
  The arguments required when starting the server are the following:
  Client listen Port number
  Server listen Port number
  Other Server addresses, comma separated
  Server id

*/

public class Server {

  private int serverID;
  private int clientPort;
  private int serverPort;
  private InetSocketAddress[] serverAddresses;

  public static void main(String args[]) {
    Server server = new Server();
    if (!(args.length == 4)) server.bootFailure();

    server.setupServer(args);
  }

  private void setupServer(String[] args) {
    this.serverID = Integer.parseInt(args[3]);
    this.clientPort = Integer.parseInt(args[0]);
    this.serverPort = Integer.parseInt(args[1]);
    this.serverAddresses = parseAddresses(args[2]);

    System.out.println(this.serverID);
    System.out.println(this.clientPort);
    System.out.println(this.serverPort);
    for (int i = 0; i < serverAddresses.length; i++) {
      System.out.println(this.serverAddresses[i].toString());
    }


  }

  private InetSocketAddress[] parseAddresses(String addresses) {
    String[] rawAddresses = addresses.split(",");
    InetSocketAddress[] socketAddresses = new InetSocketAddress[rawAddresses.length];
    int i = 0;
    for (String address : rawAddresses) {
      String[] parts = address.split(":");
      int port = Integer.parseInt(parts[1]);
      InetSocketAddress addr = new InetSocketAddress(parts[0], port);
      socketAddresses[i++] = addr;
    }

    return socketAddresses;
  }

  private void bootFailure() {
    System.out.println("Failed to boot");
  }

  public void acceptServers() {}

  public void acceptClient() {}

  public void connectServers() {}


}
