/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main;

/**
 * Created by nitishgupta on 4/1/16.
 */


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.pos.LBJavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

/**
 * Wraps the Illinois Chunker (Shallow Parser) in an illinois-core-utilities Annotator
 * 
 * @author James Clarke, Mark Sammons, Nitish Gupta
 *
 */

public class ChunkerAnnotator extends Annotator {
    private static final String NAME = ChunkerAnnotator.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(ChunkerAnnotator.class);
    private Chunker tagger;
    private String posfield = ViewNames.POS;
    private String tokensfield = ViewNames.TOKENS;
    private String sentencesfield = ViewNames.SENTENCE;


    /**
     * default: don't use lazy initialization
     */
    public ChunkerAnnotator() {
        this(false);
    }

    /**
     * Constructor parameter allows user to specify whether or not to lazily initialize.
     *
     * @param lazilyInitialize If set to 'true', models will not be loaded until first call
     *        requiring Chunker annotation.
     */
    public ChunkerAnnotator(boolean lazilyInitialize) {
        this(lazilyInitialize, new ChunkerConfigurator().getDefaultConfig());
    }

    public ChunkerAnnotator(boolean lazilyInitialize, ResourceManager rm) {
        super(ViewNames.SHALLOW_PARSE, new String[] {ViewNames.POS}, lazilyInitialize, new ChunkerConfigurator().getConfig(rm));
    }

    @Override
    public void initialize(ResourceManager rm) {
        tagger = new Chunker(rm.getString(ChunkerConfigurator.MODEL_PATH.key),rm.getString(ChunkerConfigurator.MODEL_LEX_PATH.key));
    }


    @Override
    public void addView(TextAnnotation record) throws AnnotatorException {
        if (!record.hasView(tokensfield) || !record.hasView(sentencesfield)
                || !record.hasView(posfield)) {
            String msg = "Record must be tokenized, sentence split, and POS-tagged first.";
            logger.error(msg);
            throw new AnnotatorException(msg);
        }

        List<Constituent> tags = record.getView(posfield).getConstituents();
        List<Token> lbjTokens = LBJavaUtils.recordToLBJTokens(record);

        View chunkView = new SpanLabelView(ViewNames.SHALLOW_PARSE, this.NAME, record, 1.0);

        int currentChunkStart = 0;
        int currentChunkEnd = 0;

        String clabel = "";
        Constituent previous = null;
        int tcounter = 0;
        for (Token lbjtoken : lbjTokens) {
            Constituent current = tags.get(tcounter);
            tagger.discreteValue(lbjtoken);
            logger.debug("{} {}", lbjtoken.toString(), (null == lbjtoken.type) ? "NULL"
                    : lbjtoken.type);

            // what happens if we see an Inside tag -- even if it doesn't follow a Before tag
            if (null != lbjtoken.type && lbjtoken.type.charAt(0) == 'I') {
                if (lbjtoken.type.length() < 3)
                    throw new IllegalArgumentException("Chunker word label '" + lbjtoken.type
                            + "' is too short!");
                if (null == clabel) // we must have just seen an Outside tag and possibly completed
                                    // a chunk
                {
                    // modify lbjToken.type for later ifs
                    lbjtoken.type = "B" + lbjtoken.type.substring(1);
                } else if (clabel.length() >= 3 && !clabel.equals(lbjtoken.type.substring(2))) {
                    // trying to avoid mysterious null pointer exception...
                    lbjtoken.type = "B" + lbjtoken.type.substring(1);
                }
            }
            if ((lbjtoken.type.charAt(0) == 'B' || lbjtoken.type.charAt(0) == 'O')
                    && clabel != null) {

                if (previous != null) {
                    currentChunkEnd = previous.getEndSpan();
                    Constituent label =
                            new Constituent(clabel, ViewNames.SHALLOW_PARSE, record,
                                    currentChunkStart, currentChunkEnd);
                    chunkView.addConstituent(label);
                    clabel = null;
                } // else no chunk in progress (we are at the start of the doc)
            }

            if (lbjtoken.type.charAt(0) == 'B') {
                currentChunkStart = current.getStartSpan();
                clabel = lbjtoken.type.substring(2);
            }
            previous = current;
            tcounter++;
        }
        if (clabel != null && null != previous) {
            currentChunkEnd = previous.getEndSpan();
            Constituent label =
                    new Constituent(clabel, ViewNames.SHALLOW_PARSE, record, currentChunkStart,
                            currentChunkEnd);
            chunkView.addConstituent(label);
        }
        record.addView(ViewNames.SHALLOW_PARSE, chunkView);

        return; // chunkView;
    }

    @Override
    public String getViewName() {
        return ViewNames.SHALLOW_PARSE;
    }

    /**
     * Can be used internally by {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService} to
     * check for pre-requisites before calling any single (external)
     * {@link edu.illinois.cs.cogcomp.annotation.Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by
     *         this ViewGenerator
     */
    @Override
    public String[] getRequiredViews() {
        return new String[] {ViewNames.POS};
    }

    /**
     * Return possible tag values that the ChunkerAnnotator can produce.
     *
     * @return the set of string representing the tag values
     */
    @Override
    public Set<String> getTagValues() {
        if (!isInitialized()) {
            doInitialize();
        }
        Lexicon labelLexicon = tagger.getLabelLexicon();
        Set<String> tagSet = new HashSet();
        for (int i =0; i < labelLexicon.size(); ++i) {
            tagSet.add(labelLexicon.lookupKey(i).getStringValue());
        }
        return tagSet;
    }


}
