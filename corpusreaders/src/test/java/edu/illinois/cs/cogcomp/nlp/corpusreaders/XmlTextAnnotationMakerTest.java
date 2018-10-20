/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * Test XmlTextAnnotationMaker functionality. Non-unit test, as it requires access to ERE corpus.
 *
 * @author mssammon
 */

public class XmlTextAnnotationMakerTest {

    /**
     * tags and attributes reader needs to handle
     * copied from EREDocumentReader
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


    private static final String XML_FILE2 =
            "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/" +
                    "data/source/ENG_DF_001237_20150411_F0000008E.xml"; //ENG_DF_001238_20150323_F0000007D.xml"; // ENG_DF_000261_20150319_F00000084.xml";
    // public void testNerReader() {

    /**
     * non-unit Test, as it needs the ERE corpus.
     * @param args
     */
    public static void main(String[] args) {

        boolean throwExceptionOnXmlTagMiss = true;

        XmlTextAnnotationMaker maker = null;
        try {
            maker = EREDocumentReader.buildEreXmlTextAnnotationMaker(EREDocumentReader.EreCorpus.ENR3.name(), throwExceptionOnXmlTagMiss);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        testWithFile(maker, XML_FILE2);

        testWithFile(maker, XML_FILE);
    }

    private static void testWithFile(XmlTextAnnotationMaker maker, String xmlFile) {
        String xmlStr = null;
        try {
            xmlStr = LineIO.slurp(xmlFile);
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

        View mentionView = output.getTextAnnotation().getView(ViewNames.SENTENCE);

        for (Constituent c : mentionView.getConstituents()) {
            int start = c.getStartCharOffset();
            int end = c.getEndCharOffset();
            String cleanForm = c.getSurfaceForm();
            IntPair sourceSpan = st.getOriginalOffsets(start, end);
            System.out.println("------\nclean: " + cleanForm  + ", (" + start + ", " + end + ")");
            System.out.println("------\nsource: " + st.getOrigText().substring(sourceSpan.getFirst(), sourceSpan.getSecond()) +
                ", (" + sourceSpan.getFirst() + ", " + sourceSpan.getSecond() + ")\n");
        }
        List<XmlDocumentProcessor.SpanInfo> markup = output.getXmlMarkup();
        Map<IntPair, XmlDocumentProcessor.SpanInfo> markupMap = XmlDocumentProcessor.compileOffsetSpanMapping(markup);
        for (IntPair offsets : markupMap.keySet()) {
            System.out.print(offsets.getFirst() + "-" + offsets.getSecond() + ": ");
            Map<String, Pair<String, IntPair>> attVals = markupMap.get(offsets).attributes;
            for (String attType : attVals.keySet())
                System.out.println(attType + ": " + attVals.get(attType).getFirst());
            System.out.println();
        }

    }

}
