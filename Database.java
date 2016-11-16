public class Database {
  public final String databaseFilePath;

  public Database(String databaseFilePath) {
    int i = Integer.parseInt(databaseFilePath.replaceAll("\\D", ""));
    this.databaseFilePath = "db"+Integer.toString(i)+".txt";
    if (!this.databaseExists()) {
      this.createDatabase();
    }
  }

  public final boolean databaseExists() {
    File f = new File(this.databaseFilePath);
    return f.exists() && !f.isDirectory();
  }

}
