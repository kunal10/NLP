package nlp.lm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nlp.lm.BigramModel.ModelType;

/**
 * A simple variant of Bigram Model which combines a forward and backward 
 * Bigram Model.
 */
public class BidirectionalBigramModel {

	public BidirectionalBigramModel() {
		fwdModel = new BigramModel(ModelType.FORWARD);
		bwdModel = new BigramModel(ModelType.BACKWARD);
	}
	
	public enum TestType { ALL, SHORT, LONG };

	// Train bi-directional model on passed sentences.
	public void train(List<List<String>> sentences) {
		fwdModel.train(sentences);
		bwdModel.train(sentences);
	}

	public void test(List<List<String>> sentences, TestType testType) {
		double totalLogProb = 0;
		double totalNumTokens = 0;
		int kSentenceThreshold = 5;
		for (List<String> sentence : sentences) {
			if ((testType == TestType.SHORT && 
					sentence.size() > kSentenceThreshold) || 
				(testType == TestType.LONG && 
					sentence.size() <= kSentenceThreshold)) {
				// Ignore short/long sentences depending on test type.
				continue;
			} 
			totalNumTokens += sentence.size();
			double sentenceLogProb = sentenceLogProb(sentence);
			// System.out.println(sentenceLogProb + " : " + sentence);
			totalLogProb += sentenceLogProb;
		}
		double perplexity = Math.exp(-totalLogProb / totalNumTokens);
		System.out.println("Word Perplexity = " + perplexity);
	}

	/**
	 * Compute log probability of sentence (excluding end) given current model.
	 */
	public double sentenceLogProb(List<String> sentence) {
		double[] fwdTokenProbs = fwdModel.sentenceTokenProbs(sentence);
		double[] bwdTokenProbs = bwdModel.sentenceTokenProbs(sentence);
		double totalLogProb = 0;
		int len = fwdTokenProbs.length;
		for (int i = 0; i < len; i++) {
			totalLogProb += Math.log(interpolatedProb(fwdTokenProbs[i], 
					bwdTokenProbs[len -i -1]));
		}
		return totalLogProb; 
	}

	/**
	 * Interpolate bi-directional using forward and backward model predictions.
	 */
	public double interpolatedProb(double fwdVal, double bwdVal) {
		// Linearly combine weighted fwd and bwd bigram probs
		return lambdaFwd * fwdVal + lambdaBwd * bwdVal;
	}

	private BigramModel fwdModel;
	private BigramModel bwdModel;
	// Interpolation constants for forward and backward models.
	// NOTE : Should sum to 1 for correct probabilistic model.
	// TODO : Experiment and figure out best values.
	private double lambdaFwd = 0.5;
	private double lambdaBwd = 0.5;

	public static void main(String[] args) throws IOException {
		// All but last arg is a file/directory of LDC tagged input data
		File[] files = new File[args.length - 1];
		for (int i = 0; i < files.length; i++)
			files[i] = new File(args[i]);

		// Last arg is the TestFrac
		double testFraction = Double.valueOf(args[args.length - 1]);
		// Get list of sentences from the LDC POS tagged input files
		List<List<String>> sentences = POSTaggedFile.convertToTokenLists(files);
		int numSentences = sentences.size();
		// Compute number of test sentences based on TestFrac
		int numTest = (int) Math.round(numSentences * testFraction);
		// Take test sentences from end of data
		List<List<String>> testSentences = sentences
				.subList(numSentences - numTest, numSentences);
		// Take training sentences from start of data
		List<List<String>> trainSentences = sentences.subList(0,
				numSentences - numTest);

		// Print Stats.
		System.out.println("# Train Sentences = " + trainSentences.size()
				+ " (# words = " + BigramModel.wordCount(trainSentences)
				+ ") \n# Test Sentences = " + testSentences.size()
				+ " (# words = " + BigramModel.wordCount(testSentences) + ")");

		// Create a bidirectional bigram model and train it.
		BidirectionalBigramModel model = new BidirectionalBigramModel();
		System.out.println("Training...");
		model.train(trainSentences);
		// Test on training data using test and test2
		model.test(trainSentences, TestType.ALL);
		System.out.println("Testing...");
		// Test on test data using test and test2
		model.test(testSentences, TestType.ALL);
	}

}
