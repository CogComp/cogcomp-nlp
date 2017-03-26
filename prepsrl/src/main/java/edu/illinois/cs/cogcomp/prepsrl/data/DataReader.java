/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.prepsrl.data;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.caches.TextAnnotationMapDBHandler;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLConfigurator;
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

    protected ResourceManager rm = PrepSRLConfigurator.defaults();

    public static final String CANDIDATE = "candidate";

    protected IResetableIterator<TextAnnotation> dataset;
    protected List<Constituent> candidates;
    private int currentCandidate, currentTextAnnotation;
    protected String viewName, corpusName;
    protected final String file;

    private static Preprocessor preprocessor;
    private final TextAnnotationMapDBHandler dbHandler;

    public DataReader(String file, String corpusName, String viewName) {
        this.file = file;
        this.viewName = viewName;
        this.corpusName = corpusName;
        this.candidates = new ArrayList<>();
        String cacheDBDir = "data-cached";
        if (!IOUtils.exists(cacheDBDir))
            IOUtils.mkdir(cacheDBDir);
        String cacheDB = cacheDBDir + File.separator + viewName + "-cache.db";
        dbHandler = new TextAnnotationMapDBHandler(cacheDB);

        if (!dbHandler.isCached(corpusName, cacheDB)) {
            logger.info("Dataset not cached.");
            List<TextAnnotation> textAnnotations = readData();
            int processed = 0;
            int total = textAnnotations.size();
            logger.info("Finished reading from {}.", this.file);
            for (TextAnnotation ta : textAnnotations) {
                try {
                    getPreprocessor().annotate(ta);
                } catch (AnnotatorException | RuntimeException e) {
                    logger.error("Unable to preprocess TextAnnotation {}. Skipping", ta.getId());
                    continue;
                }
                dbHandler.addTextAnnotation(corpusName, ta);
                processed++;
                if (processed % 1000 == 0)
                    logger.info("Processed {} of {} TextAnnotations", processed, total);
            }
            logger.info("Finished pre-processing {} TextAnnotations.", processed);
        }
        logger.info("Dataset cached.");
        dataset = dbHandler.getDataset(corpusName);
    }

    /**
     * Set the {@code dataset} source to a different part of the cached MapDB. To be used only after
     * the {@code dataset} has been cached (by constructing the {@link DataReader} object.
     *
     * @param corpusName The new part of the MapDB (usually either "train" or "test")
     */
    public void setDataset(String corpusName) {
        dataset = dbHandler.getDataset(corpusName);
    }

    private Preprocessor getPreprocessor() {
        if (preprocessor == null)
            preprocessor = new Preprocessor(rm);
        return preprocessor;
    }

    public abstract List<TextAnnotation> readData();

    public abstract List<Constituent> candidateGenerator(TextAnnotation ta);

    protected List<Constituent> getFinalCandidates(View goldView, List<Constituent> candidates) {
        List<Constituent> finalCandidates = new ArrayList<>();
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

    private Constituent getExactMatch(View view, Constituent c) {
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
            currentTextAnnotation++;
            if (!dataset.hasNext())
                return null;
            TextAnnotation ta = dataset.next();
            if (!ta.hasView(viewName))
                return next();
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
        candidates = new ArrayList<>();
        currentTextAnnotation = 0;
        dataset.reset();
    }

    @Override
    public void close() {
        dbHandler.close();
    }
}
