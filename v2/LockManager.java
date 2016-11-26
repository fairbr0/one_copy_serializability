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
  private Boolean lockedVotesX;
  private Boolean lockedVotesY;
  private Logger logger;
	private Boolean inTransaction;
	private int transactionNumber;

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
		this.inTransaction = false;
		this.transactionNumber = 0;
  }

  public boolean getLocks(LinkedList<Lock> locklist, int transactionNumber) throws IOException {
    //to implement : the quorum protocol
    this.locklist = locklist;
		this.transactionNumber = transactionNumber;
    log("<lm> About to request votes for locks: " + locklist.toString());
    //prepare the message to request lock
    for (Lock l : locklist) {
			if (!this.votes.containsKey(l.getData())) {
				this.votes.put(l.getData(), this.initalVotes);
			}
    }

		while (!checkVotes()) {

	    Flag[] flags = new Flag[2];
	    flags[0] = Flag.GETVOTES;
	    flags[1] = Flag.REQ;

	    Message<LinkedList<Lock>> message;

	    for (int i = 0; i < server.numOfServers; i++) {
					LinkedList<Lock> tempLockList = new LinkedList<Lock>();
					log("The size of the tempLockList is " + Integer.toString(locklist.size()));
					for (int k=0; k<locklist.size(); k++) {
						//log("At position " + k);
						Lock item = locklist.get(k);
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

					locklist = tempLockList;
					if (locklist.size() != 0) {
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

							for (int j=0; j<locklist.size(); j++) {
								Lock item = locklist.get(j);
								String itemName = item.getData();
								int numberOfVotes = payload.get(itemName);
								log("Item from the locklist is " + itemName + " and number of votes given are " + numberOfVotes);
								this.votes.put(itemName, this.votes.get(itemName) + numberOfVotes);
								log("The number of votes in the system for item " + itemName + " is now " + this.votes.get(itemName));
							}

			      } else if (FlagChecker.containsFlag(responseFlags, Flag.REJ)) {
			        //if read, check if rej due to read or write.
			        // if read, then continue
			        // if write, then wait and retry.

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

							boolean execute = false;
							LinkedList<Lock> itemsToEnquire = new LinkedList<Lock>();
							for (int j=0; j<locklist.size(); j++) {
								Lock item = locklist.get(j);
								String itemName = item.getData();
								int numberOfVotes = payload.get(itemName);
								log("Item from the locklist is " + itemName + " and number of votes given are " + numberOfVotes);
								if (numberOfVotes > 0) {
									this.votes.put(itemName, this.votes.get(itemName) + numberOfVotes);
								} else if (numberOfVotes == -1) {
									// here we want to send a message back to the server asking what its server number is - if its less
									// than ours well give it our votes - otherwise it will give us its votes - all wrapped into new method
									execute = true;
									itemsToEnquire.add(item);
									log("<lm> Someone has votes that they wont give us... bastards, will show em");
								} else if(numberOfVotes == -2) {
									log("<lm> The poor bastard has no votes :(");
									try{
										log("SLEEPPPINGGGG");
										//Thread.sleep(1000);
										log("SLEEPPPINGGGG complete");
									} catch (Exception e) {

									}
									continue;
									// The server has no votes therefore continue to persue other servers.
								} else {
									throw new MattBradburyException("A server has sent back an integer that is <-2 or 0 WTF...");
								}
								log("The number of votes in the system for item " + itemName + " is now " + this.votes.get(itemName));
							}

							if (execute) {
									log("We have a server that doesnt want to give us its vote... now to show em...");
									checkServerNumbers(response.serverNumber, itemsToEnquire);
							}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			      } else {
			        throw new MattBradburyException("Another Matt boo boo");
			      }
					}
	    }
			log("The number of votes that I have for data item X is " + this.votes.get("x"));
		}
		this.inTransaction = true;

    return true;
  }

	public boolean checkVotes() {

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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void checkServerNumbers(int serverCollisionNumber, LinkedList<Lock> list) throws IOException {
		//send a message asking what their server number is
		Flag[] flags = new Flag[2];
		flags[0] = Flag.SERVERNUMBER;
		flags[1] = Flag.REQ;
		Message<String> newMessage = new Message<String>(flags, "Server Number Request", this.serverNumber);
		log("<lm> sending a message to the person who has votes but wont surrender them to find out what hes playing at " + newMessage.toString());
		server.requestToServer(newMessage, serverCollisionNumber);

		//get the response integer number
		Message<Integer> response = server.getServerResponseMessage();
		log("<lm> Recieved in check server number method: " + response.toString());
		Flag[] responseFlags = response.getFlags();
		Integer payload = response.getMessage();

		Flag[] flagsnew = new Flag[2];
		//if he has a lower server number than me - send him all my votes for those items
		if (payload < this.serverNumber) {
			//send my votes
			flagsnew[0] = Flag.SENDINGVOTES;
			flagsnew[1] = Flag.REQ;

		} else { //Otherwise my server number is lower than his - therefore get bitch to send me all my votes
			//get them to give me their votes
			flagsnew[0] = Flag.GIVEVOTES;
			flagsnew[1] = Flag.REQ;
			Message<LinkedList<Lock>> newM = new Message<LinkedList<Lock>>(flagsnew, list, this.serverNumber);
			log("<lm> sending a message to the person who has votes but wont surrender them to find out what hes playing at " + newMessage.toString());
			server.requestToServer(newM, serverCollisionNumber);

			//Take the recived message of their votes and add the votes to the system....
			Message<Integer> responsenew = server.getServerResponseMessage();
			log("<lm> Recieved in check server number method: " + responsenew.toString());
			Flag[] responseFlagsb = responsenew.getFlags();
			Integer payloadnew = responsenew.getMessage();
			//now add the votes - unimplemented
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public void processRequestMessages() throws IOException {
		Thread thread = new Thread(() -> {
      try {
				//have changed this so that it only runs when a we are not in a transaction.
				while (!inTransaction) {
	        log("<lm> Listening for a vote request");
	        Message m = server.getServerRequestMessage();
	        int serverNumber = m.serverNumber;
	        Message newMessage;
	        HashMap<String, Integer> votesToSend;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					if (FlagChecker.containsFlag(m.flags, Flag.SERVERNUMBER)) {
						//A server is requesting what our server number is
						Flag[] flags = new Flag[2];
						flags[0] = Flag.ACK;//???
						flags[1] = Flag.RSP;//???

						newMessage = new Message<Integer>(flags, this.serverNumber, this.serverNumber);
	          log("<lm> Responding to server with votes: " + newMessage.toString());
	          server.requestToServer(newMessage, serverNumber);
					} else if (FlagChecker.containsFlag(m.flags, Flag.GIVEVOTES)) {
						Flag[] flags = new Flag[2];
						flags[0] = Flag.ACK;//???
						flags[1] = Flag.RSP;//???

					} else if (FlagChecker.containsFlag(m.flags, Flag.SENDINGVOTES)) {
						Flag[] flags = new Flag[2];
						flags[0] = Flag.ACK;//???
						flags[1] = Flag.RSP;//???

					}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					else if (FlagChecker.containsFlag(m.flags, Flag.GETVOTES)) {
						Message<LinkedList<Lock>> n = (Message<LinkedList<Lock>>) m;
	          log("<lm> Got vote request from " + n.serverNumber + " for " + n.getMessage().toString());
	          LinkedList<Lock> payload  = n.getMessage();

	          Iterator<Lock> itter = payload.iterator();

						Flag[] flags = new Flag[2];
						flags[0] = Flag.ACK;
						flags[1] = Flag.RSP;

	          votesToSend = new HashMap<String, Integer>();
						int count = 0;
	          while (itter.hasNext()){
							count++;
	            Lock item = itter.next();

							//Send -1 when it has got votes but it doesnt want to surrender them
							//Send -2 when it has not got any votes to give :(

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	            //need to store what transaction sent message, and what for (r/w).
	            //then can tell sender on rej why, and also change who gets the locks.
	            if(!votes.containsKey(item.getData())) {
	              //can give votes but its never seen it before
	              votes.put(item.getData(), 0);
	              votesToSend.put(item.getData(), this.initalVotes);
	              log("<lm> Sending votes for item " + item.getData());
	            } else if (votes.containsKey(item.getData()) && (votes.get(item.getData()) != 0)) {
	              //lockedVotes = true;

								if (this.inTransaction) {
		              flags[0] = Flag.REJ;
									votesToSend.put(item.getData(), -1);
								} else if (!this.inTransaction) {
									votesToSend.put(item.getData(), 0);
									votesToSend.replace(item.getData(), votes.get(item.getData()));
									votes.replace(item.getData(), 0);
								} else {
									log("hgjefgv,kshdmbhds");
								}

	              //send the person votes
	              log("<lm> Sending votes for item vgfjyfjh " + item.getData());
	            } else if (votes.containsKey(item.getData())) {
	              flags[0] = Flag.REJ;
	              //its seen it before but it has no votes
								votesToSend.put(item.getData(), -2);
	              //votesToSend.replace(item.getData(), -2);
	              //send a rejection

	              log("<lm> Not Sending votes for item " + item.getData());
	            } else {
								log("jhdbjvbhm");
	              throw new MattBradburyException("Unrecognised type");
	            }

	          }


	          newMessage = new Message<HashMap>(flags, votesToSend, this.serverNumber);
	          log("<lm> Responding to server with votes: " + newMessage.toString() + " " + Integer.toString(votesToSend.size()) + " counter = " + count);
						log("the values in the votes to send hashmap are " + votesToSend.entrySet());
	          server.requestToServer(newMessage, serverNumber);
	        } else if (FlagChecker.containsFlag(m.flags, Flag.RESET)) {
						//this.votes = new HashMap<String, Integer>();
						Message<LinkedList<String>> n = (Message<LinkedList<String>>) m;
						LinkedList<String> payload  = n.getMessage();
						Iterator<String> itter = payload.iterator();

						while (itter.hasNext()){
							String itemName = itter.next();
							log("reset for item " + itemName);
							this.votes.put(itemName, 1);
						}
						//here we dont want to be redeclaring the hash map we just want to get the object and reset the values to 0.

						log("<lm> Hashmap size is now " + Integer.toString(this.votes.size()));
					}
	        Thread.sleep(100);

				}
			} catch (InterruptedException e) {
        e.printStackTrace();
      } catch (IOException io) {
        io.printStackTrace();
      }

    });
		thread.start();
	}

  public boolean releaseLocks(LinkedList<String> listToReset) {
    // values is a list of values which were updated in the transaction.
    // must propogate the update
		Flag[] flags = new Flag[2];
		flags[0] = Flag.RESET;
		flags[1] = Flag.REQ;
		Message<LinkedList<String>> m = new Message(flags, listToReset, this.serverNumber);
		server.broadcast(m);
		//this.votes = new HashMap<String, Integer>();
		//LinkedList<String>
		Iterator<String> i = listToReset.iterator();
		while (i.hasNext()) {
			String variable = i.next();
			this.votes.put(variable, this.initalVotes);
		}
		log("<lm>Hashmap size is now " + Integer.toString(this.votes.size()));
    log("<lm> Releasing Locks");
		log("The number of votes I have for variable X is " + this.votes.get("x"));
		this.inTransaction = false;
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
