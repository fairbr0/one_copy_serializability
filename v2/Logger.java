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
	private int serverNumber;

  public Logger(int serverNumber) throws IOException {
		this.serverNumber=serverNumber;
		this.logFilePath = "Log" + Integer.toString(serverNumber) + ".txt";
		if (!this.logExists()) {
			this.createLog();
		}
	}

	private final boolean logExists() {
    File f = new File(this.logFilePath);
    return f.exists() && !f.isDirectory();
  }

  private final void createLog() throws IOException {
    File f = new File(logFilePath);
    f.createNewFile();
		writeLog("This is the log file for "+ Integer.toString(serverNumber));
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
				log.println("<timestamp " + sdf.format(date) + "> <" + Integer.toString(serverNumber) + "> " + message);
				//System.out.println(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSS");
			System.err.println("<timestamp " + sdf.format(date) + "> <" + Integer.toString(serverNumber) + ">  Tried to write log but log file does not exist");
		}
	}

}
