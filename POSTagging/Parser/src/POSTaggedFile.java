import java.io.*;
import java.util.*;

/**
 *
 * @author Ray Mooney Methods for processing Linguistic Data Consortium
 *         (LDC,www.ldc.upenn.edu) data files that are tagged for Part Of Speech
 *         (POS). Converts tagged files into simple untagged Lists of sentences
 *         which are Lists of String tokens.
 */

public class POSTaggedFile {

	/** The name of the LDC POS file */
	public File file = null;
	/** Whether orthographic features should be used in parsing */
	public boolean useOrthographicFeatures = false;
	/** The I/O reader for accessing the file */
	protected BufferedReader reader = null;

	/** Create an object for a given LDC POS tagged file */
	public POSTaggedFile(File file) {
		this(file, false);
	}

	public POSTaggedFile(File file, boolean useOrthographicFeatures) {
		this.file = file;
		this.useOrthographicFeatures = useOrthographicFeatures;
		try {
			this.reader = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			System.out.println("\nCould not open POSTaggedFile: " + file);
			System.exit(1);
		}
	}

	/**
	 * Return the next line of POS tagged tokens from this file. Returns "\n" if
	 * end of sentence and start of a new one. Returns null if end of file
	 */
	protected String getNextPOSLine() {
		String line = null;
		try {
			do {
				// Read a line from the file
				line = reader.readLine();
				if (line == null) {
					// End of file, no more tokens, return null
					reader.close();
					return null;
				}
				// Sentence boundary indicator
				if (line.startsWith("======="))
					line = "\n";
				// If sentence number indicator for ATIS or comment for Brown,
				// ignore it
				if (line.startsWith("[ @") || line.startsWith("*x*"))
					line = "";
			} while (line.equals(""));
		} catch (IOException e) {
			System.out
					.println("\nCould not read from TextFileDocument: " + file);
			System.exit(1);
		}
		return line;
	}

	/**
	 * Take a line from the file and return a list of String tokens in the line
	 */
	protected List<String> getTokens(String line) {
		List<String> tokenList = new ArrayList<String>();
		line = line.trim();
		// Use a tokenizer to extract token/POS pairs in line,
		// ignore brackets indicating chunk boundaries
		StringTokenizer tokenizer = new StringTokenizer(line, " []");
		while (tokenizer.hasMoreTokens()) {
			String tokenPos = tokenizer.nextToken();
			tokenList.add(segmentToken(tokenPos));
		}
		return tokenList;
	}

	protected List<String> findOrthographicFeatures(String token) {
		List<String> features = new ArrayList<String>();
		if (Character.isUpperCase(token.charAt(0))) {
			features.add("caps");
		}
		if (Character.isDigit(token.charAt(0))) {
			features.add("number");
		}
		if (token.contains("-")) {
			features.add("hypen");
		}
		// Add common suffixes.
		Set<String> suffixes = new HashSet<String>();
		suffixes.add("able");
		suffixes.add("al");
		suffixes.add("ed");
		suffixes.add("er");
		suffixes.add("es");
		suffixes.add("est");
		suffixes.add("ful");
		suffixes.add("ic");
		suffixes.add("ing");
		suffixes.add("ive");
		suffixes.add("ing");
		suffixes.add("less");
		suffixes.add("ly");
		suffixes.add("ment");
		suffixes.add("ness");
		suffixes.add("ous");
		suffixes.add("s");
		suffixes.add("tion");
		for (String suffix : suffixes) {
			if (token.endsWith(suffix)) {
				features.add(suffix);
			}
		}
		return features;
	}

	/** Segment a token/POS string and return just the token */
	protected String segmentToken(String tokenPos) {
		if (!useOrthographicFeatures) {
			return tokenPos.replace('/', ' ');
		}
		String[] tokens = tokenPos.split("/");
		List<String> orthographicFeatures = findOrthographicFeatures(tokens[0]);
		StringBuilder builder = new StringBuilder();
		builder.append(tokens[0]);
		builder.append(' ');
		if (!orthographicFeatures.isEmpty()) {
			builder.append(String.join(" ", orthographicFeatures));
		}
		if (tokens.length > 1) {
			builder.append(' ');
			builder.append(tokens[1]);
		}
		System.out.println(builder.toString());
		return builder.toString();
	}

	/**
	 * Return a List of sentences each represented as a List of String tokens
	 * for the sentences in this file
	 */
	protected List<List<String>> tokenLists() {
		List<List<String>> sentences = new ArrayList<List<String>>();
		List<String> sentence = new ArrayList<String>();
		String line;
		while ((line = getNextPOSLine()) != null) {
			// Newline line indicates new sentence
			if (line.equals("\n")) {
				if (!sentence.isEmpty()) {
					// Save completed sentence
					sentences.add(sentence);
					// and start a new sentence
					sentence = new ArrayList<String>();
				}
			} else {
				// Get the tokens in the line
				List<String> tokens = getTokens(line);
				if (!tokens.isEmpty()) {
					// If last token is an end-sentence token "</S>"
					if (tokens.get(tokens.size() - 1).equals("</S>")) {
						// Then remove it
						tokens.remove(tokens.size() - 1);
						// and add final sentence tokens
						sentence.addAll(tokens);
						// Save completed sentence
						sentences.add(sentence);
						// and start a new sentence
						sentence = new ArrayList<String>();
					} else {
						// Add the tokens in the line to the current sentence
						sentence.addAll(tokens);
					}
				}
			}
		}
		// File should always end at end of a sentence
		assert (sentence.isEmpty());
		return sentences;
	}

	/**
	 * Take a list of LDC tagged input files or directories and convert them to
	 * a List of sentences each represented as a List of token Strings
	 */
	public static List<List<String>> convertToTokenLists(File[] files) {
		return convertToTokenLists(files, false);
	}
	public static List<List<String>> convertToTokenLists(File[] files,
			boolean useOrthoFeatures) {
		List<List<String>> sentences = new ArrayList<List<String>>();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (!file.isDirectory()) {
				if (!file.getName().contains("CHANGES.LOG"))
					sentences.addAll(
							new POSTaggedFile(file, useOrthoFeatures)
									.tokenLists());
			} else {
				File[] dirFiles = file.listFiles();
				sentences.addAll(
						convertToTokenLists(dirFiles, useOrthoFeatures));
			}
		}
		return sentences;
	}

	public static void writeToFile(List<List<String>> sentences,
			String outputFile) {
		try {
			FileWriter fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			for (List<String> sentence : sentences) {
				for (String token : sentence) {
					token = token + "\n";
					bw.write(token);
				}
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Convert LDC POS tagged files to just lists of tokens for each sentences
	 * and print them out.
	 */
	public static void main(String[] args) throws IOException {
		// File[] files = new File[args.length];
		// for (int i = 0; i < files.length; i++)
		// files[i] = new File(args[i]);
		// List<List<String>> sentences = convertToTokenLists(files);
		for (String arg : args) {
			System.out.println(arg + "\t");
		}
		File[] Files = new File[1];
		String inputPath = args[0];
		Files[0] = new File(inputPath);
		boolean useOrthographicFeatures = Boolean.parseBoolean(args[2]);
		List<List<String>> sentences = convertToTokenLists(Files,
				useOrthographicFeatures);
		System.out.println("# Sentences=" + sentences.size());
		String outputPath = args[1];
		writeToFile(sentences, outputPath);
		System.out.println(sentences);
	}

}
