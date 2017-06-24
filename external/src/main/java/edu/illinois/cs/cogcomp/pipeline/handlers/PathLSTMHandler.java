/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PathLSTMHandler extends Annotator {

    public static final String SRL_VERB_PATH_LSTM = "SRL_VERB_PATH_LSTM";

    private CompletePipeline SRLpipeline;

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
    }

    private PredicateArgumentView getSRL(TextAnnotation ta) throws Exception {
        log.debug("Input: {}", ta.getText());

        PredicateArgumentView pav =
                new PredicateArgumentView(viewName, "PathLSTMGenerator", ta, 1.0);

        for(int sentIt = 0; sentIt < ta.getNumberOfSentences(); sentIt++) {
            log.info("Sentence " + sentIt + " out of " + ta.getNumberOfSentences() + " sentences. ");
            List<String> words = new LinkedList<String>();
            words.add("<ROOT>"); // dummy ROOT token
            words.addAll(Arrays.asList(ta.getSentence(sentIt).getTokens())); // pre-tokenized text

            // run SRL
            Sentence parsed = SRLpipeline.parse(words);

            for (Predicate p : parsed.getPredicates()) {
                // skip nominal predicates
                if (p.getPOS().startsWith("N"))
                    continue;

                IntPair predicateSpan = new IntPair(p.getIdx() - 1, p.getIdx());
                String predicateLemma = p.getLemma();

                Constituent predicate =
                        new Constituent("Predicate", viewName, ta, predicateSpan.getFirst(),
                                predicateSpan.getSecond());
                predicate.addAttribute(PredicateArgumentView.LemmaIdentifier, predicateLemma);

                String sense = p.getSense();
                predicate.addAttribute(PredicateArgumentView.SenseIdentifer, sense);

                List<Constituent> args = new ArrayList<>();
                List<String> relations = new ArrayList<>();

                for (Word a : p.getArgMap().keySet()) {

                    Set<Word> singleton = new TreeSet<Word>();
                    String label = p.getArgumentTag(a);
                    Yield y = a.getYield(p, label, singleton);
                    IntPair span = new IntPair(y.first().getIdx() - 1, y.last().getIdx());

                    assert span.getFirst() <= span.getSecond() : ta;
                    args.add(new Constituent(label, viewName, ta, span.getFirst(), span.getSecond()));
                    relations.add(label);
                }

                pav.addPredicateArguments(predicate, args,
                        relations.toArray(new String[relations.size()]), new double[relations.size()]);

            }
        }

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
