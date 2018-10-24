/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.bayraktar.BayraktarPatternLabeler;
import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.comma.lbj.ListCommasConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocativePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.OxfordCommaConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.SubstitutePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.readers.CommaParser;
import edu.illinois.cs.cogcomp.comma.readers.CommaParser.Ordering;
import edu.illinois.cs.cogcomp.comma.readers.PrettyCorpusReader;
import edu.illinois.cs.cogcomp.comma.sl.StructuredCommaClassifier;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser.SplitPolicy;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Run this first. It will print out the prediction performance of all the different models
 * evaluated through 5 fold cval
 * 
 * @author navari
 *
 */
public class ClassifierComparison {
    public static void main(String[] args) throws Exception {
        PrettyCorpusReader pcr =
                new PrettyCorpusReader(CommaProperties.getInstance().getCommaLabeledDataFile());
        CommaParser parser = new CommaParser(pcr.getSentences(), Ordering.ORDERED, true);
        System.out.println("GOLD GOLD");
        localCVal(true, true, parser, 250, 0.003, 0, 2.0, false);
        System.out.println("GOLD AUTO");
        localCVal(true, false, parser, 200, 0.003, 0, 2.0, false);
        System.out.println("AUTO AUTO");
        localCVal(false, false, parser, 250, 0.003, 0, 3.5, false);

        List<Classifier> lbjExtractors = new ArrayList<>();
        lbjExtractors.add(new LocalCommaClassifier().getExtractor());
        Classifier lbjLabeler = new LocalCommaClassifier().getLabeler();
        System.out.println("STRUCTURED GOLD");
        StructuredCommaClassifier goldStructured =
                new StructuredCommaClassifier(lbjExtractors, lbjLabeler);
        structuredCVal(goldStructured, parser, true, false);
        System.out.println("STRUCTURED AUTO");
        StructuredCommaClassifier autoStructured =
                new StructuredCommaClassifier(lbjExtractors, lbjLabeler);
        structuredCVal(autoStructured, parser, false, false);

        System.out.println("BAYRAKTAR GOLD");
        EvaluateDiscrete bayraktarGold = getBayraktarBaselinePerformance(parser, true);
        bayraktarGold.printPerformance(System.out);
        System.out.println("BAYRAKTAR AUTO");
        EvaluateDiscrete bayraktarAuto = getBayraktarBaselinePerformance(parser, false);
        bayraktarAuto.printPerformance(System.out);

        reasonForBelievingThatStructuredIsPerformingWorseDueToOverfitting(parser, false);

        printConstrainedClassifierPerformance(parser);
    }

    public static void printConstrainedClassifierPerformance(Parser parser) {
        List<Pair<Classifier, EvaluateDiscrete>> classifiers = new ArrayList<>();
        LocalCommaClassifier learner = new LocalCommaClassifier();
        EvaluateDiscrete unconstrainedPerformance = new EvaluateDiscrete();
        learner.setLTU(new SparseAveragedPerceptron(0.003, 0, 3.5));
        classifiers.add(new Pair<Classifier, EvaluateDiscrete>(
                new SubstitutePairConstrainedCommaClassifier(), new EvaluateDiscrete()));
        classifiers.add(new Pair<Classifier, EvaluateDiscrete>(
                new LocativePairConstrainedCommaClassifier(), new EvaluateDiscrete()));
        classifiers.add(new Pair<Classifier, EvaluateDiscrete>(
                new ListCommasConstrainedCommaClassifier(), new EvaluateDiscrete()));
        classifiers.add(new Pair<Classifier, EvaluateDiscrete>(
                new OxfordCommaConstrainedCommaClassifier(), new EvaluateDiscrete()));

        int k = 5;
        parser.reset();
        FoldParser foldParser = new FoldParser(parser, k, SplitPolicy.sequential, 0, false);
        for (int i = 0; i < k; foldParser.setPivot(++i)) {
            foldParser.setFromPivot(false);
            foldParser.reset();
            learner.forget();
            BatchTrainer bt = new BatchTrainer(learner, foldParser);
            Lexicon lexicon = bt.preExtract(null);
            learner.setLexicon(lexicon);
            bt.train(250);
            learner.save();
            foldParser.setFromPivot(true);
            foldParser.reset();
            unconstrainedPerformance.reportAll(EvaluateDiscrete.evaluateDiscrete(learner,
                    learner.getLabeler(), foldParser));
            for (Pair<Classifier, EvaluateDiscrete> pair : classifiers) {
                foldParser.reset();
                pair.getSecond().reportAll(
                        EvaluateDiscrete.evaluateDiscrete(pair.getFirst(), learner.getLabeler(),
                                foldParser));
            }
        }

        for (Pair<Classifier, EvaluateDiscrete> pair : classifiers) {
            System.out.println(pair.getFirst().name + " " + pair.getSecond().getOverallStats()[2]);
        }
    }

    public static EvaluateDiscrete structuredCVal(StructuredCommaClassifier model, Parser parser,
            boolean useGoldFeatures, boolean testOnTrain) throws Exception {
        Comma.useGoldFeatures(useGoldFeatures);
        int k = 5;
        parser.reset();
        FoldParser foldParser = new FoldParser(parser, k, SplitPolicy.sequential, 0, false);
        EvaluateDiscrete cvalResult = new EvaluateDiscrete();
        for (int i = 0; i < k; foldParser.setPivot(++i)) {
            foldParser.setFromPivot(false);
            foldParser.reset();
            LinkedHashSet<CommaSRLSentence> trainSentences = new LinkedHashSet<>();
            for (Object comma = foldParser.next(); comma != null; comma = foldParser.next()) {
                trainSentences.add(((Comma) comma).getSentence());
            }
            model.train(new ArrayList<>(trainSentences), null);
            if (!testOnTrain)
                foldParser.setFromPivot(true);
            foldParser.reset();
            LinkedHashSet<CommaSRLSentence> testSentences = new LinkedHashSet<>();
            for (Object comma = foldParser.next(); comma != null; comma = foldParser.next()) {
                testSentences.add(((Comma) comma).getSentence());
            }
            EvaluateDiscrete evaluator = model.test(new ArrayList<>(testSentences), null);
            cvalResult.reportAll(evaluator);
        }
        cvalResult.printPerformance(System.out);

        return cvalResult;
    }

    public static EvaluateDiscrete localCVal(boolean trainOnGold, boolean testOnGold,
            Parser parser, int learningRounds, double learningRate, double threshold,
            double thickness, boolean testOnTrain) {
        int k = 5;
        LocalCommaClassifier learner = new LocalCommaClassifier();
        learner.setLTU(new SparseAveragedPerceptron(learningRate, threshold, thickness));
        parser.reset();
        final FoldParser foldParser = new FoldParser(parser, k, SplitPolicy.sequential, 0, false);
        EvaluateDiscrete performanceRecord = new EvaluateDiscrete();
        for (int i = 0; i < k; foldParser.setPivot(++i)) {
            foldParser.setFromPivot(false);
            foldParser.reset();
            learner.forget();
            BatchTrainer bt = new BatchTrainer(learner, foldParser);
            Comma.useGoldFeatures(trainOnGold);
            Lexicon lexicon = bt.preExtract(null);
            learner.setLexicon(lexicon);
            bt.train(learningRounds);
            if (!testOnTrain)
                foldParser.setFromPivot(true);
            foldParser.reset();
            Comma.useGoldFeatures(testOnGold);
            EvaluateDiscrete currentPerformance =
                    EvaluateDiscrete.evaluateDiscrete(learner, learner.getLabeler(), foldParser);
            performanceRecord.reportAll(currentPerformance);
        }
        // System.out.println(performanceRecord.getOverallStats()[2]);
        performanceRecord.printPerformance(System.out);
        // performanceRecord.printConfusion(System.out);
        return performanceRecord;
    }

    /**
     * prints Bayraktar baseline performance based on only those commas whose Bayraktar patterns
     * have been annotated
     */
    public static EvaluateDiscrete getBayraktarBaselinePerformance(Parser parser, boolean testOnGold) {
        parser.reset();
        EvaluateDiscrete bayraktarEvaluation = new EvaluateDiscrete();
        Comma comma;
        while ((comma = (Comma) parser.next()) != null) {
            if (!BayraktarPatternLabeler.isLabelAvailable(comma))
                continue;
            Comma.useGoldFeatures(true);
            String goldLabel = comma.getLabel();
            Comma.useGoldFeatures(testOnGold);
            String bayraktarPrediction = comma.getBayraktarLabel();
            bayraktarEvaluation.reportPrediction(bayraktarPrediction, goldLabel);
        }
        return bayraktarEvaluation;
    }

    /**
     * Structured's higher performance on the train set and lower performance on test set is
     * indicative of overfitting
     */
    public static void reasonForBelievingThatStructuredIsPerformingWorseDueToOverfitting(
            Parser parser, boolean useGoldFeatures) throws Exception {
        List<Classifier> lbjExtractors = new ArrayList<>();
        lbjExtractors.add(new LocalCommaClassifier().getExtractor());
        Classifier lbjLabeler = new LocalCommaClassifier().getLabeler();
        StructuredCommaClassifier structured =
                new StructuredCommaClassifier(lbjExtractors, lbjLabeler);

        int learningRounds = 250;
        double learningRate = 0.003;
        double threshold = 0;
        double thickness = 3.5;

        EvaluateDiscrete structuredPerformanceOnTrainSet =
                structuredCVal(structured, parser, useGoldFeatures, true);
        EvaluateDiscrete structuredPerformanceOnTestSet =
                structuredCVal(structured, parser, useGoldFeatures, false);
        EvaluateDiscrete localPerformanceOnTrainSet =
                localCVal(useGoldFeatures, useGoldFeatures, parser, learningRounds, learningRate,
                        threshold, thickness, true);
        EvaluateDiscrete localPerformanceOnTestSet =
                localCVal(useGoldFeatures, useGoldFeatures, parser, learningRounds, learningRate,
                        threshold, thickness, false);


        System.out.println("Structured performance on train set "
                + structuredPerformanceOnTrainSet.getOverallStats()[2]);
        System.out.println("Structured performance on test set "
                + structuredPerformanceOnTestSet.getOverallStats()[2]);
        System.out.println("Local performance on train set "
                + localPerformanceOnTrainSet.getOverallStats()[2]);
        System.out.println("Localperformance on test set "
                + localPerformanceOnTestSet.getOverallStats()[2]);

    }
}
