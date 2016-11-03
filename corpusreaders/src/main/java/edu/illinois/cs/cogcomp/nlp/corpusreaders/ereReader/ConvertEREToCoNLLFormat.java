/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.SimpleXMLParser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;

/**
 * Read the ERE data and produce, in CoNLL format, gold standard 
 * data including named and nominal named entities, but excluding pronouns.
 * @author redman
 */
public class ConvertEREToCoNLLFormat {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Three arguments are required in the following order:\n"
                + "<argument 1> should be the source directory containing the ERE unstructured source text.\n"
                + "<argument 2> should contain the ERE labeled data.\n"
                + "<argument 3> is the directory were results will be placed.");
            System.exit(1);
        }
        final String source = args[0];  //  "/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/source/";
        final String golds = args[1];   // "/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/ere/";
        final String conll = args[2];   // "/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/inCoNLL/";
        String[] goldfiles = new File(golds).list();
        EREDocumentReader dr = new EREDocumentReader("ERE Corpora",  source);
        int gold_counter = 0;
        while(dr.hasNext()) {
            TextAnnotation ta = dr.next();            
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
            CoNLL2002Writer.writeViewInCoNLL2003Format(nerView, ta, conll+ta.getCorpusId()+".txt");
        }
    }
    static private int starts [];
    static private int ends [];
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
