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

  public LockManager(int r, int w, Server server, int initalVotes, int serverNumber) {
    this.w = w;
    this.r = r;
    this.server = server;
    this.initalVotes = initalVotes;
    this.serverNumber = serverNumber;
    this.votes = new HashMap<String, Integer>();
    this.lockedVotesX = false;
    this.lockedVotesY = false;
  }

  public boolean getLocks(LinkedList<Lock> locklist) throws IOException {
    //to implement : the quorum protocol
    this.locklist = locklist;

    //prepare the message to request lock
    for (Lock l : locklist) {
      votes.put(l.getData(), initalVotes);
    }

    Flag[] flags = new Flag[2];
    flags[0] = Flag.GETVOTES;
    flags[1] = Flag.REQ;

    Message<LinkedList<Lock>> message = new Message<LinkedList<Lock>>(flags, locklist, serverNumber);
    System.out.println("lock message going out from server " + serverNumber + " is " + message.toString());
    int numberOfSentMessages = server.broadcast(message);

    // intercept message responses
    for (int i = 0; i < numberOfSentMessages, i++) {
        Message<HashMap> response = server.getServerResponseMessage();
        System.out.println(response.toString());
        Flags[] flags = m.getFlags();
        HashMap<String, Integer> payload = m.getData();
        if (FlagChecker.containsFlag(flags, Flag.ACK)) {
          
        } else if (FlagChecker.containsFlag(flags, Flag.REJ)) {
          continue;
        } else {
          throw new MattBradburyException("Another Matt boo boo");
        }
    }
    System.out.println("Got votes back: " + response.toString());
    return true;
  }

  public void processRequestMessages() throws IOException {
		Thread thread = new Thread(() -> {
      try {
        System.out.println("Listening for a request message in lock manager");
        Message<LinkedList<Lock>> m = server.getServerRequestMessage();
        System.out.println("LM got message from request queue");
        int serverNumber = m.serverNumber;
        Message newMessage;
        HashMap<String, Integer> votesToSend;

        if (FlagChecker.containsFlag(m.flags, Flag.GETVOTES)) {
            LinkedList<Lock> payload  = m.getMessage();

            Iterator<Lock> itter = payload.iterator();

            Flag[] flags = new Flag[2];
            flags[0] = Flag.ACK;
            flags[1] = Flag.RSP;

            votesToSend = new HashMap<String, Integer>();
            while (itter.hasNext()){
              Lock item = itter.next();
              if(!votes.containsKey(item.getData())) {
                //can give votes but its never seen it before
                votes.put(item.getData(), 0);
                votesToSend.put(item.getData(), this.initalVotes);
              } else if (votes.containsKey(item.getData()) && (votes.get(item.getData()) != 0)) {
                //lockedVotes = true;
                votesToSend.replace(item.getData(), votes.get(item.getData()));
                votes.replace(item.getData(), 0);
                //send the person votes
              } else if (votes.containsKey(item.getData())) {
                flasg[0] = Flag.REJ;
                //its seen it before but it has no votes
                votesToSend.replace(item.getData(), 0);
                //send a rejection
              } else {
                throw new MattBradburyException("Unrecognised type");
              }

            }
            newMessage = new Message<HashMap>(flags, votesToSend, this.serverNumber);
            System.out.println("response message with votes: " + newMessage.toString());
            server.requestToServer(newMessage, serverNumber);
        }

        /*if(!lockedVotes){
          if(FlagChecker.containsFlag(m.flags, Flag.ACK)) {

          } else if(FlagChecker.containsFlag(m.flags, Flag.REQ)) {

          } else if(FlagChecker.containsFlag(m.flags, Flag.REJ)) {

          } else if(FlagChecker.containsFlag(m.flags, Flag.RSP)) {

          } else if(FlagChecker.containsFlag(m.flags, Flag.GETVOTES)) {
            lockedVotes = true;
            m.
            int payload = votes.message
            Flag[] flags = new Flag[2];
            Flag.ACK;
            Flag.RSP;
            newMessage = new Message<Integer>(flags, payload, serverNumber);
          } else {
            throw new MattBradburyException("Someone Fucked up... It was probably me (matt) but ill blame Jarred anyway");
          }
          payload
          flags

        } else {
          payload
          flags

          //Fuck off Jarred you CUNT
        }

        server.requestToServer(newMessage, serverNumber);*/

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
}
