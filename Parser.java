import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Parser {

	public Parser(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public void parse() {
		try {
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);

			FileWriter fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("=")) {
					continue;
				}
				String[] tokens = line.split(" ");
				for (String token : tokens) {
					// Ignore start and end tokens.
					if (token.startsWith("[") || token.endsWith("]")) {
						continue;
					}
					if (token.startsWith("@")) {
						bw.write("\n");
						continue;
					}
					token = token.replace('/', ' ');
					bw.write(token + "\n");
				}				
			}

			// Close the buffered reader/writer
			br.close();
			bw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFile = "/Users/kunal/Workspace/NLP/POSTagging/data/atis/atis3.pos";
		String outputFile = "/Users/kunal/Workspace/NLP/POSTagging/data/atis/atis3.txt";
		Parser p = new Parser(inputFile, outputFile);
		p.parse();
	}

	private String inputFile;
	private String outputFile;
}
