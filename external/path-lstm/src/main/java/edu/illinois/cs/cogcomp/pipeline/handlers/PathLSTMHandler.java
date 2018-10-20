/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.edison.utilities.FrameData;
import edu.illinois.cs.cogcomp.edison.utilities.FramesManager;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Yield;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;

public class PathLSTMHandler extends Annotator {

    public static final String SRL_VERB_PATH_LSTM = "PATH_LSTM_SRL";

    private CompletePipeline SRLpipeline;

    private FramesManager propBankManager = null;
    private FramesManager nomBankManager = null;

    public PathLSTMHandler(boolean lazilyInitialize) {
        super(SRL_VERB_PATH_LSTM, new String[] {}, /* empty, because the required views are provided internally */
                lazilyInitialize);
    }

    private final static Logger log = LoggerFactory.getLogger(PathLSTMHandler.class);

    @Override
    public void initialize(ResourceManager rm) {
        try {
            // TODO: move the end-point url to the resource configurator
            Datastore ds = new Datastore("http://smaug.cs.illinois.edu:8080");
            File lemmaModel =
                    ds.getFile("org.cogcomp.mate-tools",
                            "CoNLL2009-ST-English-ALL.anna.lemmatizer.model", 3.3, false);
            File parserModel =
                    ds.getFile("org.cogcomp.mate-tools",
                            "CoNLL2009-ST-English-ALL.anna.parser.model", 3.3, false);
            File posModel =
                    ds.getFile("org.cogcomp.mate-tools",
                            "CoNLL2009-ST-English-ALL.anna.postagger.model", 3.3, false);
            File pathLSTM = ds.getFile("uk.ac.ed.inf", "pathLSTM.model", 1.0, false);
            // SRL pipeline options (currently hard-coded)
            String[] args =
                    new String[] {"eng", "-lemma", lemmaModel.getAbsolutePath(), "-parser",
                            parserModel.getAbsolutePath(), "-tagger", posModel.getAbsolutePath(),
                            "-srl", pathLSTM.getAbsolutePath(), "-reranker", "-externalNNs",};
            CompletePipelineCMDLineOptions options = new CompletePipelineCMDLineOptions();
            options.parseCmdLineArgs(args);
            try {
                SRLpipeline = CompletePipeline.getCompletePipeline(options);
            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (DatastoreException e) {
            e.printStackTrace();
        }

        try {
            this.propBankManager = new FramesManager(true);
            this.nomBankManager = new FramesManager(false);
        } catch (InvalidPortException | InvalidEndpointException | DatastoreException e) {
            e.printStackTrace();
        }
    }

    private PredicateArgumentView getSRL(TextAnnotation ta) throws Exception {
        log.debug("Input: {}", ta.getText());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        PredicateArgumentView pav =
                new PredicateArgumentView(viewName, "PathLSTMGenerator", ta, 1.0);

        int tokenOffset = 0;
        for(int sentIt = 0; sentIt < ta.getNumberOfSentences(); sentIt++) {
            log.info("Sentence " + sentIt + " out of " + ta.getNumberOfSentences() + " sentences. ");
            int finalSentIt = sentIt;
            int finalTokenOffset = tokenOffset;
            final Future future = executor.submit(
                    (Callable) () -> {
                        List<String> words = new LinkedList<String>();
                        words.add("<ROOT>"); // dummy ROOT token
                        words.addAll(Arrays.asList(ta.getSentence(finalSentIt).getTokens())); // pre-tokenized text

                        // run SRL
                        Sentence parsed = SRLpipeline.parse(words);

                        for (Predicate p : parsed.getPredicates()) {
                            // skip nominal predicates
                            // if (p.getPOS().startsWith("N")) continue;

                            IntPair predicateSpan = new IntPair(p.getIdx() - 1, p.getIdx());
                            String predicateLemma = p.getLemma();
                            Constituent predicate =
                                    new Constituent("Predicate." + p.getPOS(), viewName, ta, finalTokenOffset + predicateSpan.getFirst(),
                                            finalTokenOffset + predicateSpan.getSecond());
                            predicate.addAttribute(PredicateArgumentView.LemmaIdentifier, predicateLemma);
                            String sense = p.getSense();
                            String senseOnly = sense.split("\\.")[1];
                            predicate.addAttribute(PredicateArgumentView.SenseIdentifer, senseOnly);
                            FrameData.SenseFrameData frameData = null;
                            if(p.getPOS().startsWith("N")) {
                                // if it's noun, load from NomBank
                                frameData = nomBankManager.getFrameWithSense(predicateLemma, senseOnly);
                            }
                            else {
                                // otherwise, use propbank
                                frameData = propBankManager.getFrameWithSense(predicateLemma, senseOnly);
                            }
                            pav.addConstituent(predicate);

                            for (Word a : p.getArgMap().keySet()) {
                                Set<Word> singleton = new TreeSet<Word>();
                                String argTag = p.getArgumentTag(a);
                                String argDesc = (frameData != null) ? FramesManager.getArgDcrp(argTag, frameData): "";
                                Yield y = a.getYield(p, argTag, singleton);
                                IntPair span = new IntPair(y.first().getIdx() - 1, y.last().getIdx());
                                String argLabel = (argDesc.length() > 1) ? argTag + "." + argDesc : argTag;
                                Constituent c = new Constituent(argLabel, viewName, ta,
                                        finalTokenOffset + span.getFirst(), finalTokenOffset + span.getSecond());
                                assert span.getFirst() <= span.getSecond() : ta;
                                List<Constituent> consList = pav.getConstituentsWithSpan(
                                    new IntPair(finalTokenOffset + span.getFirst(), finalTokenOffset + span.getSecond()));
                                if (consList.isEmpty()) {
                                    pav.addConstituent(c);
                                }
                                else {
                                    c = consList.get(0);
                                }
                                pav.addRelation(new Relation(argLabel, predicate, c, 1.0));
                            }
                        }
                        return "ok";
                    });
            try {
                future.get(120, TimeUnit.SECONDS);
            } catch (TimeoutException ie) {
                log.error("Timeout in execution of PathLSTM . . . ");
            }
            tokenOffset += ta.getSentence(sentIt).getTokens().length;
        }
        executor.shutdown();
        return pav;
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        // Check if all required views are present
        try {
            View srlView = getSRL(ta);
            ta.addView(getViewName(), srlView);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AnnotatorException(e.getMessage());
        }
    }
}
