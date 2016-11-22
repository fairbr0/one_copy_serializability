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
      this.db = new Database(serverNumber);
      this.logger = logger;
      this.server = server;
      this.serverNumber = serverNumber;
  }

  public void setTransaction(LinkedList<String> transaction) {
    this.transaction = transaction;
  }

  public boolean runTransaction() throws IOException{
    VarList variables = new VarList();
    VarList writtenVars = new VarList();
    log("<dm> Locks all aquired. Running transaction");
    //todo : Copy the database file. Run each transaction line on the database file and
    // update as needed. Once read commit, write. If interupted, revert to the copy.

    for (String query : transaction) {
      String[] parts = query.split(" ");
      for (int i = 0; i < parts.length; i++) {
        System.out.println(parts[i]);
      }
      if (parts[0].equals("begin")) {
        //logic to save old version if needed
        log("<dm> Begin Transation " + parts[1]);
      }

      else if (parts[0].equals("read")) {
        String data = parts[1];
        //get value from the database
        int value = this.db.readDatabase(data);
        variables.setVar(data, value);
        log("<dm> Read " + data);
      }

      else if (parts[0].equals("write")) {
        int newValue = variables.getVar(parts[1]).value;
        String data = parts[1];
        String q = data + "=" + newValue;
        this.db.writeDatabase(q);
        writtenVars.setVar(data, newValue);
        log("<dm> Wrote " + data + " locally");
        //write to database
      }

      else if (parts[0].equals("commit")) {
        //logic to make results perminent
        log("<dm> Committing Transation");
        if (writtenVars.size() > 0) {
          boolean result = propogateWrites(writtenVars);
          //do something with result
        }
      }

      else {
        String data = parts[0];
        int value = Integer.parseInt(parts[2]);
        variables.setVar(data, value);
        log("<dm> Setting " + data + " to value " + value);
        // here are commands like x = 20. Will need to get clear list of arshad what can be
      }
    }
    ///need to change to return only modified values. Hack to make it compile
    return true;
  }

  public void listenWriteCommand() {
    Thread thread = new Thread(() -> {
      try {
        while (true) {
          Message m = server.getServerWriteMessage();
          VarList varlist = (VarList) m.getMessage();
          for (Var var : VarList.getVarList()) {
            this.db.writeDatabase(var.data, var.value);
          }
          Flag[] flags = {Flag.RSP, Flag.ACK};
          Message resp = new Message<String>(flags, "", this.serverNumber);
        }
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
  }

  public boolean propogateWrites(VarList vars) throws IOException {
    log("<dm> Propogating Writes to servers.");
    Flag[] flags = new Flag[1];
    flags[0] = Flag.WRITE;
    Message m = new Message<VarList>(flags, vars, this.serverNumber);

    int n = server.broadcast(m);

    for (int i = 0; i < n; i++) {
      Message rec = server.getServerResponseMessage();
      Flag[] recFlags = rec.getFlags();
      if (!FlagChecker.containsFlag(flags, Flag.ACK)) {
        throw new IOException();
      }
    }

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
