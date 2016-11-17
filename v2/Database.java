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

public class Database {

	private String databaseFilePath;
	private int serverNumber;

	public Database(int serverNumber) throws IOException {
		this.databaseFilePath = "Db"+ Integer.toString(serverNumber) +".txt";
		if (!this.databaseExists()) {
			this.createDatabase();
		}
	}

	private final boolean databaseExists() {
		File f = new File(this.databaseFilePath);
		return f.exists() && !f.isDirectory();
	}

	private final void createDatabase() throws IOException {
		File f = new File(databaseFilePath);
		f.createNewFile();
		System.out.println("Creating a new file");
		System.out.println(this.databaseFilePath);
		Path path = Paths.get(this.databaseFilePath, new String[0]);
		BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		try {
			writer.write("x=0");
			writer.newLine();
			writer.write("y=0");
		} finally {
			System.out.println("Write successfull");
			writer.close();
		}
	}

	public final int readDatabase(String item) throws IOException, FileNotFoundException{
		BufferedReader br = new BufferedReader(new FileReader(databaseFilePath));
		try {

			String line= br.readLine();

    		while (line!=null){
        	if ((line.split("=")[0]).equals(item)) {
						return Integer.parseInt(line.split("=")[1]);
					} else {
						line= br.readLine();
					}
    		}
				return -1;
		} finally {
				br.close();
		}
	}

	public final void writeDatabase(String object) throws  IOException {
		Path path = Paths.get(this.databaseFilePath, new String[0]);
		BufferedReader br = new BufferedReader(new FileReader(databaseFilePath));

		try {
			int counter=0;
			String[] lines = new String[2];
			String line= br.readLine();



			while (line!=null){
				System.out.println("While loop called");
				System.out.println("line = " + line.split("=")[0]);
				System.out.println("object = " + object.split("=")[0]);
			  if ((line.split("=")[0]).equals(object.split("=")[0])) {
					lines[counter] = object;
				} else {
					lines[counter] = line;
				}
				if(counter <1){
					counter++;
				}
				line= br.readLine();
				br.readLine();
			}

			BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

			for (int i=0; i<lines.length; i++){
				System.out.println(lines[i]);
				writer.write(lines[i]);
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
