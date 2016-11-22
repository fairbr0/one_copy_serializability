import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;
import java.io.IOException;

public class DataManager {

  LinkedList<String> transaction;
  Database db;
  Logger logger;
  Server server;

  public DataManager(int serverNumber, Logger logger, Server server) throws IOException {
      this.db = new Database(serverNumber);
      this.logger = logger;
      this.server = server;
  }

  public void setTransaction(LinkedList<String> transaction) {
    this.transaction = transaction;
  }

  public void runTransaction() throws IOException{
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

  }

  public boolean propogateWrites(VarList vars) {
    log("<dm> Propogating Writes to servers.");
    Flag[] flags = new Flag[1];
    flags[0] = Flag.WRITE;
    Message m = new Message<VarList>(flags, vars);

    int n = server.broadcast(m);

    for (int i = 0; i < n; i++) {
      Message m = server.getServerResponseMessage();
      Flag[] flags = m.getFlags();
      if (!containsFlag(flags, Flag.ACK)) {
        throw new Exception();
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

class Var {
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

class VarList {
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
}
