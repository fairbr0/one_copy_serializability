import java.io.IOException;
import java.io.BufferedReader;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileNotFoundException;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {

	private String logFilePath;
  private String databaseFilePath;
	private String type;
	private int serverNumber;

  public Logger(String path, String type) throws IOException{
		if(type == "Server"){
			this.serverNumber=Integer.parseInt(path);
			this.type = type;
	    this.logFilePath = type + "Log"+path+".txt";
	    if (!this.logExists()) {
	      this.createLog();
	    }
	    this.databaseFilePath = type + "Db"+path+".txt";
	    if (!this.databaseExists()) {
	      this.createDatabase();
	    }
		} else {
			this.serverNumber=Integer.parseInt(path);
			this.serverNumber = this.serverNumber-9000;
			this.type = type;
			this.logFilePath = type + "Log"+ Integer.toString(this.serverNumber) +".txt";
			if (!this.logExists()) {
				System.out.println(this.logFilePath);
				this.createLog();
			}
		}
  }

  private final boolean logExists() {
    File f = new File(this.logFilePath);
    return f.exists() && !f.isDirectory();
  }

  private final void createLog() throws IOException {
    File f = new File(logFilePath);
    f.createNewFile();
		writeLog("This is the log file for " + type + " "+ serverNumber);
  }

  public final void writeDatabase(String message) throws  IOException {
		Path path = Paths.get(this.databaseFilePath, new String[0]);
		BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		try {
			writer.write(message);
			writer.flush();
		}
		finally {
			writer.close();
		}
	}

	public final void writeLog(String message) throws  IOException {
		if (logExists()) {
			try (
				Writer fw = new FileWriter(logFilePath, true);
				Writer bw = new BufferedWriter(fw);
				PrintWriter log = new PrintWriter(bw)
			) {
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSS");
				log.println("<timestamp " + sdf.format(date) + "> <"  + type + " " +serverNumber+ "> " + message);
				//System.out.println(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSS");
			System.err.println("<timestamp " + sdf.format(date) + "> <"  + type + " " +serverNumber+ ">  Tried to write log but log file does not exist");
		}
	}

  private final boolean databaseExists() {
    File f = new File(this.databaseFilePath);
    return f.exists() && !f.isDirectory();
  }

  private final void createDatabase() throws IOException {
    File f = new File(databaseFilePath);
    f.createNewFile();
		writeLog("0");
  }

  public final int queryDatabase() throws IOException {
		int value = 0;
		Path path = Paths.get(this.databaseFilePath, new String[0]);
		BufferedReader reader = Files.newBufferedReader(path);
		Throwable throwable = null;
		try {
			String line = reader.readLine();
			while (line != null) {
				String[] parts = line.split("=");
				value = Integer.parseInt(parts[1].replaceAll("\\s", ""));
				line = reader.readLine();
			}
		} catch (Throwable line) {
			throwable = line;
			throw line;
		} finally {
			if (reader != null) {
				if (throwable != null) {
					try {
						reader.close();
					} catch (Throwable line) {
						throwable.addSuppressed(line);
					}
				} else {
					reader.close();
				}
			}
		}
		writeLog("Query database response = " + (Object)value + ">\n");
		return value;
	}

	public final String readDatabase() throws IOException, FileNotFoundException{
		BufferedReader br = new BufferedReader(new FileReader(databaseFilePath));
    try {
        String line = br.readLine();
				return line;
    } finally {
        br.close();
    }
	}

}
