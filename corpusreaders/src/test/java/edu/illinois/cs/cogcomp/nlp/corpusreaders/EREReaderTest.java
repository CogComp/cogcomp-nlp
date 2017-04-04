/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ERENerReader;
import edu.illinois.cs.cogcomp.nlp.utilities.TextAnnotationPrintHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor.SPAN_INFO;
import static edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader.*;
import static org.junit.Assert.*;


/**
 * Tests for ERE reader. NOT a unit test as it requires actual corpus files.
 *
 * @author mssammon
 */
public class EREReaderTest {
    private static final String NAME = EREReaderTest.class.getCanonicalName();
    private static final String RELVALUE = "Alona Kimhi--generalaffiliation--> Israeli(1.0)";
    private static final String DATETIMEVAL = "2015-03-22T00:00:00";
    private static final IntPair DATETIMEOFFSETS = new IntPair(1090, 1109);
    private static final String ORIGAUTHVAL = "tinydancer";
    private static final IntPair ORIGAUTHOFFSETS = new IntPair(2943, 2953);
    private static final String AUTHORVAL = "tinydancer";
    private static final IntPair AUTHOROFFSETS = new IntPair(1947, 1957);
    private static final String MENTION_ID_VAL = "m-568eb23c_1_797";
    private static final String NOUN_TYPE_VAL = "NAM";
    private static final String ENTITY_ID_VAL = "ent-NIL00380";
    private static final String SPECIFICITY_VAL = "specific";
    private static final IntPair QUOTEOFFSETS = new IntPair(1148, 1384);
    private static final String QUOTE_VAL = "\n\"Drink cyanide, bloody Neanderthals. You won,\" award-winning Israeli " +
            "author and actress Alona Kimhi wrote on her Facebook page, before erasing it as her comments became " +
            "the talk of the town. \"Only death will save you from yourselves.\"\n";
    private static boolean doSerialize = true;

//
//            "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/" +
//                    "data/source/ENG_DF_001241_20150407_F0000007T.xml";

    // public void testNerReader() {
    public static void main(String[] args) {


        String corpusDir =
                "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data-sample/";

        XmlTextAnnotation outputXmlTa = runTest(corpusDir);


        corpusDir =
                "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/";

        outputXmlTa = runTest(corpusDir);

        System.out.println("Testing EREMentionRelationReader...");



        StringTransformation xmlSt = outputXmlTa.getXmlSt();
        String origXml = xmlSt.getOrigText();

        Map<IntPair, Map<String, String>> markupInfo = outputXmlTa.getXmlMarkup();

        String dateTimeReported = markupInfo.get(DATETIMEOFFSETS).get(DATETIME);
        assertEquals(DATETIMEVAL, dateTimeReported);
        assertEquals(DATETIMEVAL, origXml.substring(DATETIMEOFFSETS.getFirst(), DATETIMEOFFSETS.getSecond()));

//        private static final String ORIGAUTHVAL = "tinydancer";
//        private static final IntPair ORIGAUTHOFFSETS = new IntPair(2943, 2953);
        String origAuth = markupInfo.get(ORIGAUTHOFFSETS).get(ORIG_AUTHOR);
        assertEquals(ORIGAUTHVAL, origAuth);
        assertEquals(ORIGAUTHVAL, origXml.substring(ORIGAUTHOFFSETS.getFirst(), ORIGAUTHOFFSETS.getSecond()));

        String auth = markupInfo.get(AUTHOROFFSETS).get(AUTHOR);
        assertEquals(AUTHORVAL, auth);
        assertEquals(AUTHORVAL, origXml.substring(AUTHOROFFSETS.getFirst(), AUTHOROFFSETS.getSecond()));

        /*
         * other values recorded at same offsets are not required to be mapped to xml document char offsets.
         * Since this value is not retained in the cleaned text, there is NO CORRESPONDING CONSTITUENT.
         */
        String mid = markupInfo.get(AUTHOROFFSETS).get(ENTITY_MENTION_ID);
        assertEquals(MENTION_ID_VAL, mid);

        String nt = markupInfo.get(AUTHOROFFSETS).get(NOUN_TYPE);
        assertEquals(NOUN_TYPE_VAL, nt);

        String eid = markupInfo.get(AUTHOROFFSETS).get(ENTITY_ID);
        assertEquals(ENTITY_ID_VAL, eid);

        String spec = markupInfo.get(AUTHOROFFSETS).get(SPECIFICITY);
        assertEquals(SPECIFICITY_VAL, spec);

        assertEquals(QUOTE, markupInfo.get(QUOTEOFFSETS).get(SPAN_INFO));
        String quoteStr = origXml.substring(QUOTEOFFSETS.getFirst(), QUOTEOFFSETS.getSecond());
        assertEquals(QUOTE_VAL, quoteStr);

        EREMentionRelationReader emr = null;
        try {
            boolean throwExceptionOnXmlTagMismatch = true;
            emr = new EREMentionRelationReader("ERE", corpusDir, throwExceptionOnXmlTagMismatch);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        assert (emr.hasNext());

        String wantedId = "ENG_DF_000170_20150322_F00000082.xml";
        String posterId = "TheOldSchool";
        do {
            outputXmlTa = emr.next();
        } while (!outputXmlTa.getTextAnnotation().getId().equals(wantedId) && emr.hasNext());

        if ( !outputXmlTa.getTextAnnotation().getId().equals(wantedId))
            fail("ERROR: didn't find corpus entry with id '" + wantedId + "'." );

        TextAnnotation output = outputXmlTa.getTextAnnotation();

        assert (output.hasView(ViewNames.MENTION_ERE));

        View nerRelation = output.getView(ViewNames.MENTION_ERE);
        assert (nerRelation.getConstituents().size() > 0);

        System.out.println("EREMentionRelationReader found " + nerRelation.getRelations().size()
                + " relations: ");
        for (Relation r : nerRelation.getRelations())
            System.out.println(TextAnnotationPrintHelper.printRelation(r));

        String relValue = nerRelation.getRelations().get(0).toString();
        assertEquals(RELVALUE, relValue);

        System.out.println(TextAnnotationPrintHelper.OUTPUT_SEPARATOR);
        System.out.println("ERE Coreference chains:");

        assert (output.hasView(ViewNames.COREF_ERE));

        CoreferenceView cView = (CoreferenceView) output.getView(ViewNames.COREF_ERE);

        assert (cView.getConstituents().size() > 0);

        // check no duplicate mentions are added.
        Set<IntPair> mentionSpans = new HashSet<>();
        for (Constituent c : cView.getConstituents()) {
            IntPair cSpan = c.getSpan();
            assertFalse(mentionSpans.contains(cSpan));
            mentionSpans.add(cSpan);
        }

        System.out.println(TextAnnotationPrintHelper.printCoreferenceView(cView));



        if (doSerialize) {
            String jsonStr = SerializationHelper.serializeToJson(output);
            try {
                LineIO.write("EREsample.json", Collections.singletonList(jsonStr));
            } catch (IOException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }


            TextAnnotation newTa = null;

            try {
                newTa = SerializationHelper.deserializeFromJson(jsonStr);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }

            assertNotNull(newTa);
        }
        System.out.println("Report: " + emr.generateReport());


    }


    private static XmlTextAnnotation runTest(String corpusDir) {

        ERENerReader nerReader = null;
        boolean addNominalMentions = true;
        boolean throwExceptionOnXmlTagMismatch = true;
        try {
            nerReader = new ERENerReader("ERE", corpusDir, addNominalMentions, throwExceptionOnXmlTagMismatch);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: " + NAME
                    + ": couldn't instantiate ERENerReader with corpus dir '" + corpusDir + ": "
                    + e.getMessage());
        }

        XmlTextAnnotation outputXmlTa = nerReader.next();
        TextAnnotation output = outputXmlTa.getTextAnnotation();
        View nerEre = null;
        if (addNominalMentions) {
            assert (output.hasView(ViewNames.MENTION_ERE));
            nerEre = output.getView(ViewNames.MENTION_ERE);
        } else {
            assert (output.hasView(ViewNames.NER_ERE));
            nerEre = output.getView(ViewNames.NER_ERE);
        }

        assert (nerEre.getConstituents().size() > 0);

        StringTransformation xmlSt = outputXmlTa.getXmlSt();
        String origXmlStr = xmlSt.getOrigText();
        System.out.println("ERENerReader found " + nerEre.getConstituents().size()
                + " NER constituents: ");

        for (Constituent c : nerEre.getConstituents()) {
            System.out.println(TextAnnotationPrintHelper.printConstituent(c));
            int start = c.getStartCharOffset();
            int end = c.getEndCharOffset();
            IntPair origOffsets = xmlSt.getOriginalOffsets(start, end);
            String origStr = origXmlStr.substring(origOffsets.getFirst(), origOffsets.getSecond());
            System.out.println("Constituent (clean) text: '" + c.getSurfaceForm() + "'");
            System.out.println("Original text: '" + origStr + "'\n---------\n" );
        }
        System.out.println("Report: " + nerReader.generateReport());

        return outputXmlTa;
    }

}
