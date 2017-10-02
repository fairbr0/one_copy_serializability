import java.io.IOException;

public class DatabaseTester {

	private Database a;

	public DatabaseTester() {
		try {
			this.a = new Database(0, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeDatabase(String message){
		try {
			a.writeDatabase(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readDatabase(String x) {
		try {
			System.out.println(a.readDatabase(x));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		DatabaseTester test = new DatabaseTester();
		test.readDatabase("x");
		test.writeDatabase("x=10");
		test.readDatabase("x");
		test.readDatabase("y");
		test.writeDatabase("y=20");
		test.readDatabase("y");
	}
}
