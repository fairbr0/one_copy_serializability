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
    log("<LM> Getting locks");
    setInitialVoteList();
    aquireLock();
    log("<LM> Hard Lock enabled");
    return true;
  }

  public boolean releaseLocks(LinkedList<String> listToReset) {
    // values is a list of values which were updated in the transaction.
    // must propogate the update
    log("<LM> Releasing locks");
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
		this.hardlock = false;
    this.softlock = false;
    log("<LM> Hard Lock Released");
    log("<LM> Soft Lock Released");
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

          if (FlagChecker.containsFlag(incommingMessage.flags, Flag.SERVERNUMBER)) {
            Flag[] flags = new Flag[2];
            flags[0] = Flag.ACK;
            flags[1] = Flag.RSP;
            newMessage = new Message<Integer>(flags, this.serverNumber, this.serverNumber);
            log("<LM> Responding to server with server number: " + newMessage.toString());
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
                log("<LM>Sending a rejection as Hard Lock is on");
              } else if (this.softlock && contains(this.locklist, item.getData()) && voteRequestTransaction < this.transactionNumber) {
                setSendVotes(votesToSend, item);
                log("<LM> Sending votes as the transaction ID is less than the transaction we are processing");
              } else if (this.softlock && contains(this.locklist, item.getData()) && voteRequestTransaction > this.transactionNumber) {
                votesToSend.put(item.getData(), -1);
                flags[0] = Flag.REJ;
                log("<LM> NOT sending votes as the transaction ID is greater than the transaction we are processing");
              } else {
                setSendVotes(votesToSend, item);
              }

            }
            log("<LM> Vote response sent to server " + incommingServerNumber);
            newMessage = new Message<HashMap>(flags, votesToSend, this.serverNumber);
            server.requestToServer(newMessage, incommingServerNumber);
          }

          else if (FlagChecker.containsFlag(incommingMessage.flags, Flag.RESET)) {
            Message<LinkedList<String>> n = (Message<LinkedList<String>>) incommingMessage;
            LinkedList<String> payload  = n.getMessage();
            Iterator<String> itter = payload.iterator();

            while (itter.hasNext()){
              String itemName = itter.next();
              log("<LM> Reseting votes for item " + itemName);
              this.votes.put(itemName, this.initalVotes);
            }
          }

        }
      } catch (IOException t) {
        t.printStackTrace();
      }
    });
    thread.start();
  }

  private void aquireLock() throws IOException {
    log("<LM> Aquiring Locks");
    this.softlock = true;
    log("<LM> Soft Lock enabled");
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
          log("<LM> Vote response recieved from server " + response.serverNumber);

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
    } else if (this.votes.containsKey(data) && (this.votes.get(data) > 0)) {
      votesToSend.put(data, 0);
      if (item.getLock() == LOCK_TYPE.WRITE) {
        votesToSend.replace(data, votes.get(data));
        this.votes.replace(data, 0);
      } else if (item.getLock() == LOCK_TYPE.READ) {
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
    log("<LM> Sending " + votesToSend.get(data) + " votes for item " + data );
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
  
    	this.hardlock = true;

		for (Lock l : this.locklist) {
			if (!this.votes.containsKey(l.getData())) {
				log("<LM> CHECKVOTES RESULT = FALSE");
				this.hardlock = false;
				return false;
			} else {
				int votes = this.votes.get(l.getData());
				if (l.getLock() == LOCK_TYPE.READ) {
					if (votes < this.r) {
						log("<LM> CHECKVOTES RESULT = FALSE");
						this.hardlock = false;
						return false;
					}
				} else {
					if (votes < this.w) {
						log("<LM> CHECKVOTES RESULT = FALSE");
						this.hardlock = false;
						return false;
					}
				}
			}
    }
		log("<LM> CHECKVOTES RESULT = TRUE");
		
		return true;
	}

  private void setInitialVoteList() {
    log("<LM> Initializing vote list");
    for (Lock l : this.locklist) {
			if (!this.votes.containsKey(l.getData())) {
				this.votes.put(l.getData(), this.initalVotes);
			}
    }
  }

  private LinkedList<Lock> createServerMessageLockList() {
    LinkedList<Lock> tempLockList = new LinkedList<Lock>();
    Iterator<Lock> it = this.locklist.iterator();
    while (it.hasNext()){
      Lock item = it.next();
      String itemName = item.getData();		// x or y
      LOCK_TYPE lockType = item.getLock();	//Lock Type
      if(lockType == LOCK_TYPE.READ) {
        if (this.votes.get(itemName) < r) {
          tempLockList.add(item);
        }
      } else if (lockType == LOCK_TYPE.WRITE) {
        if (this.votes.get(itemName) < w) {
          tempLockList.add(item);
        }
      } else {
        throw new MattBradburyException();
      }
    }
    return tempLockList;
  }

  private void addVoteList(LinkedList<Lock> locklist, HashMap<String, Integer> votes) {
    for (int j=0; j<locklist.size(); j++) {
      Lock item = locklist.get(j);
      String itemName = item.getData();
      int numberOfVotes = votes.get(itemName);
      log("<LM> Item from the locklist is " + itemName + " and number of votes given are " + numberOfVotes);
      this.votes.put(itemName, this.votes.get(itemName) + numberOfVotes);
      log("<LM> The number of votes in the system for item " + itemName + " is now " + this.votes.get(itemName));
    }
  }

  private void addRejectedVoteList(LinkedList<Lock> locklist, HashMap<String, Integer> votes) {
    LinkedList<Lock> itemsToEnquire = new LinkedList<Lock>();
    for (int j=0; j<locklist.size(); j++) {
      Lock item = locklist.get(j);
      String itemName = item.getData();
      int numberOfVotes = votes.get(itemName);
      log("<LM> Item from the locklist is " + itemName + " and number of votes given are " + numberOfVotes);
      if (numberOfVotes >= 0) {
        this.votes.put(itemName, this.votes.get(itemName) + numberOfVotes);
      } else if (numberOfVotes == -1) {
        // here we want to send a message back to the server asking what its server number is - if its less
        // than ours well give it our votes - otherwise it will give us its votes - all wrapped into new method

        continue;
      }
      log("<LM> The number of votes in the system for item " + itemName + " is now " + this.votes.get(itemName));
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
