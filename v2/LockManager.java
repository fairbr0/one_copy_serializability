import java.io.IOException;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.lang.Object.*;

public class LockManager {

  private final int r;
  private final int w;
  private Server server;
  private final int initalVotes;
  private HashMap<String, Integer> votes;
  private LinkedList<Lock> locklist;
  private int serverNumber;
  private Logger logger;
  private boolean softlock;
  private boolean hardlock;
  private int transactionNumber = -1;

  public LockManager(int r, int w, Server server, int initalVotes, int serverNumber, Logger logger) {
    this.w = w;
    this.r = r;
    this.server = server;
    this.initalVotes = initalVotes;
    this.serverNumber = serverNumber;
    this.votes = new HashMap<String, Integer>();
    this.logger = logger;
		this.softlock = false;
    this.hardlock = false;
  }

  public boolean getLocks(LinkedList<Lock> locklist, int transactionNumber) throws IOException {
    this.locklist = locklist;
    this.transactionNumber = transactionNumber;
    setInitialVoteList();
    aquireLock();
    this.hardlock = true;
    return true;
  }

  public boolean releaseLocks(LinkedList<String> listToReset) {
    // values is a list of values which were updated in the transaction.
    // must propogate the update
		Flag[] flags = new Flag[2];
		flags[0] = Flag.RESET;
		flags[1] = Flag.REQ;

    if (listToReset.size() != 0) {
    	Message<LinkedList<String>> m = new Message(flags, listToReset, this.serverNumber);
    	server.broadcast(m);
    	//this.votes = new HashMap<String, Integer>();
    	//LinkedList<String>
    	Iterator<String> i = listToReset.iterator();
    	while (i.hasNext()) {
    		String variable = i.next();
    		this.votes.put(variable, this.initalVotes);
    	}
    }

		log("<lm>Hashmap size is now " + Integer.toString(this.votes.size()));
    log("<lm> Releasing Locks");
		log("The number of votes I have for variable x is " + this.votes.get("x"));
		this.hardlock = false;
    this.softlock = false;
    return true;
  }

  public void processRequestMessages() throws IOException {
    Thread thread = new Thread(() -> {
      try {
        while (true) {
          Message incommingMessage = server.getServerRequestMessage();
          int incommingServerNumber = incommingMessage.serverNumber;
          HashMap<String, Integer> votesToSend = new HashMap<String, Integer>();

          Message newMessage;

          ////FFFLLLAAGGGSSSS
          if (FlagChecker.containsFlag(incommingMessage.flags, Flag.SERVERNUMBER)) {
            Flag[] flags = new Flag[2];
            flags[0] = Flag.ACK;
            flags[1] = Flag.RSP;
            newMessage = new Message<Integer>(flags, this.serverNumber, this.serverNumber);
            log("<lm> Responding to server with server number: " + newMessage.toString());
            server.requestToServer(newMessage, serverNumber);
          }

          else if (FlagChecker.containsFlag(incommingMessage.flags, Flag.GETVOTES)) {
            Message<Pair<Integer, LinkedList<Lock>>> n = (Message<Pair<Integer, LinkedList<Lock>>>) incommingMessage;
            int voteRequestTransaction = n.getMessage().t1;
            LinkedList<Lock> payload = n.getMessage().t2;

            Iterator<Lock> it = payload.iterator();
            Flag[] flags = {Flag.ACK, Flag.RSP};
            while (it.hasNext()) {
              Lock item = it.next();
              if (this.hardlock && contains(this.locklist, item.getData())) {
                votesToSend.put(item.getData(), -1);
                flags[0] = Flag.REJ;
                log("We've got a hard on");
              } else if (this.softlock && contains(this.locklist, item.getData()) && voteRequestTransaction < this.transactionNumber) {
                setSendVotes(votesToSend, item);
                log("We've got a softie and the other server is more important than is");
              } else if (this.softlock && contains(this.locklist, item.getData()) && voteRequestTransaction > this.transactionNumber) {
                votesToSend.put(item.getData(), -1);
                flags[0] = Flag.REJ;
                log("We've still got a softie but the other server is now worse than us - therefore dont send our votes.");
              } else {
                setSendVotes(votesToSend, item);
              }

            }

            log("<lm> sending the message: " + votesToSend.toString());
            newMessage = new Message<HashMap>(flags, votesToSend, this.serverNumber);
            server.requestToServer(newMessage, incommingServerNumber);
          }

          else if (FlagChecker.containsFlag(incommingMessage.flags, Flag.RESET)) {
            Message<LinkedList<String>> n = (Message<LinkedList<String>>) incommingMessage;
            LinkedList<String> payload  = n.getMessage();
            Iterator<String> itter = payload.iterator();

            while (itter.hasNext()){
              String itemName = itter.next();
              log("reset for item " + itemName);
              this.votes.put(itemName, this.initalVotes);
            }
            //here we dont want to be redeclaring the hash map we just want to get the object and reset the values to 0.

            log("<lm> Hashmap size is now " + Integer.toString(this.votes.size()));
          }

        }
      } catch (IOException t) {
        t.printStackTrace();
      }
    });
    thread.start();
  }
  //method with all the aquisition logic
  private void aquireLock() throws IOException {
    this.softlock = true;
    while (!checkVotes()) {
      for (int i = 0; i < server.numOfServers; i++) {

        LinkedList<Lock> newLocklist = createServerMessageLockList();
        //get the actual number of server your talking too
        int k = (i >= this.serverNumber) ? i + 1 : i;

        if (locklist.size() != 0) {
          Flag[] flags = {Flag.GETVOTES, Flag.REQ};
          Pair<Integer, LinkedList<Lock>> votePayload = new Pair<Integer, LinkedList<Lock>>(this.transactionNumber, newLocklist);
          Message<Pair<Integer, LinkedList<Lock>>> message = new Message<Pair<Integer, LinkedList<Lock>>>(flags, votePayload, serverNumber);
          server.requestToServer(message, k);

          Message<HashMap> response = server.getServerResponseMessage();
          Flag[] responseFlags = response.getFlags();
          HashMap<String, Integer> payload = response.getMessage();

          if (FlagChecker.containsFlag(responseFlags, Flag.ACK)) {
            addVoteList(newLocklist, payload);
          }

          else if (FlagChecker.containsFlag(responseFlags, Flag.REJ)) {
            addRejectedVoteList(newLocklist, payload);
          }

          else {
            throw new MattBradburyException("Incorrect Flags");
          }
        }
        if (checkVotes()) {
          break;
        }
      }
    }
  }

  private void setSendVotes(HashMap<String, Integer> votesToSend, Lock item) {
    String data = item.getData();
    if (!this.votes.containsKey(data)) {
      this.votes.put(data, 0);
      votesToSend.put(data, this.initalVotes);
      log("<lm> Sending votes for item " + data);
    } else if (this.votes.containsKey(data) && (this.votes.get(data) > 0)) {
      votesToSend.put(data, 0);
      if (item.getLock() == LOCK_TYPE.WRITE) {
        votesToSend.replace(data, votes.get(data));
        this.votes.replace(data, 0);
      } else if (item.getLock() == LOCK_TYPE.READ && !this.softlock) {
        votesToSend.replace(data, 1);
        this.votes.replace(data, this.votes.get(data) - 1);
      } else {
        votesToSend.replace(data, 0);
      }
    } else if (this.votes.containsKey(data)){
      votesToSend.put(data, 0);
    } else {
      throw new MattBradburyException("Unknown data item");
    }
  }

  private boolean contains(LinkedList<Lock> list, String item) {
    Iterator<Lock> it = list.iterator();
    while (it.hasNext()) {
      Lock l = it.next();
      if (l.getData().equals(item)) {
        return true;
      }
    }
    return false;
  }

  private boolean checkVotes() {

		for (Lock l : this.locklist) {
			if (!this.votes.containsKey(l.getData())) {
				log("<LM> CHECKVOTES RESULT = FALSE");
				return false;
			} else {
				int votes = this.votes.get(l.getData());
				if (l.getLock() == LOCK_TYPE.READ) {
					if (votes < this.r) {
						log("<LM> CHECKVOTES RESULT = FALSE");
						return false;
					}
				} else {
					if (votes < this.w) {
						log("<LM> CHECKVOTES RESULT = FALSE");
						return false;
					}
				}
			}
    }
		log("<LM> CHECKVOTES RESULT = TRUE");
		return true;
	}

  private void setInitialVoteList() {
    for (Lock l : this.locklist) {
			if (!this.votes.containsKey(l.getData())) {
				this.votes.put(l.getData(), this.initalVotes);
			}
    }
  }

  private LinkedList<Lock> createServerMessageLockList() {
    LinkedList<Lock> tempLockList = new LinkedList<Lock>();
    log("The size of the tempLockList is " + Integer.toString(locklist.size()));
    Iterator<Lock> it = this.locklist.iterator();
    while (it.hasNext()){
      //log("At position " + k);
      Lock item = it.next();
      String itemName = item.getData();		// x or y
      LOCK_TYPE lockType = item.getLock();	//Lock Type
      if(lockType == LOCK_TYPE.READ) {
        if (this.votes.get(itemName) < r) {
          tempLockList.add(item);
          log("item added");
          log(Integer.toString(locklist.size()));
        }
      } else if (lockType == LOCK_TYPE.WRITE) {
        if (this.votes.get(itemName) < w) {
          tempLockList.add(item);
          log("item added");
          log(Integer.toString(locklist.size()));
        }
      } else {
        throw new MattBradburyException();
      }
    }
    log("List of locks to aquire completed");
    return tempLockList;
  }

  private void addVoteList(LinkedList<Lock> locklist, HashMap<String, Integer> votes) {
    for (int j=0; j<locklist.size(); j++) {
      Lock item = locklist.get(j);
      String itemName = item.getData();
      int numberOfVotes = votes.get(itemName);
      log("Item from the locklist is " + itemName + " and number of votes given are " + numberOfVotes);
      this.votes.put(itemName, this.votes.get(itemName) + numberOfVotes);
      log("The number of votes in the system for item " + itemName + " is now " + this.votes.get(itemName));
    }
  }

  private void addRejectedVoteList(LinkedList<Lock> locklist, HashMap<String, Integer> votes) {
    LinkedList<Lock> itemsToEnquire = new LinkedList<Lock>();
    for (int j=0; j<locklist.size(); j++) {
      Lock item = locklist.get(j);
      String itemName = item.getData();
      int numberOfVotes = votes.get(itemName);
      log("Item from the locklist is " + itemName + " and number of votes given are " + numberOfVotes);
      if (numberOfVotes >= 0) {
        this.votes.put(itemName, this.votes.get(itemName) + numberOfVotes);
      } else if (numberOfVotes == -1) {
        // here we want to send a message back to the server asking what its server number is - if its less
        // than ours well give it our votes - otherwise it will give us its votes - all wrapped into new method

        continue;
      }
      log("The number of votes in the system for item " + itemName + " is now " + this.votes.get(itemName));
    }
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
