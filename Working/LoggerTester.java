import java.io.IOException;

public class LoggerTester {

	public static void main (String args[]) throws IOException {
		Logger logger = new Logger("0", "Server");
		logger.writeLog("Jarred is a cunt");
		logger.writeLog("Matt is an even bigger cunt");
		logger.writeLog("Lets make CS246 great again");
		logger.writeDatabase("10");
		logger.writeDatabase("10");
		System.out.println(logger.readDatabase());
		System.out.println("All working!!!");
	}
}
