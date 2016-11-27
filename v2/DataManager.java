import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;
import java.io.IOException;
import java.io.Serializable;

public class DataManager {

  LinkedList<String> transaction;
  Database db;
  Logger logger;
  Server server;
  int serverNumber;

  public DataManager(int serverNumber, Logger logger, Server server) throws IOException {
      this.db = new Database(serverNumber, logger);
      this.logger = logger;
      this.server = server;
      this.serverNumber = serverNumber;
  }

  public void setTransaction(LinkedList<String> transaction) {
    this.transaction = transaction;
  }

  public LinkedList<String> runTransaction() throws IOException{
    VarList variables = new VarList();
    VarList writtenVars = new VarList();
    log("<DM> Locks all aquired. Running transaction");
    //todo : Copy the database file. Run each transaction line on the database file and
    // update as needed. Once read , write. If interupted, revert to the copy.

    for (String query : transaction) {
      String[] parts = query.split(" ");
      if (parts[0].equals("begin")) {
        //logic to save old version if needed
        log("<DM> Begin Transation " + parts[1]);
      }

      else if (parts[0].equals("read")) {
        String data = parts[1];
        //get value from the database
        int value = this.db.readDatabase(data);
        variables.setVar(data, value);
        log("<DM> Read " + data + " : " + value);
      }

      else if (parts[0].equals("write")) {
        int newValue = variables.getVar(parts[1]).value;
        String data = parts[1];
        String q = data + "=" + newValue;
        this.db.writeDatabase(q);
        writtenVars.setVar(data, newValue);
        log("<DM> Wrote " + data + " locally");
        //write to database
      }

      else if (parts[0].equals("commit")) {
        //logic to make results perminent
        log("<DM> Committing Transation");
        if (writtenVars.size() > 0) {
          boolean result = propogateWrites(writtenVars);

					//The system gets to here... now we want to do a two way handshake
          //do something with result
        }
      }

      else {
        String data = parts[0];
        int value = Integer.parseInt(parts[2]);
        variables.setVar(data, value);
        log("<DM> Setting " + data + " to value " + value);
        // here are commands like x = 20. Will need to get clear list of arshad what can be
      }
    }
    ///need to change to return only modified values. Hack to make it compile

    return varToStringConverter(writtenVars.getVarList());
  }

	private LinkedList<String> varToStringConverter(LinkedList<Var> varList) {
		LinkedList<String> list = new LinkedList<String>();
		Iterator<Var> it = varList.iterator();
		while (it.hasNext()) {
			list.add(it.next().data);
		}
		return list;
	}

  public void listenWriteCommand() {
    Thread thread = new Thread(() -> {
      try {
        while (true) {
					log("<DM> Got a listen write command");
          Message m = server.getServerWriteMessage();
          VarList varlist = (VarList) m.getMessage();
					int sender = m.serverNumber;
          for (Var var : varlist.getVarList()) {
            this.db.writeDatabase(var.data +"="+ Integer.toString(var.value));
						log("<DM> Wrote recieved changes to the database with data = " + var.data  + " and value = " + var.value);
          }
          Flag[] flags = {Flag.RSP, Flag.ACK};
          Message resp = new Message<String>(flags, "", this.serverNumber);
					this.server.requestToServer(resp, sender );
        	Thread.sleep(10);
        }
      } catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
			  e.printStackTrace();
			}
    });
		thread.start();
  }

  public boolean propogateWrites(VarList vars) throws IOException {
    log("<DM> Propogating Writes to servers.");
    Flag[] flags = new Flag[1];
    flags[0] = Flag.WRITE;
    Message m = new Message<VarList>(flags, vars, this.serverNumber);

    int n = server.broadcast(m);

    log("<DM> Waiting for ACK's");
    for (int i = 0; i < n; i++) {
      Message rec = server.getServerResponseMessage();
			log("<DM> " + rec.toString());
      Flag[] recFlags = rec.getFlags();
      if (!FlagChecker.containsFlag(recFlags, Flag.ACK)) {
        throw new IOException();
      }


    }

		log("<DM> Got ack from all servers");

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

class Var implements Serializable {
  String data;
  int value;

  public Var(String data, int value) {
    this.data = data;
    this.value = value;
  }

  public void set(int value) {
    this.value = value;
  }
}

class VarList implements Serializable {
  LinkedList<Var> vars;

  public VarList() {
    vars = new LinkedList<Var>();
  }

  public void setVar(String data, int value) {
    Iterator<Var> it = vars.iterator();
    while (it.hasNext()) {
      Var v = it.next();
      if (v.data.equals(data)) {
        v.set(value);
        return;
      }
    }
    Var v = new Var(data, value);
    vars.add(v);
    return;
  }

  public Var getVar(String data) {
    //get the value
    Iterator<Var> it = vars.iterator();
    while (it.hasNext()) {
      Var v = it.next();
      if (v.data.equals(data)) {
        return v;
      }
    }
    return null;
  }

  public LinkedList<Var> getVarList() {
    return this.vars;
  }

  public int size() {
    return vars.size();
  }
}
