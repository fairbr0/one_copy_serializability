public class DataManager {

  String databasePath;
  LinkedList<String> transaction;

  public DataManager(String databasePath) {
      this.databasePath = databasePath;
  }

  public void setTranaction(LinkedList<String> transaction) {
    this.transaction = transaction;
  }

  public VarList runTransaction() {
    VarList variables = new VarList();
    //todo : Copy the database file. Run each transaction line on the database file and
    // update as needed. Once read commit, write. If interupted, revert to the copy.

    for (String query : transaction) {
      String[] parts = query.split(" ");
      if (parts[0].equals("begin")) {
        //logic to save old version if needed
      }

      if (parts[0].equals("read")) {
        String data = parts[1];
        //get value from the database
        variables.setVar(data, value);
      }

      if (parts[0].equals("write")) {
        int newValue = variables.getVar(parts[1]);
        //write to database
      }

      if (parts[0].equals("commit")) {
        //logic to make results perminent
      }

      else {
        // here are commands like x = 20. Will need to get clear list of arshad what can be
      }
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

  public VarList {
    vars = new HashSet<Var>();
  }

  public void setVar(Data string, int value) {
    Iterator<Var> it = vars.iterator();
    while (it.hasNext()) {
      Var v = it.next();
      if (v.data.equals(string)) {
        v.set(value);
        return;
      }
    }
    Var v = new Var(data, value);
    vars.add(v);
    return;
  }

  public Var getVar(Data string) {
    //get the value
    Iterator<Var> it = vars.iterator();
    while (it.hasNext()) {
      Var v = it.next();
      if (v.data.equals(string)) {
        return v.value;
      }
    }
    return null;
  }
}
