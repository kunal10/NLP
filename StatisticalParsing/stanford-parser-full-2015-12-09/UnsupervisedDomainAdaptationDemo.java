import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.EvaluateTreebank;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Timing;

public class UnsupervisedDomainAdaptationDemo {
	private MemoryTreebank selfTrainTreebank;
	private MemoryTreebank trainTreebank;
	private MemoryTreebank testTreebank;

	public UnsupervisedDomainAdaptationDemo() {
		this.trainTreebank = new MemoryTreebank("utf-8");
		this.selfTrainTreebank = new MemoryTreebank("utf-8");
		this.testTreebank = new MemoryTreebank("utf-8");
	}

	public MemoryTreebank getSelfTrainTreebank() {
		return selfTrainTreebank;
	}

	public MemoryTreebank getTrainTreebank() {
		return trainTreebank;
	}

	public MemoryTreebank getTestTreebank() {
		return testTreebank;
	}

	private MemoryTreebank makeMemoryTreebank(String treebankPath,
			FileFilter filt) {
		System.err.println(
				"Training a parser from treebank dir: " + treebankPath);
		MemoryTreebank trainTreebank = new MemoryTreebank("utf-8");
		System.err.print("Reading trees...");
		if (filt == null) {
			trainTreebank.loadPath(treebankPath);
		} else {
			trainTreebank.loadPath(treebankPath, filt);
		}

		Timing.tick("done [read " + trainTreebank.size() + " trees].");
		return trainTreebank;
	}

	private void tagAndAddUnsupervisedData(LexicalizedParser lp) {
		for (Tree t : selfTrainTreebank) {
			List<? extends HasWord> sen = t.yieldWords();
			Tree taggedSen = lp.apply(sen);
			trainTreebank.add(taggedSen);
		}
	}

	private void readSeedTreeBankForBrown(int seedSize, String seedPath) {
		MemoryTreebank fullSeedTreebank = new MemoryTreebank("utf-8");
		File[] files = new File(seedPath).listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				System.out.println("Generating Treebanks for Directory: "
						+ file.getName());
				MemoryTreebank treebank = makeMemoryTreebank(
						file.getAbsolutePath(), null);
				int numSentences = treebank.size();
				System.out.println("Num Sentences: " + numSentences);
				for (int i = 0; i < numSentences; i++) {
					if (i <= 0.9 * numSentences) {
						fullSeedTreebank.add(treebank.get(i));
					}
				}
			} else {
				System.out.println("File: " + file.getName());
			}
		}

		int numSentences = fullSeedTreebank.size();
		if (seedSize < 0) {
			for (int i = 0; i < numSentences; i++) {
				trainTreebank.add(fullSeedTreebank.get(i));
			}
		} else {
			// Add first seedSize sentences.
			List<Integer> indices = new ArrayList<>();
			for (int i = 0; i < numSentences; i++) {
				indices.add(i);
			}
			Collections.shuffle(indices);
			for (int i = 0; i < seedSize; i++) {
				trainTreebank.add(fullSeedTreebank.get(i));
			}
		}
	}

	private void readTrainingAndTestBanksForBrown(int selfTrainSize,
			String selfTrainPath) {
		MemoryTreebank fullSelfTrainTreebank = new MemoryTreebank("utf-8");
		File[] files = new File(selfTrainPath).listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				System.out.println("Generating Treebanks for Directory: "
						+ file.getName());
				MemoryTreebank treebank = makeMemoryTreebank(
						file.getAbsolutePath(), null);
				int numSentences = treebank.size();
				System.out.println("Num Sentences: " + numSentences);
				for (int i = 0; i < numSentences; i++) {
					if (i <= 0.9 * numSentences) {
						fullSelfTrainTreebank.add(treebank.get(i));
					} else {
						testTreebank.add(treebank.get(i));
					}
				}
			} else {
				System.out.println("File: " + file.getName());
			}
		}

		int numSentences = fullSelfTrainTreebank.size();
		if (selfTrainSize < 0) {
			for (int i = 0; i < numSentences; i++) {
				selfTrainTreebank.add(fullSelfTrainTreebank.get(i));
			}
		} else {
			// Select seedSize sentences randomly from entire seed set.
			List<Integer> indices = new ArrayList<>();
			for (int i = 0; i < numSentences; i++) {
				indices.add(i);
			}
			Collections.shuffle(indices);
			for (int i = 0; i < selfTrainSize; i++) {
				selfTrainTreebank.add(fullSelfTrainTreebank.get(i));
			}
		}
	}

	private void readSeedTreeBankForWsj(int seedSize, String seedPath) {
		MemoryTreebank fullSeedTreebank = makeMemoryTreebank(seedPath, null);
		int numSentences = fullSeedTreebank.size();
		System.out.println("Loaded Seed Treebank.");
		System.out.println("Num Sentences: " + numSentences);
		if (seedSize < 0) {
			for (int i = 0; i < numSentences; i++) {
				trainTreebank.add(fullSeedTreebank.get(i));
			}
		} else {
			// Add first seedSize sentences.
			for (int i = 0; i < seedSize; i++) {
				if (fullSeedTreebank.get(i) == null) {
					System.out.println("Could not fetch tree: " + i);
				} else {
					trainTreebank.add(fullSeedTreebank.get(i));
				}
			}
		}
	}

	private void readTrainingAndTestBanksForWsj(int selfTrainSize,
			String selfTrainPath, String testPath) {
		MemoryTreebank fullSelfTrainTreebank = makeMemoryTreebank(selfTrainPath,
				null);
		int numSentences = fullSelfTrainTreebank.size();
		if (selfTrainSize < 0) {
			for (int i = 0; i < numSentences; i++) {
				selfTrainTreebank.add(fullSelfTrainTreebank.get(i));
			}
		} else {
			// Add first selfTrainSize sentences.
			for (int i = 0; i < selfTrainSize; i++) {
				selfTrainTreebank.add(fullSelfTrainTreebank.get(i));
			}
		}
		testTreebank = makeMemoryTreebank(testPath, null);
	}

	public static void main(String[] args) {
		String seedPath = args[0];
		int seedSize = Integer.parseInt(args[1]);

		String selfTrainPath = args[2];
		int selfTrainSize = Integer.parseInt(args[3]);

		String testPath = null;
		if (args.length > 4) {
			testPath = args[4];
		}

		UnsupervisedDomainAdaptationDemo demo = new UnsupervisedDomainAdaptationDemo();

		// Read seed tree banks.
		if (seedPath.endsWith("brown")) {
			demo.readSeedTreeBankForBrown(seedSize, seedPath);
		} else {
			demo.readSeedTreeBankForWsj(seedSize, seedPath);
		}

		// Read self train and test tree banks.
		if (selfTrainPath.endsWith("brown")) {
			demo.readTrainingAndTestBanksForBrown(selfTrainSize, selfTrainPath);
		} else {
			demo.readTrainingAndTestBanksForWsj(selfTrainSize, selfTrainPath,
					testPath);
		}

		Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		op.setOptions("-goodPCFG", "-evals", "tsv");

		LexicalizedParser lp;

		// Train parser from seed data.
		System.out.println("Training parser on seed data.");
		lp = LexicalizedParser.trainFromTreebank(demo.getTrainTreebank(), op);
		// Evaluate accuarcy without unsupervised domain adaptation
		System.out
				.println("Evaluating accuracy without out of domain training");
		EvaluateTreebank evaluator = new EvaluateTreebank(lp);
		evaluator.testOnTreebank(demo.getTestTreebank());

		// Tag unsupervised domain data and add it to training tree bank.
		System.out.println("Tagging out of domain data");
		demo.tagAndAddUnsupervisedData(lp);

		// Train parser on larger data.
		System.out.println("Retraining the parser");
		lp = LexicalizedParser.trainFromTreebank(demo.getTrainTreebank(), op);
		// Evaluate accuracy after unsupervised domain adaptation.
		System.out.println("Evaluating after domain adaptation");
		evaluator = new EvaluateTreebank(lp);
		evaluator.testOnTreebank(demo.getTestTreebank());
	}
}
