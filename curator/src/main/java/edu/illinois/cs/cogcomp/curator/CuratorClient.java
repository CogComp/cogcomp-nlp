/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.ViewTypes;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.AvoidUsing;
import edu.illinois.cs.cogcomp.core.utilities.Identifier;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.thrift.base.*;
import edu.illinois.cs.cogcomp.thrift.curator.Curator;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <b>UPDATE:</b> While {@link edu.illinois.cs.cogcomp.curator.CuratorClient} will still be able to
 * provide {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}s and
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View}s, the <i>canonical</i>
 * way to access the {@code Curator} is now through the {@link CuratorAnnotatorService} (which
 * creates a {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService} object).
 *
 * A client for using the Curator to get
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}s.
 * <p>
 * The general use case involves the following:
 * <ol>
 * <li>
 * Create a new {@link CuratorClient}
 * 
 * <pre>
 * {
 *     &#064;code
 *     // Assuming we're starting from raw text
 *     ResourceManager rm = new CuratorConfigurator().getDefaultConfig();
 *     CuratorClient curator = new CuratorClient(rm);
 * }
 * </pre>
 * 
 * </li>
 * <li>
 * Create a new {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}
 * 
 * <pre>
 * {
 *     &#064;code
 *     TextAnnotation ta = client.getTextAnnotation(text);
 * }
 * </pre>
 * 
 * </li>
 * <li>
 * Add views to the
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation} using the
 * required view's name (supported views can be found in
 * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames}.
 *
 * <pre>
 *     {@code client.addTextAnnotationView(ta, ViewNames.POS);}
 * </pre>
 * 
 * </li>
 * </ol>
 *
 * @author Christos Christodoulopoulos
 * @author Mark Sammons
 */
@AvoidUsing(reason = "This is no longer the recommended way of calling Curator",
        alternative = "CuratorAnnotatorService")
public class CuratorClient {
    private static Logger logger = LoggerFactory.getLogger(CuratorClient.class);

    private String curatorHost;
    private int curatorPort;

    private boolean respectTokenization, forceUpdate;

    /**
     * Create a new curator client pointing to the specified host and port. The client that this
     * constructor creates will use the names in
     * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} for the various annotators when
     * calling the curator.
     *
     * @param rm The {@link ResourceManager} containing the properties for Curator
     */
    public CuratorClient(ResourceManager rm) {
        this.curatorHost = rm.getString(CuratorConfigurator.CURATOR_HOST);
        this.curatorPort = rm.getInt(CuratorConfigurator.CURATOR_PORT);
        this.respectTokenization = rm.getBoolean(CuratorConfigurator.RESPECT_TOKENIZATION);
        this.forceUpdate = rm.getBoolean(CuratorConfigurator.CURATOR_FORCE_UPDATE);
    }

    /**
     * Creates a new {@link edu.illinois.cs.cogcomp.thrift.curator.Record} for the specified
     * {@code text}. This method calls the Curator to get the tokenization and the sentences unless
     * the CuratorClient's {@link #respectTokenization} field is set to {@code true}, in which case
     * it generates sentence and label views based on newlines and whitespace characters.
     * (<b>NB:</b> tabs will be treated as tokens!) Consecutive whitespace characters will not
     * generate empty tokens, but the token offsets and sentence offsets will count all whitespace
     * characters.
     * <p>
     * <b> Note: </b> The {@code Record} returned by this method will not have any views except the
     * {@code Sentence} and {@code Token} view. To get other views from the Curator, call the
     * appropriate {@link CuratorClient} functions (e.g TODO).
     *
     * @param text The text (tokenized or not)
     * @return A {@link edu.illinois.cs.cogcomp.thrift.curator.Record} with
     *         {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#TOKENS} and
     *         {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#SENTENCE} views.
     */
    private Record getRecord(String text) throws ServiceUnavailableException,
            AnnotationFailedException, TException, SocketException {
        // Instantiate a basic record for a given text with a curator-compatible identifier
        // and initialized empty view collections
        Record record = new Record();
        record.setRawText(text);
        record.setLabelViews(new TreeMap<String, Labeling>());
        record.setParseViews(new TreeMap<String, Forest>());
        record.setClusterViews(new TreeMap<String, Clustering>());
        record.setViews(new TreeMap<String, View>());
        record.setIdentifier(Identifier.getId(text, respectTokenization));

        if (respectTokenization) {
            List<String> inputs = new LinkedList<>();

            String[] sentences = text.split(System.getProperty("line.separator"));

            for (String sentence : sentences)
                if (sentence.length() > 0)
                    inputs.add(sentence);

            Labeling sents = RecordUtils.sentences(inputs);
            record.getLabelViews().put(ViewNames.SENTENCE, sents);
            Labeling tokens = RecordUtils.tokenize(inputs);
            record.getLabelViews().put(ViewNames.TOKENS, tokens);
        } else {
            addRecordView(record, ViewNames.TOKENS);
            addRecordView(record, ViewNames.SENTENCE);
        }
        return record;
    }

    /**
     * Creates a new
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation} for the
     * specified {@code text} belonging to the {@code corpusId} with id {@code textId}. This method
     * calls the Curator to get the tokenization and the sentences unless the CuratorClient's
     * {@link #respectTokenization} field is set to {@code true}, in which case it generates
     * sentence and label views based on newlines and whitespace characters. (<b>NB:</b> tabs will
     * be treated as tokens!) Consecutive whitespace characters will not generate empty tokens, but
     * the token offsets and sentence offsets will count all whitespace characters.
     * <p>
     * <b> Note: </b> The {@code Record} returned by this method will not have any views except the
     * {@code Sentence} and {@code Token} view. To get other views from the Curator, call the
     * appropriate {@link CuratorClient} functions (e.g TODO).
     *
     * @param corpusId Identifier for the corpus
     * @param textId Identifier for the text
     * @param text The raw text
     * @return A {@code TextAnnotation} with
     *         {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#TOKENS} and
     *         {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#SENTENCE} views.
     */
    public TextAnnotation getTextAnnotation(String corpusId, String textId, String text)
            throws ServiceUnavailableException, AnnotationFailedException, TException,
            SocketException {
        Record record = getRecord(text);
        final Labeling tokensLabeling = record.getLabelViews().get(ViewNames.TOKENS);
        final Labeling sentenceLabeling = record.getLabelViews().get(ViewNames.SENTENCE);

        return CuratorDataStructureInterface.getTextAnnotationFromRecord(corpusId, textId, record,
                tokensLabeling, sentenceLabeling);
    }

    /**
     * Adds a view to a {@link edu.illinois.cs.cogcomp.thrift.curator.Record}.
     *
     * @param record The {@link edu.illinois.cs.cogcomp.thrift.curator.Record} to annotate
     * @param viewName The view to add
     */
    private void addRecordView(Record record, String viewName) throws TException,
            AnnotationFailedException, ServiceUnavailableException, SocketException {
        Record newRecord =
                addRecordViewFromCurator(record.getRawText(), RecordUtils.getSentenceList(record),
                        viewName);

        if (ViewNames.getViewType(viewName) == ViewTypes.TOKEN_LABEL_VIEW
                || ViewNames.getViewType(viewName) == ViewTypes.SPAN_LABEL_VIEW) {
            Map<String, Labeling> labelViews = newRecord.getLabelViews();
            record.labelViews.put(viewName, labelViews.get(convertCuratorViewName(viewName)));
        } else if (ViewNames.getViewType(viewName) == ViewTypes.COREF_VIEW) {
            Map<String, Clustering> clusterViews = newRecord.getClusterViews();
            record.clusterViews.put(viewName, clusterViews.get(convertCuratorViewName(viewName)));
        } else if (ViewNames.getViewType(viewName) == ViewTypes.DEPENDENCY_VIEW
                || ViewNames.getViewType(viewName) == ViewTypes.PARSE_VIEW
                || ViewNames.getViewType(viewName) == ViewTypes.PREDICATE_ARGUMENT_VIEW) {
            Map<String, Forest> parseViews = newRecord.getParseViews();
            record.parseViews.put(viewName, parseViews.get(convertCuratorViewName(viewName)));
        }
    }

    public edu.illinois.cs.cogcomp.core.datastructures.textannotation.View getTextAnnotationView(
            TextAnnotation ta, String viewName) throws TException, AnnotationFailedException,
            ServiceUnavailableException, SocketException {
        edu.illinois.cs.cogcomp.core.datastructures.textannotation.View view;
        Record record =
                addRecordViewFromCurator(ta.getText(), TextAnnotationUtilities.getSentenceList(ta),
                        viewName);
        ViewTypes viewType = ViewNames.getViewType(viewName);
        if (viewType == ViewTypes.TOKEN_LABEL_VIEW) {
            Labeling labeling = record.getLabelViews().get(convertCuratorViewName(viewName));
            view =
                    CuratorDataStructureInterface.alignLabelingToTokenLabelView(viewName, ta,
                            labeling);
        } else if (viewType == ViewTypes.SPAN_LABEL_VIEW) {
            boolean allowOverlappingSpans = false;
            if (viewName.equals(ViewNames.WIKIFIER))
                allowOverlappingSpans = true;
            Labeling labeling = record.getLabelViews().get(convertCuratorViewName(viewName));
            view =
                    CuratorDataStructureInterface.alignLabelingToSpanLabelView(viewName, ta,
                            labeling, allowOverlappingSpans);
        } else if (viewType == ViewTypes.DEPENDENCY_VIEW) {
            Forest depForest = record.getParseViews().get(convertCuratorViewName(viewName));

            if (depForest.trees.size() > TextAnnotationUtilities.getSentenceList(ta).size())
                throw new AnnotationFailedException("mismatched number of trees and sentences.");

            view =
                    CuratorDataStructureInterface.alignForestToDependencyView(viewName, ta,
                            depForest);
        } else if (viewType == ViewTypes.PARSE_VIEW) {

            Forest parseForest = record.getParseViews().get(convertCuratorViewName(viewName));

            if (parseForest.trees.size() > TextAnnotationUtilities.getSentenceList(ta).size())
                throw new AnnotationFailedException("mismatched number of trees and sentences.");

            view =
                    CuratorDataStructureInterface.alignForestToParseTreeView(viewName, ta,
                            parseForest);
        } else if (viewType == ViewTypes.PREDICATE_ARGUMENT_VIEW) {
            Forest forest = record.getParseViews().get(convertCuratorViewName(viewName));
            view =
                    CuratorDataStructureInterface.alignForestToPredicateArgumentView(viewName, ta,
                            forest);
        } else if (viewType == ViewTypes.COREF_VIEW) {
            Clustering corefClustering =
                    record.getClusterViews().get(convertCuratorViewName(viewName));
            view =
                    CuratorDataStructureInterface.alignClusteringToCoreferenceView(viewName, ta,
                            corefClustering);
        } else
            throw new AnnotationFailedException("Unrecognised view type " + viewType);
        return view;
    }

    /**
     * Does the network call to the Curator and fetches a record that has a particular view.
     *
     * @param text The raw text (this will be used if {@link #respectTokenization} is false.
     * @param sentences The list of tokenized sentences (will be {@code null} if
     *        {@link #respectTokenization} is true.
     * @param viewName The view to get (according to the Curator lingo.)
     * @return A {@link edu.illinois.cs.cogcomp.thrift.curator.Record} with the requested view
     */
    private Record addRecordViewFromCurator(String text, List<String> sentences, String viewName)
            throws ServiceUnavailableException, AnnotationFailedException, TException,
            SocketException {
        viewName = convertCuratorViewName(viewName);
        TTransport transport = new TSocket(this.curatorHost, this.curatorPort);

        logger.debug("Calling curator on host '" + curatorHost + "', port '" + curatorPort
                + "' for view '" + viewName + "'...");

        try {
            ((TSocket) transport).getSocket().setReuseAddress(true);
        } catch (SocketException e) {
            logger.error("Unable to setReuseAddress!", e);
            throw e;
        }
        transport = new TFramedTransport(transport);
        TProtocol protocol = new TBinaryProtocol(transport);
        transport.open();

        Curator.Client client = new Curator.Client(protocol);

        Record newRecord;
        if (respectTokenization) {
            newRecord = client.wsprovide(viewName, sentences, forceUpdate);
        } else {
            newRecord = client.provide(viewName, text, forceUpdate);
        }

        transport.close();
        return newRecord;
    }

    /**
     * <b>NB:</b>Temporary fix until Curator gets the new ViewNames.
     * <p>
     * Converts a view name from {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} to
     * one compatible with the current instance of Curator.
     * 
     * @param viewName The view name to convert
     * @return The Curator compatible name
     */
    @SuppressWarnings("deprecation")
    private String convertCuratorViewName(String viewName) {
        switch (viewName) {
            case ViewNames.SENTENCE:
                return "sentences";
            case ViewNames.TOKENS:
                return "tokens";
            case ViewNames.LEMMA:
                return "lemma";
            case ViewNames.POS:
                return "pos";
            case ViewNames.NER_CONLL:
            case ViewNames.NER:
                return "ner";
            case ViewNames.NER_ONTONOTES:
                return "ner-ext";
            case ViewNames.SHALLOW_PARSE:
            case ViewNames.CHUNK:
                return "chunk";
            case ViewNames.QUANTITIES:
                return "quantities";
            case ViewNames.WIKIFIER:
                return "wikifier";
            case ViewNames.DEPENDENCY:
                return "dependencies";
            case ViewNames.DEPENDENCY_STANFORD:
                return "stanfordDep";
            case ViewNames.PARSE_CHARNIAK:
                return "charniak";
            case ViewNames.PARSE_CHARNIAK_KBEST:
                return "charniak_k_best";
            case ViewNames.PARSE_STANFORD:
                return "stanfordParse";
            case ViewNames.PARSE_STANFORD_KBEST:
                return "stanfordKbestParse";
            case ViewNames.PARSE_BERKELEY:
                return "berkeley";
            case ViewNames.SRL_VERB:
            case ViewNames.SRL:
                return "srl";
            case ViewNames.SRL_NOM:
            case ViewNames.NOM:
                return "nom";
            case ViewNames.SRL_PREP:
                return "prep";
            case ViewNames.COREF:
                return "coref";
        }
        return null;
    }
}
