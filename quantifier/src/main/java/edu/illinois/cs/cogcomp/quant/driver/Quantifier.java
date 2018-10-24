/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.quant.lbj.*;
import edu.illinois.cs.cogcomp.quant.standardize.Normalizer;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class Quantifier extends Annotator {

    public Normalizer normalizer;
    public static Pattern wordSplitPat[];
    public static TextAnnotationBuilder taBuilder;
    private String dataDir = "data";
    private String modelsDir = "models";
    private String modelName = modelsDir + File.separator + new QuantitiesClassifier();

    private QuantitiesClassifier chunker;

    static {
        StatefulTokenizer tokenizer = new StatefulTokenizer();
        taBuilder = new TokenizerTextAnnotationBuilder(tokenizer);
    }

    public Quantifier() {
        // lazily initialize by default.
        this(true);
    }

    /**
     * Constructor allowing choice whether or not to lazily initialize.
     *
     * @param lazilyInitialize if 'true', load models only on first call to
     *        {@link Annotator#getView(TextAnnotation)}
     */
    public Quantifier(boolean lazilyInitialize) {
        super(ViewNames.QUANTITIES, new String[0], lazilyInitialize);
    }

    public static String wordsplitSentence(String sentence) {
        Matcher matcher;
        matcher = wordSplitPat[0].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), "- " + matcher.group(1));
        }
        matcher = wordSplitPat[1].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + " -");
        }
        matcher = wordSplitPat[2].matcher(sentence);
        while (matcher.find()) {
            sentence =
                    sentence.replace(matcher.group(), matcher.group(1) + " - " + matcher.group(2));
        }
        matcher = wordSplitPat[3].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + matcher.group(2));
        }
        matcher = wordSplitPat[4].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), "$ " + matcher.group(1));
        }
        matcher = wordSplitPat[5].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + " $");
        }
        matcher = wordSplitPat[6].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + " %");
        }
        return sentence;
    }

    /**
     * @param inputTA the tokenized annotation of text input. If this parameter is not available,
     *        the user can pass null, in which case we will tokenize it ourselves.
     */
    public List<QuantSpan> getSpans(String text, boolean standardized, TextAnnotation inputTA)
            throws AnnotatorException {
        TextAnnotation annotation =
                (inputTA != null) ? inputTA : taBuilder.createTextAnnotation(text);
        List<QuantSpan> quantSpans = new ArrayList<QuantSpan>();
        String[] sentences = new String[annotation.getNumberOfSentences()];
        for (int i = 0; i < annotation.getNumberOfSentences(); ++i) {
            sentences[i] = annotation.getSentence(i).getText();
        }

        // if there is no annotator, initialize it
        if (DataReader.preprocessor == null) {
            DataReader.preprocessor = new Preprocessor(PreprocessorConfigurator.defaults());
        }

        // if it does not include POS or NER_CONLL, add them
        DataReader.preprocessor.annotate(annotation);
        assert annotation.getAvailableViews().contains(ViewNames.POS);
        String previous = "";
        String chunk = "";
        boolean inChunk = false;
        String prediction = "";
        int startPos = 0, endPos = 0, tokenPos = 0;
        List<Constituent> tokens = annotation.getView(ViewNames.TOKENS).getConstituents();
        for (int i = 0; i < tokens.size(); ++i) {
            prediction = chunker.discreteValue(tokens.get(i));
            if (prediction.startsWith("B-") || prediction.startsWith("I-")
                    && !previous.endsWith(prediction.substring(2))) {
                if (!inChunk && tokenPos < annotation.size()) {
                    inChunk = true;
                    startPos = annotation.getTokenCharacterOffset(tokenPos).getFirst();
                }
            }
            if (inChunk) {
                chunk += tokens.get(i).getSurfaceForm() + " ";
            }
            if (!prediction.equals("O")
                    && tokenPos < annotation.size()
                    && (i == (tokens.size() - 1)
                            || chunker.discreteValue(tokens.get(i + 1)).equals("O")
                            || chunker.discreteValue(tokens.get(i + 1)).startsWith("B-") || !chunker
                            .discreteValue(tokens.get(i + 1)).endsWith(prediction.substring(2)))) {

                endPos = annotation.getTokenCharacterOffset(tokenPos).getSecond() - 1;
                QuantSpan span = new QuantSpan(null, startPos, endPos);
                try {
                    if (standardized) {
                        span.object =
                                normalizer.parse(chunk, chunker.discreteValue(tokens.get(i))
                                        .substring(2));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (span.object != null)
                    quantSpans.add(span);
                inChunk = false;
                chunk = "";
            }
            previous = prediction;
            if (tokenPos < annotation.size()
                    && annotation.getToken(tokenPos).trim()
                            .endsWith(tokens.get(i).getSurfaceForm().trim())) {
                tokenPos++;
            }
        }
        return quantSpans;
    }

    public String getAnnotatedString(String text, boolean standardized) throws Exception {
        String ans = "";
        TextAnnotation ta = taBuilder.createTextAnnotation(text);
        List<QuantSpan> quantSpans = getSpans(text, standardized, null);
        int quantIndex = 0;
        for (int i = 0; i < ta.size(); ++i) {
            if (quantSpans.get(quantIndex).start == ta.getTokenCharacterOffset(i).getFirst()) {
                ans += " [ ";
            }
            ans += ta.getToken(i) + " ";
            if (quantSpans.get(quantIndex).end == ta.getTokenCharacterOffset(i).getSecond()) {
                ans += " ] " + quantSpans.get(quantIndex) + " ";
                if (quantIndex < quantSpans.size() - 1)
                    quantIndex++;
            }
        }
        return ans;
    }

    @Override
    public void initialize(ResourceManager resourceManager) {
        System.out.println("Initializing Quantifier . . . ");
        chunker =
                new QuantitiesClassifier("models/QuantitiesClassifier.lc",
                        "models/QuantitiesClassifier.lex");
        normalizer = new Normalizer();
        wordSplitPat = new Pattern[25];
        // Dashes
        wordSplitPat[0] = Pattern.compile("-(\\D)");
        wordSplitPat[1] = Pattern.compile("(\\S)-");
        wordSplitPat[2] = Pattern.compile("(\\d)-(\\d|\\.\\d)");
        // Remove commas from within numbers
        wordSplitPat[3] = Pattern.compile("(\\d),(\\d)");
        // Remove dollar signs
        wordSplitPat[4] = Pattern.compile("\\$(\\d)");
        wordSplitPat[5] = Pattern.compile("(\\d)\\$");
        // Percentages
        wordSplitPat[6] = Pattern.compile("(\\d)%");
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        assert (ta.hasView(ViewNames.SENTENCE));
        SpanLabelView quantifierView =
                new SpanLabelView(ViewNames.QUANTITIES, "illinois-quantifier", ta, 1d);
        List<QuantSpan> quantSpans = getSpans(ta.getTokenizedText(), true, ta);
        for (QuantSpan span : quantSpans) {
            int startToken = ta.getTokenIdFromCharacterOffset(span.start);
            int endToken = ta.getTokenIdFromCharacterOffset(span.end);
            quantifierView.addSpanLabel(startToken, endToken, span.object.toString(), 1d);
        }
        ta.addView(ViewNames.QUANTITIES, quantifierView);
    }

    public void trainOnAll() {
        QuantitiesClassifier classifier =
                new QuantitiesClassifier(modelName + ".lc", modelName + ".lex");
        QuantitiesDataReader trainReader =
                new QuantitiesDataReader(dataDir + "/allData.txt", "train");
        BatchTrainer trainer = new BatchTrainer(classifier, trainReader);
        trainer.train(45);
        classifier.save();
    }

    public void train() {
        QuantitiesClassifier classifier =
                new QuantitiesClassifier(modelName + ".lc", modelName + ".lex");
        QuantitiesDataReader trainReader =
                new QuantitiesDataReader(dataDir + "/train.txt", "train");
        BatchTrainer trainer = new BatchTrainer(classifier, trainReader);
        trainer.train(45);
        classifier.save();
    }

    public void test() {
        QuantitiesClassifier classifier =
                new QuantitiesClassifier(modelName + ".lc", modelName + ".lex");
        QuantitiesDataReader testReader = new QuantitiesDataReader(dataDir + "/test.txt", "test");
        TestDiscrete tester = new TestDiscrete();
        tester.addNull("O");
        TestDiscrete
                .testDiscrete(tester, classifier, new QuantitiesLabel(), testReader, true, 1000);
    }

    public static void main(String args[]) throws Throwable {
        Quantifier quantifier = new Quantifier();
        quantifier.trainOnAll();
        quantifier.test();
    }
}
