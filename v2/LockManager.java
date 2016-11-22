import java.io.IOException;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class LockManager {

  private int r;
  private int w;
  private Server server;
  private int initalVotes;
  private HashMap<String, Integer> votes;
  private LinkedList<Lock> locklist;
  private int serverNumber;
  private Boolean lockedVotesX;
  private Boolean lockedVotesY;
  private Logger logger;

  public LockManager(int r, int w, Server server, int initalVotes, int serverNumber, Logger logger) {
    this.w = w;
    this.r = r;
    this.server = server;
    this.initalVotes = initalVotes;
    this.serverNumber = serverNumber;
    this.votes = new HashMap<String, Integer>();
    this.lockedVotesX = false;
    this.lockedVotesY = false;
    this.logger = logger;
  }

  public boolean getLocks(LinkedList<Lock> locklist) throws IOException {
    //to implement : the quorum protocol
    this.locklist = locklist;
    log("<lm> About to request votes for locks: " + locklist.toString());
    //prepare the message to request lock
    for (Lock l : locklist) {
      votes.put(l.getData(), initalVotes);
    }

    Flag[] flags = new Flag[2];
    flags[0] = Flag.GETVOTES;
    flags[1] = Flag.REQ;

    Message<LinkedList<Lock>> message;

    for (int i = 0; i < server.numOfServers; i++) {
      message = new Message<LinkedList<Lock>>(flags, locklist, serverNumber);
      int k = (i >= this.serverNumber) ? i + 1 : i;
      log("<lm> Lock message going to server " + k);
      server.requestToServer(message, k);

      Message<HashMap> response = server.getServerResponseMessage();
      log("<lm> Recieved: " + response.toString());
      Flag[] responseFlags = response.getFlags();
      HashMap<String, Integer> payload = response.getMessage();

      if (FlagChecker.containsFlag(responseFlags, Flag.ACK)) {
        //add votes
        //check vote count. if enough, break
        //store which server sent votes for release
        

      } else if (FlagChecker.containsFlag(responseFlags, Flag.REJ)) {
        //if read, check if rej due to read or write.
        // if read, then continue
        // if write, then wait and retry.
        continue;
      } else {
        throw new MattBradburyException("Another Matt boo boo");
      }
    }

    return true;
  }

  public void processRequestMessages() throws IOException {
		Thread thread = new Thread(() -> {
      try {
        log("<lm> Listening for a vote request");
        Message<LinkedList<Lock>> m = server.getServerRequestMessage();
        int serverNumber = m.serverNumber;
        Message newMessage;
        HashMap<String, Integer> votesToSend;

        if (FlagChecker.containsFlag(m.flags, Flag.GETVOTES)) {

          log("<lm> Got vote request from " + m.serverNumber + " for " + m.getMessage().toString());
          LinkedList<Lock> payload  = m.getMessage();

          Iterator<Lock> itter = payload.iterator();

          Flag[] flags = new Flag[2];
          flags[0] = Flag.ACK;
          flags[1] = Flag.RSP;

          votesToSend = new HashMap<String, Integer>();
          while (itter.hasNext()){
            Lock item = itter.next();

            //need to store what transaction sent message, and what for (r/w).
            //then can tell sender on rej why, and also change who gets the locks.
            if(!votes.containsKey(item.getData())) {
              //can give votes but its never seen it before
              votes.put(item.getData(), 0);
              votesToSend.put(item.getData(), this.initalVotes);
              log("<lm> Sending votes for item " + item.getData());
            } else if (votes.containsKey(item.getData()) && (votes.get(item.getData()) != 0)) {
              //lockedVotes = true;
              votesToSend.replace(item.getData(), votes.get(item.getData()));
              votes.replace(item.getData(), 0);
              //send the person votes
              log("<lm> Sending votes for item " + item.getData());
            } else if (votes.containsKey(item.getData())) {
              flags[0] = Flag.REJ;
              //its seen it before but it has no votes
              votesToSend.replace(item.getData(), 0);
              //send a rejection

              log("<lm> Not Sending votes for item " + item.getData());
            } else {
              throw new MattBradburyException("Unrecognised type");
            }

          }
          newMessage = new Message<HashMap>(flags, votesToSend, this.serverNumber);
          log("<lm> Responding to server with votes: " + newMessage.toString());
          server.requestToServer(newMessage, serverNumber);
        }
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (IOException io) {
        io.printStackTrace();
      }
    });
		thread.start();
	}

  public boolean releaseLocks(VarList values) {
    // values is a list of values which were updated in the transaction.
    // must propogate the update
    return true;
  }

  private void log(String message) {
    try {
      System.out.println(message);
		  logger.writeLog(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
