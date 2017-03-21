/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test XmlTextAnnotationMaker functionality.
 *
 * @author mssammon
 */

public class XmlTextAnnotationMakerTest {

    /**
     * tags and attributes reader needs to handle
     */
    public static final String QUOTE = "quote";
    public static final String AUTHOR = "author";
    public static final String ID = "id";
    public static final String DATETIME = "datetime";
    public static final String POST = "post";
    public static final String DOC = "doc";
    public static final String ORIG_AUTHOR = "orig_author";
    public static final String HEADLINE = "headline";
    public static final String IMG = "img";
    private static final String XML_FILE =
            "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/" +
                "data/source/ENG_DF_001241_20150407_F0000007T.xml";

    // public void testNerReader() {

    /**
     * non-unit Test, as it needs the ERE corpus.
     * @param args
     */
    public static void main(String[] args) {

        TextAnnotationBuilder textAnnotationBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());

        Map<String, Set<String>> tagsWithAtts = new HashMap<>();
        Set<String> attributeNames = new HashSet<>();
        attributeNames.add(AUTHOR);
        attributeNames.add(ID);
        attributeNames.add(DATETIME);
        tagsWithAtts.put(POST, attributeNames);
        attributeNames = new HashSet<>();
        attributeNames.add(ID);
        tagsWithAtts.put(DOC, attributeNames);
        attributeNames = new HashSet<>();
        attributeNames.add(ORIG_AUTHOR);
        tagsWithAtts.put(QUOTE, attributeNames);

        Set<String> tagsWithText = new HashSet<>();
        tagsWithText.add(HEADLINE);
        tagsWithText.add(POST);

        Set<String> tagsToIgnore = new HashSet<>();
        tagsToIgnore.add(QUOTE);
        tagsToIgnore.add(IMG);

        XmlDocumentProcessor xmlProcessor = new XmlDocumentProcessor(tagsWithText, tagsWithAtts, tagsToIgnore);
        XmlTextAnnotationMaker maker = new XmlTextAnnotationMaker(textAnnotationBuilder, xmlProcessor);

        String xmlStr = null;
        try {
            xmlStr = LineIO.slurp(XML_FILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        XmlTextAnnotation output = maker.createTextAnnotation(xmlStr, "test", "test");

        TextAnnotation ta = output.getTextAnnotation();
        Sentence firstSentence = ta.getSentence(0);
        String firstSentenceText = firstSentence.getText();
        System.out.println(firstSentenceText);

        Constituent thirdWord = ta.getView(ViewNames.TOKENS).getConstituentsCoveringSpan(2,3).get(0);

        int thirdStartChar = thirdWord.getStartCharOffset();
        int thirdEndChar = thirdWord.getEndCharOffset();
        String thirdWordForm = thirdWord.getSurfaceForm();

        StringTransformation st = output.getXmlSt();
        IntPair origSpan = st.getOriginalOffsets(thirdStartChar, thirdEndChar);
//        int origStartChar = st.computeOriginalOffset(thirdStartChar);
//        int origEndChar = st.computeOriginalOffset(thirdEndChar);
//        String origWordForm = xmlStr.substring(origStartChar, origEndChar);
        String origWordForm = st.getOrigText().substring(origSpan.getFirst(), origSpan.getSecond());

        System.out.println("Third word: " + thirdWordForm);
        String transformStr = st.getTransformedText().substring(thirdStartChar, thirdEndChar);
        System.out.println("corresponding substring from transformed text: " + transformStr);
        System.out.println("original text substring using mapped offsets: " + origWordForm);

        if (!transformStr.equals(origWordForm))
            System.err.println("ERROR: test failed: word '" + transformStr + "' not identical to original word '" +
                origWordForm + "'. ");

        Map<IntPair, Map<String, String>> atts = output.getXmlMarkup();

        for (IntPair offsets : atts.keySet()) {
            System.out.print(offsets.getFirst() + "-" + offsets.getSecond() + ": ");
            Map<String, String> attVals = atts.get(offsets);
            for (String attType : attVals.keySet())
                System.out.println(attType + ": " + attVals.get(attType));
            System.out.println();
        }

    }

}
