/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

import java.util.*;

/**
 * The {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService} object with <i>most</i> of the
 * annotators available in CCG Curator.
 * <p>
 * Do not call this class directly; instead, use
 * {@link edu.illinois.cs.cogcomp.curator.CuratorFactory}
 * <p>
 * TODO Until a caching mechanism is available in illinois-core-utilities, this AnnotatorService
 * will not support caching
 *
 * @author Christos Christodouloupoulos
 * @author Narender Gupta
 */
public class CuratorAnnotatorService implements AnnotatorService {

    private static TextAnnotationBuilder taBuilder;
    private final Map<String, Annotator> viewProviders;


    /**
     * Overloaded constructor with default configuration.
     * 
     * @see edu.illinois.cs.cogcomp.curator.CuratorAnnotatorService#CuratorAnnotatorService(ResourceManager)
     */
    protected CuratorAnnotatorService() throws Exception {
        this(new CuratorConfigurator().getDefaultConfig());
    }

    /**
     * Builds an {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService} object that contains
     * all {@code Curator} components under
     * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames}. (to access the rest of the
     * {@code Curator} annotators, you will need to requrest it manually, by altering
     * {@link edu.illinois.cs.cogcomp.curator.CuratorClient#addRecordViewFromCurator}).
     * <p>
     * Each {@code Curator} internal component is wrapped in a
     * {@link edu.illinois.cs.cogcomp.curator.CuratorAnnotator} that automates the retrieval of
     * {@code requiredViews} and {@code viewName}. The current set of components is built using the
     * dependencies and view names found in
     * {@code /shared/trollope/curator/dist/configs/annotators-trollope.xml, Mar 12 09:12}.
     *
     * @param rm ResourceManager with properties for the
     *        {@link edu.illinois.cs.cogcomp.curator.CuratorClient} and caching behavior
     */
    protected CuratorAnnotatorService(ResourceManager rm) {
        if (!rm.getBoolean(CuratorConfigurator.DISABLE_CACHE))
            throw new IllegalArgumentException("CuratorAnnotatorService doesn't support caching");

        CuratorClient curatorClient = new CuratorClient(rm);

        taBuilder = new CuratorTextAnnotationBuilder(curatorClient);

        String[] requiredViews;
        List<Annotator> annotators = new ArrayList<>();
        // Build the individual Curator components
        // (based off /shared/trollope/curator/dist/configs/annotators-trollope.xml, Mar 12 09:12)
        requiredViews = new String[] {ViewNames.SENTENCE, ViewNames.TOKENS};
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.POS, requiredViews));
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.NER_CONLL, requiredViews));
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.NER_ONTONOTES, requiredViews));
        annotators
                .add(new CuratorAnnotator(curatorClient, ViewNames.PARSE_STANFORD, requiredViews));
        // TODO Curator doesn't return this at the moment
        // annotators.add(new CuratorAnnotator(curatorClient, ViewNames.PARSE_STANFORD_KBEST,
        // requiredViews));
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.DEPENDENCY_STANFORD,
                requiredViews));
        annotators
                .add(new CuratorAnnotator(curatorClient, ViewNames.PARSE_CHARNIAK, requiredViews));
        // TODO Curator doesn't return this at the moment
        // annotators.add(new CuratorAnnotator(curatorClient, ViewNames.PARSE_CHARNIAK_KBEST,
        // requiredViews));

        requiredViews = new String[] {ViewNames.SENTENCE, ViewNames.TOKENS, ViewNames.POS};
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.QUANTITIES, requiredViews));
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.LEMMA, requiredViews));
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.SHALLOW_PARSE, requiredViews));
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.DEPENDENCY, requiredViews));
        annotators
                .add(new CuratorAnnotator(curatorClient, ViewNames.PARSE_BERKELEY, requiredViews));

        requiredViews =
                new String[] {ViewNames.SENTENCE, ViewNames.TOKENS, ViewNames.POS,
                        ViewNames.NER_CONLL};
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.COREF, requiredViews));

        requiredViews =
                new String[] {ViewNames.SENTENCE, ViewNames.TOKENS, ViewNames.POS,
                        ViewNames.SHALLOW_PARSE, ViewNames.PARSE_CHARNIAK, ViewNames.NER_CONLL};
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.SRL_VERB, requiredViews));
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.SRL_NOM, requiredViews));

        requiredViews =
                new String[] {ViewNames.SENTENCE, ViewNames.TOKENS, ViewNames.POS,
                        ViewNames.SHALLOW_PARSE, ViewNames.DEPENDENCY};
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.SRL_PREP, requiredViews));

        requiredViews =
                new String[] {ViewNames.SENTENCE, ViewNames.TOKENS, ViewNames.POS,
                        ViewNames.SHALLOW_PARSE, ViewNames.NER_CONLL};
        annotators.add(new CuratorAnnotator(curatorClient, ViewNames.WIKIFIER, requiredViews));

        viewProviders = new HashMap<>(annotators.size());
        for (Annotator annotator : annotators)
            viewProviders.put(annotator.getViewName(), annotator);
    }

    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text)
            throws AnnotatorException {
        return taBuilder.createTextAnnotation(corpusId, docId, text);
    }

    @Override
    public TextAnnotation createBasicTextAnnotation(String corpusId, String docId, String text,
            Tokenizer.Tokenization tokenization) throws AnnotatorException {
        return taBuilder.createTextAnnotation(corpusId, docId, text, tokenization);
    }

    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId, String text)
            throws AnnotatorException {
        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text);
        for (String viewName : viewProviders.keySet())
            addView(ta, viewName);
        return ta;
    }

    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId,
            String text, Tokenizer.Tokenization tokenization) throws AnnotatorException {
        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text, tokenization);
        for (String viewName : viewProviders.keySet())
            addView(ta, viewName);
        return ta;
    }

    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId,
            String text, Set<String> viewNames) throws AnnotatorException {
        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text);
        for (String viewName : viewNames)
            addView(ta, viewName);
        return ta;
    }

    @Override
    public TextAnnotation createAnnotatedTextAnnotation(String corpusId, String textId,
            String text, Tokenizer.Tokenization tokenization, Set<String> viewNames)
            throws AnnotatorException {
        TextAnnotation ta = createBasicTextAnnotation(corpusId, textId, text, tokenization);
        for (String viewName : viewNames)
            addView(ta, viewName);
        return ta;
    }

    @Override
    public boolean addView(TextAnnotation ta, String viewName) throws AnnotatorException {
        if (ViewNames.SENTENCE.equals(viewName) || ViewNames.TOKENS.equals(viewName))
            return false;

        if (!viewProviders.containsKey(viewName))
            throw new AnnotatorException("View " + viewName + " is not supported.");

        boolean isUpdated = false;

        // TODO Until a caching mechanism is available in illinois-core-utilities, this
        // AnnotatorService will not support caching
        if (ta.hasView(viewName))
            return false;

        Annotator annotator = viewProviders.get(viewName);

        for (String prereqView : annotator.getRequiredViews())
            isUpdated = addView(ta, prereqView);

        ta.addView(annotator);

        // TODO Until a caching mechanism is available in illinois-core-utilities, this
        // AnnotatorService will not support caching
        return isUpdated;
    }
}
