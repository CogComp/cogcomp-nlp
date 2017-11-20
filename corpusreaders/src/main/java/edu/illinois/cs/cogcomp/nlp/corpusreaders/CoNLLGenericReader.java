/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;


/**
 *
 * This reads column format CoNLL data, customizable by extending it.
 *
 * Created by xle2 on 11/1/2017.
 */
public abstract class CoNLLGenericReader extends AnnotationReader<TextAnnotation> {

    protected static Logger logger = LoggerFactory.getLogger(CoNLLGenericReader.class);
    protected List<TextAnnotation> textAnnotations;

    /**
     * This expects a directory that contains CoNLL format files.
     */
    public CoNLLGenericReader(String corpusName, String conllDirectory) {
        super(CorpusReaderConfigurator.buildResourceManager(corpusName, conllDirectory, conllDirectory, ".conll", ".conll"));
        this.currentAnnotationId = 0;
        this.textAnnotations = new ArrayList<>();

        String corpusDirectory =
                this.resourceManager.getString(CorpusReaderConfigurator.SOURCE_DIRECTORY.key);

        String[] files;
        try {
            files = IOUtils.isFile(corpusDirectory) ?
                    new String[] {corpusDirectory} :
                    IOUtils.lsFiles(corpusDirectory);
        } catch (IOException e) {
            logger.error("Error listing directory.");
            logger.error(e.getMessage());
            return;
        }

        Arrays.sort(files);
        try {
            for (String file : files) {
                textAnnotations.add(loadCoNLLFile(file));
            }
        } catch (IOException e) {
            logger.error("Error reading file.");
            logger.error(e.getMessage());
        }
    }

    /**
     * Defines the useful columns for processing of the CoNLL format file
     */
    protected static class CoNLLColumnConfig {
        public int tokenColumn;  // Column number of original token
        public List<Integer> tokenLabelColumns;  // Column numbers of token labels, such as LEMMA, POS
        public List<Integer> iobColumns;  // Column numbers of IOB format span labels, such as SHALLOW_PARSE and NER

        public CoNLLColumnConfig(
                int tokenColumn, List<Integer> tokenLabelColumns, List<Integer> iobColumns) {
            this.tokenColumn = tokenColumn;
            this.tokenLabelColumns = tokenLabelColumns;
            this.iobColumns = iobColumns;
        }
    }

    /**
     * @return A definition of the useful columns
     */
    protected abstract CoNLLColumnConfig getColumnConfig();

    /**
     * Produce a List of span labels when fed with span starts and stops
     * A span label is stored as a Pair of a token position and a token label
     */
    protected static class SpanCollector {
        protected int startPosition = Integer.MAX_VALUE;
        protected String currentValue = null;
        protected List<Pair<IntPair, String>> collectedSpans = new ArrayList<>();

        public void startSpan(int position, String value) {
            stopSpan(position);
            startPosition = position;
            currentValue = value;
        }

        public void stopSpan(int position) {
            if (position > startPosition) {
                collectedSpans.add(new Pair<>(new IntPair(startPosition, position), currentValue));
                startPosition = Integer.MAX_VALUE;
            }
        }

        public List<Pair<IntPair, String>> collect() {
            return collectedSpans;
        }

        public Stream<Pair<IntPair, String>> stream() {
            return collect().stream();
        }
    }

    /**
     * Process lists of token labels and span labels to build the TextAnnotation
     * @param ta The TextAnnotation to be built
     * @param tokenLabelStreams A list of token labels, stored as a Pair of a token position and a token label
     * @param spanCollectors A span label collector
     * @param filename The source of the token and span labels
     */
    protected abstract void processViews(
            TextAnnotation ta,
            Map<Integer, List<Pair<Integer, String>>> tokenLabelStreams,
            Map<Integer, SpanCollector> spanCollectors,
            String filename);

    /**
     * Helper for create a TokenLabelView from a stream of token labels
     */
    protected int createTokenLabelView(
            Stream<Pair<Integer, String>> tokenLabels,
            TextAnnotation ta,
            String viewName) {
        TokenLabelView view = new TokenLabelView(viewName, "GoldStandard", ta, 1.0);
        tokenLabels.forEach(label -> view.addTokenLabel(label.getFirst(), label.getSecond(), 1.0));
        ta.addView(viewName, view);
        return view.count();
    }

    /**
     * Helper for create a SpanLabelView from a stream of span labels
     */
    protected int createSpanLabelView(
            Stream<Pair<IntPair, String>> spans,
            TextAnnotation ta,
            String viewName,
            boolean allowOverlapping) {
        SpanLabelView view = new SpanLabelView(viewName, "GoldStandard", ta, 1.0, allowOverlapping);
        spans.forEach(span -> view.addSpanLabel(
                span.getFirst().getFirst(), span.getFirst().getSecond(), span.getSecond(), 1.0));
        ta.addView(viewName, view);
        return view.count();
    }

    /**
     * This loads a CoNLL column format file into a TextAnnotation.
     * Scans through IOB columns.
     *
     * @throws FileNotFoundException
     */
    protected TextAnnotation loadCoNLLFile(String filename) throws FileNotFoundException {
        CoNLLColumnConfig columnConfig = getColumnConfig();

        List<String> tokens = new ArrayList<>();
        SpanCollector sentences = new SpanCollector();

        Map<Integer, List<Pair<Integer, String>>> tokenLabelLists = new HashMap<>();
        Map<Integer, SpanCollector> spanCollectors = new HashMap<>();
        for (Integer column : columnConfig.tokenLabelColumns) {
            tokenLabelLists.put(column, new ArrayList<>());
        }
        for (Integer column : columnConfig.iobColumns) {
            spanCollectors.put(column, new SpanCollector());
        }

        sentences.startSpan(0, ViewNames.SENTENCE);

        for (String line : LineIO.read(filename)) {
            line = line.trim();

            int currentTokenId = tokens.size();

            if (line.isEmpty()) {
                sentences.stopSpan(currentTokenId);
                sentences.startSpan(currentTokenId, ViewNames.SENTENCE);
                continue;
            }

            String[] parts = line.split("\t");

            String token = parts[columnConfig.tokenColumn];
            if (token.equals("-DOCSTART-") || token.trim().isEmpty()) {
                continue;
            }
            tokens.add(token);

            for (Integer column : columnConfig.tokenLabelColumns) {
                tokenLabelLists.get(column).add(new Pair<>(currentTokenId, parts[column]));
            }

            for (Integer column : columnConfig.iobColumns) {
                String part = parts[column];
                SpanCollector spanCollector = spanCollectors.get(column);
                if (part.startsWith("B-") || part.startsWith("U-")) {
                    // two consecutive entities.
                    spanCollector.stopSpan(currentTokenId);
                    spanCollector.startSpan(currentTokenId, part.split("-")[1]);
                }
                else if (part.startsWith("I-")) {
                    // don't do anything....
                }
                else if (part.startsWith("O") || part.startsWith("_")){
                    spanCollector.stopSpan(currentTokenId);
                }
            }
        }

        sentences.stopSpan(tokens.size());
        for (Integer column : columnConfig.iobColumns) {
            spanCollectors.get(column).stopSpan(tokens.size());
        }



        // we jump through these hoops so we can give the TA an id.
        String filenameOnly = IOUtils.getFileName(filename);
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(
                this.corpusName, filenameOnly, Collections.singletonList(tokens.toArray(new String[0])));

        createSpanLabelView(sentences.collect().stream(), ta, ViewNames.SENTENCE, false);

        processViews(ta, tokenLabelLists, spanCollectors, filename);

        return ta;
    }

    @Override
    protected void initializeReader() {}

    @Override
    public boolean hasNext() {
        return textAnnotations.size() > currentAnnotationId;
    }

    @Override
    public TextAnnotation next() {
        return textAnnotations.get(currentAnnotationId++);
    }

    @Override
    public String generateReport() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("Number of TextAnnotations generated: ")
                .append(textAnnotations.size())
                .append(System.lineSeparator());
        builder
                .append("Check the log for further details.")
                .append(System.lineSeparator());
        return builder.toString();
    }
}
