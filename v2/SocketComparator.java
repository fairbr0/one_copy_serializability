import java.util.Comparator;
import java.net.*;
import java.io.*;

public class SocketComparator implements Comparator<Socket> {
  public int compare(Socket s1, Socket s2) {
    int i;
    int j;

    if(s1.getLocalPort() >10000) {
      i = s1.getPort();
    } else {
      i = s1.getLocalPort();
    }

    if(s2.getLocalPort() >10000) {
      j = s2.getPort();
    } else {
      j = s2.getLocalPort();
    }

    return i - j;
  }
}
