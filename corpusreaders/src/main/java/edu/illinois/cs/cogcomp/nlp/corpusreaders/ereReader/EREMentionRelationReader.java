package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.SimpleXMLParser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Reads ERE data and instantiates TextAnnotations with the corresponding Mention and Relation views, as well as
 *     a Named Entity view.
 *
 * ERE annotations are provided in stand-off form: each source file (in xml, and from which character offsets
 *     are computed) has one or more corresponding annotation files (also in xml). Each annotation file corresponds
 *     to a span of the source file, and contains all information about entities, relations, and events for that
 *     span.  Entity and event identifiers presumably carry across spans from the same document.
 *
 * @author mssammon
 */
public class EREMentionRelationReader extends EREDocumentReader {

    private static final String NAME = EREDocumentReader.class.getCanonicalName();
    private static final Logger logger = LoggerFactory.getLogger(EREMentionRelationReader.class);
    private static final String ENTITIES = "entities";
    private static final String ENTITY = "entity";
    private static final String OFFSET = "offset";
    private static final String TYPE = "type";
    private static final String ENTITY_MENTION = "entity_mention";
    private static final String NOUN_TYPE = "noun_type";
    private static final String PRO = "PRO";
    private static final String LENGTH = "length";
    private static final String MENTION_TEXT = "mention_text";

    private int gold_counter;
    private int starts [];
    private int ends [];

    /**
     * @param corpusName      the name of the corpus, this can be anything.
     * @param sourceDirectory the name of the directory containing the file.
     * @throws Exception
     */
    public EREMentionRelationReader(String corpusName, String sourceDirectory) throws Exception {
        super(corpusName, sourceDirectory);
        gold_counter = 0;
    }

    @Override
    public List<TextAnnotation> getTextAnnotationsFromFile(List<Path> corpusFileListEntry) throws Exception {

        TextAnnotation sourceTa = super.getTextAnnotationsFromFile(corpusFileListEntry).get(0);
        SpanLabelView tokens = (SpanLabelView)sourceTa.getView(ViewNames.TOKENS);
        compileOffsets(tokens);
        SpanLabelView nerView = new SpanLabelView(ViewNames.NER_ERE, NAME, sourceTa, 1.0, false);

        // now pull all mentions we deal with
        for (int i = 1; i < corpusFileListEntry.size(); ++i) {

            Document doc = SimpleXMLParser.getDocument(corpusFileListEntry.get(i).toFile());
            Element element = doc.getDocumentElement();
            Element entityElement = SimpleXMLParser.getElement(element, ENTITIES);
            NodeList entityNL = entityElement.getElementsByTagName(ENTITY);
            for (int j = 0; i < entityNL.getLength(); ++i) {

                // extract the entity mentions for each entity
                readEntity(entityNL.item(i), nerView);
            }
            gold_counter++;
        }
        sourceTa.addView(ViewNames.NER_ERE, nerView);

        return Collections.singletonList(sourceTa);
    }


    /**
     * get the start and end offsets of all constituents
     * @param tokens
     * @return
     */
    private void compileOffsets(SpanLabelView tokens) {
        List<Constituent> constituents = tokens.getConstituents();
        int n = constituents.size();
        starts = new int[n];
        ends = new int[n];
        int i = 0;
        for (Constituent cons : tokens.getConstituents()) {
            starts[i] = cons.getStartCharOffset();
            ends[i] = cons.getEndCharOffset();
            i++;
        }
    }

    /**
     * Find the index of the first constituent at startOffset.
     * @param startOffset the character offset we want.
     * @return the index of the first constituent.
     */
    private int findStartIndex(int startOffset) {
        for (int i = 0 ; i < starts.length; i++) {
            if (startOffset == starts[i])
                return i;
            if (startOffset < starts[i]) {
                throw new RuntimeException("Index "+startOffset+" was not exact.");
            }
        }
        throw new RuntimeException("Index "+startOffset+" was out of range.");
    }

    /**
     * Find the index of the first constituent *near* startOffset.
     * @param startOffset the character offset we want.
     * @return the index of the first constituent.
     */
    private int findStartIndexIgnoreError(int startOffset) {
        for (int i = 0 ; i < starts.length; i++) {
            if (startOffset <= starts[i])
                return i;
        }
        throw new RuntimeException("Index "+startOffset+" was out of range.");
    }

    /**
     * Find the index of the first constituent at startOffset.
     * @param endOffset the character offset we want.
     * @return the index of the first constituent.
     */
    private int findEndIndex(int endOffset) {
        for (int i = 0 ; i < ends.length; i++) {
            if (endOffset == ends[i])
                return i;
            if (endOffset < ends[i]) {
                throw new RuntimeException("End Index "+endOffset+" was not exact.");
            }
        }
        throw new RuntimeException("Index "+endOffset+" was out of range.");
    }

    /**
     * Find the index of the first constituent at startOffset.
     * @param endOffset the character offset we want.
     * @return the index of the first constituent.
     */
    private int findEndIndexIgnoreError(int endOffset) {
        for (int i = 0 ; i < ends.length; i++) {
            if (endOffset <= ends[i])
                if (i > 0 && Math.abs(endOffset-ends[i]) > Math.abs(endOffset - ends[i-1]))
                    return i-1;
                else
                    return i;
        }
        throw new RuntimeException("Index "+endOffset+" was out of range.");
    }

    /**
     * read the entities form the gold standard xml and produce appropriate constituents in the view.
     * NOTE: the constituents will not be ordered when we are done.
     * @param node the entity node, contains the more specific mentions of that entity.
     * @param view the span label view we will add the labels to.
     * @throws XMLException
     */
    public void readEntity (Node node, SpanLabelView view) throws XMLException {
        NamedNodeMap nnMap = node.getAttributes();
        String label = nnMap.getNamedItem(TYPE).getNodeValue();

        // now for specifics get the mentions.
        NodeList nl = ((Element)node).getElementsByTagName(ENTITY_MENTION);
        for (int i = 0; i < nl.getLength(); ++i) {
            Node mentionNode = nl.item(i);
            nnMap = mentionNode.getAttributes();
            String noun_type = nnMap.getNamedItem(NOUN_TYPE).getNodeValue();
            if (noun_type.equals(PRO))
                continue; // TODO: add to different view

            // we have a valid mention(a "NAM" or a "NOM"), add it to out view.
            int offset = Integer.parseInt(nnMap.getNamedItem(OFFSET).getNodeValue());
            int length = Integer.parseInt(nnMap.getNamedItem(LENGTH).getNodeValue());

            NodeList mnl = ((Element)mentionNode).getElementsByTagName(MENTION_TEXT);
            String text = null;
            if (mnl.getLength() > 0) {
                text = SimpleXMLParser.getContentString((Element) mnl.item(0));
            }
            int si=0, ei=0;
            try {
                si = findStartIndex(offset);
                ei = findEndIndex(offset+length);
            } catch (IllegalArgumentException iae) {
                List<Constituent> foo = view.getConstituentsCoveringSpan(si, ei);
                System.err.println("Constituents covered existing span : "+foo.get(0));
                System.exit(1);;
            } catch (RuntimeException re) {
                boolean siwaszero = false;
                if (si == 0) {siwaszero = true;}
                si = findStartIndexIgnoreError(offset);
                ei = findEndIndexIgnoreError(offset+length);
                if (siwaszero)
                    System.err.println("Could not find start token : text='"+text+"' at "+offset+" to "+ (offset + length));
                else
                    System.err.println("Could not find end token : text='"+text+"' at "+offset+" to "+ (offset + length));
                TextAnnotation ta = view.getTextAnnotation();
                int max = ta.getTokens().length;
                int start = si >= 2 ? si - 2 : 0;
                int end = (ei+2) < max ? ei+2 : max;
                for (int jj = start; jj < end; jj++) {
                    System.err.print(" ");
                    if (jj == si)
                        System.err.print(":");
                    System.err.print(ta.getToken(jj));
                    if (jj == ei)
                        System.err.print(":");
                    System.err.print(" ");
                }
                System.err.println();
                System.err.flush();
            }
            try {
                view.addSpanLabel(si, ei+1, label, 1.0);
            } catch (IllegalArgumentException iae) {
                // this just overlaps, nothing we can do about it.
            }
        }
    }

}
