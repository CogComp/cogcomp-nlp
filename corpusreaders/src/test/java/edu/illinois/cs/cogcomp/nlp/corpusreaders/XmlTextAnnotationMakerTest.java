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
        attributeNames.add("author");
        attributeNames.add("id");
        attributeNames.add("datetime");
        tagsWithAtts.put("post", attributeNames);
        attributeNames = new HashSet<>();
        attributeNames.add("id");
        tagsWithAtts.put("doc", attributeNames);
        Set<String> tagsWithText = new HashSet<>();
        tagsWithText.add("headline");
        tagsWithText.add("post");
        Set<String> tagsToIgnore = new HashSet<>();

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
    }
}
