import java.io.IOException;

public class LoggerTester {

	Logger a;
	Logger b;

	public LoggerTester() {
		try {
			this.a = new Logger(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeLog (String message) {
		try {
			a.writeLog(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		LoggerTester test = new LoggerTester();
		test.writeLog("Test TEST");
		test.writeLog("Jarred is a cunt");
	}

}
