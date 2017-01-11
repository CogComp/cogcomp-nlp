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

import java.util.List;

import static edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ConvertEREToCoNLLFormat.readEntity;

/**
 * Reads ERE data and instantiates TextAnnotations with the corresponding NER view.
 * Also provides functionality to support combination with readers of other ERE annotations from the same source.
 *
 * This code is extracted from Tom Redman's code for generating CoNLL-format ERE NER data.
 * @author mssammon
 */
public class ERENerReader extends EREDocumentReader {

    private static final Logger logger = LoggerFactory.getLogger(ERENerReader.class);

    private int gold_counter;
    private int starts [];
    private int ends [];

    /**
     * @param corpusName      the name of the corpus, this can be anything.
     * @param sourceDirectory the name of the directory containing the file.
     * @throws Exception
     */
    public ERENerReader(String corpusName, String sourceDirectory) throws Exception {
        super(corpusName, sourceDirectory);
        gold_counter = 0;
    }


    @Override
    public TextAnnotation next() {
        TextAnnotation ta = super.next();
        SpanLabelView tokens = (SpanLabelView)ta.getView(ViewNames.TOKENS);
        compileOffsets(tokens);
        SpanLabelView nerView = new SpanLabelView("NER-ERE", "Tom", ta, 1.0, false);

        // now pull all mentions we deal with
        String filetoken = ta.getCorpusId();
        filetoken = filetoken.substring (0, filetoken.length() - 4);
        while (gold_counter < goldfiles.length && goldfiles[gold_counter].startsWith(filetoken)) {
            System.out.println(ta.getCorpusId()+"="+goldfiles[gold_counter]);
            Document doc = SimpleXMLParser.getDocument(golds+goldfiles[gold_counter]);
            Element element = doc.getDocumentElement();
            Element entityElement = SimpleXMLParser.getElement(element, "entities");
            NodeList entityNL = entityElement.getElementsByTagName("entity");
            for (int i = 0; i < entityNL.getLength(); ++i) {

                // extract the entity mentions for each entity
                readEntity(entityNL.item(i), nerView);
            }
            gold_counter++;
        }

    }


    /**
     * get the start and end offsets of all constituents
     * @param tokens
     * @return
     */
    private static void compileOffsets(SpanLabelView tokens) {
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
    static private int findStartIndex(int startOffset) {
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
    static private int findStartIndexIgnoreError(int startOffset) {
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
    static private int findEndIndex(int endOffset) {
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
    static private int findEndIndexIgnoreError(int endOffset) {
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
    public static void readEntity (Node node, SpanLabelView view) throws XMLException {
        NamedNodeMap nnMap = node.getAttributes();
        String label = nnMap.getNamedItem("type").getNodeValue();

        // now for specifics get the mentions.
        NodeList nl = ((Element)node).getElementsByTagName("entity_mention");
        for (int i = 0; i < nl.getLength(); ++i) {
            Node mentionNode = nl.item(i);
            nnMap = mentionNode.getAttributes();
            String noun_type = nnMap.getNamedItem("noun_type").getNodeValue();
            if (noun_type.equals("PRO"))
                continue;

            // we have a valid mention(a "NAM" or a "NOM"), add it to out view.
            int offset = Integer.parseInt(nnMap.getNamedItem("offset").getNodeValue());
            int length = Integer.parseInt(nnMap.getNamedItem("length").getNodeValue());

            NodeList mnl = ((Element)mentionNode).getElementsByTagName("mention_text");
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
                si = ConvertEREToCoNLLFormat.findStartIndexIgnoreError(offset);
                ei = ConvertEREToCoNLLFormat.findEndIndexIgnoreError(offset+length);
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
