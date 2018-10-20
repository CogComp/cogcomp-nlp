/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.depparse.core.DepInst;
import edu.illinois.cs.cogcomp.depparse.core.DepStruct;
import edu.illinois.cs.cogcomp.depparse.core.LabeledChuLiuEdmondsDecoder;
import edu.illinois.cs.cogcomp.depparse.features.LabeledDepFeatureGenerator;
import edu.illinois.cs.cogcomp.depparse.io.CONLLReader;
import edu.illinois.cs.cogcomp.depparse.io.Preprocessor;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.sl.core.*;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.HashMap;

public class MainClass {
    private static Logger logger = LoggerFactory.getLogger(MainClass.class);

    private static HashMap<String, HashMap<String, Integer>> confusionMatrix = new HashMap<>();
    private static boolean useGoldPOS;
    private static int conllIndexOffset = 0;

    public static void main(String args[]) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("MainClass", true);
        parser.addArgument("-r", "--train").type(String.class)
                .help("The training file in CoNLL format").setDefault("");
        parser.addArgument("-t", "--test").help("The test file in CoNLL format");
        parser.addArgument("-c", "--config").help("The SL configuration file")
                .setDefault("config/StructuredPerceptron.config");
        parser.addArgument("-m", "--model").help("The model output file").setDefault("out.model");
        parser.addArgument("-p", "--pos").help("The type of PoS tags to use")
                .choices("gold", "auto").setDefault("auto");
        parser.addArgument("-o", "--offset").type(Integer.class)
                .help("The offset of the pos/head/dep index for the CoNLL train/test files")
                .setDefault(0);
        parser.addArgument("-a", "--annotate")
                .type(String.class)
                .help("Annotate text file (one sentence per line) and print the output to the command line")
                .setDefault("");
        Namespace ns = parser.parseArgs(args);

        useGoldPOS = ns.getString("pos").equals("gold");
        logger.info("Using {} PoS tags", ns.getString("pos"));
        conllIndexOffset = ns.getInt("offset");
        if (!ns.getString("train").isEmpty()) {
            logger.info("Using {} configuration", ns.getString("config"));
            train(ns.getString("train"), ns.getString("config"), ns.getString("model"));
            logger.info("Testing on Training Data");
            test(ns.getString("model"), ns.getString("train"), false);
        }
        if (!ns.getString("test").isEmpty()) {
            logger.info("Testing on Test Data");
            test(ns.getString("model"), ns.getString("test"), true);
        }
        if (!ns.getString("annotate").isEmpty()) {
            annotate(ns.getString("annotate"));
        }
    }

    private static void annotate(String filepath) throws IOException {
        DepAnnotator annotator = new DepAnnotator();
        TextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, false));
        Preprocessor preprocessor = new Preprocessor();
        Files.lines(Paths.get(filepath)).forEach(line -> {
            TextAnnotation ta = taBuilder.createTextAnnotation(line);
            try {
                preprocessor.annotate(ta);
                annotator.addView(ta);
                System.out.println(ta.getView(annotator.getViewName()).toString());
            } catch (AnnotatorException e) {
                e.printStackTrace();
            }
        });
    }

    private static SLProblem getStructuredData(String filepath,
            LabeledChuLiuEdmondsDecoder infSolver) throws Exception {
        CONLLReader depReader = new CONLLReader(new Preprocessor(), useGoldPOS, conllIndexOffset);
        depReader.startReading(filepath);
        SLProblem problem = new SLProblem();
        DepInst instance = depReader.getNext();
        while (instance != null) {
            infSolver.updateInferenceSolver(instance);
            Pair<IInstance, IStructure> pair = getSLPair(instance);
            problem.addExample(pair.getFirst(), pair.getSecond());
            instance = depReader.getNext();
        }
        logger.info("{} of dependency instances.", problem.size());
        return problem;
    }

    private static SLModel train(String trainFile, String configFilePath, String modelFile) throws Exception {
		SLModel model = new SLModel();
		SLParameters para = new SLParameters();
		para.loadConfigFile(configFilePath);
		model.lm = new Lexiconer(true);
		if (model.lm.isAllowNewFeatures())
			model.lm.addFeature("W:unknownword");
		model.featureGenerator = new LabeledDepFeatureGenerator(model.lm);
		model.infSolver = new LabeledChuLiuEdmondsDecoder(model.featureGenerator);
		SLProblem problem = getStructuredData(trainFile, (LabeledChuLiuEdmondsDecoder)model.infSolver);
        ((LabeledChuLiuEdmondsDecoder) model.infSolver).saveDepRelDict();
		Learner learner = LearnerFactory.getLearner(model.infSolver, model.featureGenerator, para);
		learner.runWhenReportingProgress((w, inference) -> printMemoryUsage());
		model.wv = learner.train(problem);
		printMemoryUsage();
		model.lm.setAllowNewFeatures(false);
		model.saveModel(modelFile);
		return model;
	}

    private static void test(String modelPath, String testDataPath, boolean updateMatrix)
            throws Exception {
        SLModel model = SLModel.loadModel(modelPath);
        ((LabeledChuLiuEdmondsDecoder) model.infSolver).loadDepRelDict();
        SLProblem sp =
                getStructuredData(testDataPath, (LabeledChuLiuEdmondsDecoder) model.infSolver);
        double acc_undirected = 0.0;
        double acc_directed_unlabeled = 0.0;
        double acc_labeled = 0.0;
        double total = 0.0;

        long totalTime = 0L;
        int totalLength = 0;
        for (int i = 0; i < sp.instanceList.size(); i++) {
            DepInst sent = (DepInst) sp.instanceList.get(i);
            totalLength += sent.size();
            DepStruct gold = (DepStruct) sp.goldStructureList.get(i);
            long startTime = System.currentTimeMillis();
            DepStruct prediction = (DepStruct) model.infSolver.getBestStructure(model.wv, sent);
            totalTime += (System.currentTimeMillis() - startTime);
            IntPair tmp_undirected = evaluate(sent, gold, prediction, false, false, false);
            IntPair tmp_directed_unlabeled = evaluate(sent, gold, prediction, true, false, false);
            IntPair tmp_labeled = evaluate(sent, gold, prediction, true, true, updateMatrix);
            acc_undirected += tmp_undirected.getFirst();
            acc_directed_unlabeled += tmp_directed_unlabeled.getFirst();
            acc_labeled += tmp_labeled.getFirst();
            total += tmp_directed_unlabeled.getSecond();
        }

        System.out.println("Parsing time taken for " + sp.size()
                + " sentences with average length " + totalLength / sp.size() + ": " + totalTime);
        System.out.println("Average parsing time " + totalTime / sp.size());
        System.out.println("undirected acc " + acc_undirected);
        System.out.println("directed unlabeled acc " + acc_directed_unlabeled);
        System.out.println("labeled acc " + acc_labeled);
        System.out.println("total " + total);
        System.out.println("%age correct undirected " + (acc_undirected * 1.0 / total));
        System.out.println("%age correct directed & unlabeled "
                + (acc_directed_unlabeled * 1.0 / total));
        System.out.println("%age correct labeled " + (acc_labeled * 1.0 / total));
        if (updateMatrix)
            printMatrix();
        System.out.println("Done with testing!");
    }

    private static IntPair evaluate(DepInst sent, DepStruct gold, DepStruct pred, boolean directed,
									boolean labeled, boolean updateMatrix) {
		if (labeled && !directed)
			throw new IllegalArgumentException("Cannot evaluate labeled but undirected!");
		if (updateMatrix && !labeled)
			throw new IllegalArgumentException("Unlabeled confusion matrix does not exist!");
		int instanceLength = sent.size();
		int[] predHeads = pred.heads;
		int[] goldHeads = gold.heads;
		String[] predDeprels = pred.deprels;
		String[] goldDeprels = gold.deprels;
		int corr = 0; // count edge label
		int total = 0;
		
		for (int i = 1; i <= instanceLength; i++) {
			if (directed) {
				if (labeled) {
					if (predHeads[i] == goldHeads[i] && predDeprels[i].equals(goldDeprels[i]))
						corr++;
					if (updateMatrix) {
						confusionMatrix.computeIfAbsent(predDeprels[i], k -> new HashMap<>());
						if (confusionMatrix.get(predDeprels[i]).get(goldDeprels[i]) == null)
							confusionMatrix.get(predDeprels[i]).put(goldDeprels[i], 1);
						else {
							int count = confusionMatrix.get(predDeprels[i]).get(goldDeprels[i]);
							confusionMatrix.get(predDeprels[i]).put(goldDeprels[i], count + 1);
						}
					}
				}
				else {
					if (predHeads[i] == goldHeads[i])
						corr++;
				}
			}
			else {
				if (predHeads[goldHeads[i]] == i || predHeads[i] == goldHeads[i])
					corr++;
			}
			total++;
		}
		return new IntPair(corr, total);
	}

    private static Pair<IInstance, IStructure> getSLPair(DepInst instance) {
        DepStruct d = new DepStruct(instance);
        return new Pair<>(instance, d);
    }

    private static void printMatrix() {
        System.out.print(String.format("%-10s", ""));
        for (String gold : confusionMatrix.keySet())
            System.out.print(String.format("%-10s", gold));
        System.out.println();
        for (String pred : confusionMatrix.keySet()) {
            System.out.print(String.format("%-10s", pred));
            for (String gold : confusionMatrix.keySet()) {
                if (confusionMatrix.get(pred).get(gold) == null)
                    System.out.print(String.format("%-10s", 0));
                else
                    System.out.print(String.format("%-10s", confusionMatrix.get(pred).get(gold)));
            }
            System.out.println();
        }
    }

    private static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        NumberFormat nformat = NumberFormat.getInstance();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long allocatedMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = allocatedMemory - freeMemory;

        logger.debug("max memory: {} MB", nformat.format(maxMemory));
        logger.debug("used-up memory: {} MB", nformat.format(usedMemory));
        logger.debug("total free memory: {} MB",
                nformat.format(freeMemory + (maxMemory - allocatedMemory)));
    }
}
