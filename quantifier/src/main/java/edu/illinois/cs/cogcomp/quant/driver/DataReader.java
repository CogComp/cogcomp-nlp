/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The base data reader class containing the `LBJava` {@link Parser} code used by all applications.
 */
abstract public class DataReader implements Parser {
    protected static Logger logger = LoggerFactory.getLogger(DataReader.class);

    protected ResourceManager rm = PreprocessorConfigurator.defaults();

    public static final String CANDIDATE = "candidate";

    protected List<TextAnnotation> textAnnotations;
    protected List<Constituent> candidates;
    protected int currentCandidate, currentTextAnnotation;
    protected final String viewName;
    protected final String file;

    public static Preprocessor preprocessor;

    public DataReader(String file, String corpusName, String viewName) {
        this.file = getCorrectPath(file);
        this.viewName = viewName;
        this.candidates = new ArrayList<Constituent>();
        this.textAnnotations = new ArrayList<TextAnnotation>();
        List<TextAnnotation> rawAnnotations = readData();
        int processed = 0;
        int total = rawAnnotations.size();
        logger.info("Finished reading from {}.", this.file);
        for (TextAnnotation ta : rawAnnotations) {
            try {
                getPreprocessor().annotate(ta);
                textAnnotations.add(ta);
            } catch (Exception e) {
                logger.error("Unable to preprocess TextAnnotation {}. Skipping", ta.getId());
                continue;
            }
            processed++;
            if (processed % 1000 == 0)
                logger.info("Processed {} of {} TextAnnotations", processed, total);
        }
        logger.info("Finished pre-processing {} TextAnnotations.", processed);
    }

    private Preprocessor getPreprocessor() {
        if (preprocessor == null)
            preprocessor = new Preprocessor(rm);
        return preprocessor;
    }

    /**
     * A workaround for the unit tests in Maven that move the relative path of the root directory in
     * {@link ESRLConfigurator} to the directory of each module. <b>NB:</b> This code assumes the
     * default data directory to be <i>$ROOT_DIR/data/</i>.
     *
     * @param file The file/directory to be used
     * @return The same file/directory moved to the root dir of the main project
     */
    protected String getCorrectPath(String file) {
        if (file.contains("data") && !IOUtils.exists(new File(file).getParent())) {
            int dataIndex = file.indexOf("data") - 1;
            int prevSlashIndex = file.lastIndexOf(File.separator, dataIndex - 1);
            String fileWithParentDir =
                    file.substring(0, prevSlashIndex) + file.substring(dataIndex, file.length());
            logger.warn("{} doesn't exist, trying parent directory: {}.",
                    IOUtils.getFileName(file), fileWithParentDir);
            file = fileWithParentDir;
        }
        return file;
    }

    public abstract List<TextAnnotation> readData();

    protected void addGoldPOSView(TextAnnotation ta, List<String> sentencePOS) {
        TokenLabelView posView = new TokenLabelView(ViewNames.POS, ta);
        List<Constituent> constituents = ta.getView(ViewNames.TOKENS).getConstituents();
        assert constituents.size() == sentencePOS.size();
        for (int i = 0; i < constituents.size(); ++i) {
            Constituent constituent = (Constituent) constituents.get(i);
            posView.addTokenLabel(constituent.getStartSpan(), sentencePOS.get(i), 1.0D);
        }
        ta.addView(ViewNames.POS, posView);
    }

    protected void addGoldBIOView(TextAnnotation ta, List<String> labels) {
        SpanLabelView lightVerbView = new SpanLabelView(viewName, ta);
        int startSpan = -1;
        String prevLabel = null;
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            if (label.startsWith("B")) {
                startSpan = i;
                prevLabel = label;
            }
            if (label.startsWith("O") && startSpan != -1) {
                lightVerbView.addSpanLabel(startSpan, i, prevLabel.substring(2), 1.0);
                startSpan = -1;
            }
        }
        ta.addView(viewName, lightVerbView);
    }

    public abstract List<Constituent> candidateGenerator(TextAnnotation ta);

    protected List<Constituent> getFinalCandidates(View goldView, List<Constituent> candidates) {
        List<Constituent> finalCandidates = new ArrayList<Constituent>();
        for (Constituent c : candidates) {
            Constituent goldConst = getExactMatch(goldView, c);
            if (goldConst != null)
                finalCandidates.add(goldConst);
            else
                finalCandidates.add(new Constituent(CANDIDATE, viewName, c.getTextAnnotation(), c
                        .getStartSpan(), c.getEndSpan()));
        }
        for (Constituent c : goldView.getConstituents()) {
            if (!finalCandidates.contains(c))
                finalCandidates.add(c);
        }
        return finalCandidates;
    }

    protected Constituent getExactMatch(View view, Constituent c) {
        for (Constituent viewConst : view.getConstituents()) {
            if (viewConst.getSpan().equals(c.getSpan()))
                return viewConst;
        }
        return null;
    }

    /**
     * Fetches the next available data instance for training/testing. Also, pre-processes each new
     * {@link TextAnnotation} object before accessing its members.
     *
     * @return A {@link Constituent} (which might be a part of a {@link Relation}, depending on the
     *         type of {@link View} )
     */
    @Override
    public Object next() {
        if (candidates.isEmpty() || candidates.size() == currentCandidate) {
            if (currentTextAnnotation == textAnnotations.size())
                return null;
            TextAnnotation ta = textAnnotations.get(currentTextAnnotation);
            currentTextAnnotation++;
            candidates = candidateGenerator(ta);
            if (candidates.isEmpty())
                return next();
            currentCandidate = 0;
            if (currentTextAnnotation % 1000 == 0)
                logger.info("Read {} TextAnnotations", currentTextAnnotation);
        }
        return candidates.get(currentCandidate++);
    }

    @Override
    public void reset() {
        currentCandidate = 0;
        candidates = new ArrayList<Constituent>();
        currentTextAnnotation = 0;
    }

    @Override
    public void close() {

    }
}
