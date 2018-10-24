/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREEventReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ERENerReader;
import edu.illinois.cs.cogcomp.nlp.utilities.PrintUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.TextAnnotationPrintHelper;

import java.io.IOException;
import java.util.*;

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
    private static final String MENTION_ID_VAL = "m-568eb23c_1_108";
    private static final String NOUN_TYPE_VAL = "NAM";
    private static final String ENTITY_ID_VAL = "ent-NIL00380";
    private static final String SPECIFICITY_VAL = "specific";
    private static final IntPair POSTOFFSETS = new IntPair(2155, 2500);
    private static final IntPair QUOTEOFFSETS = new IntPair(1148, 1384);

    private static final String QUOTE_VAL = "\n\"Drink cyanide, bloody Neanderthals. You won,\" award-winning Israeli " +
            "author and actress Alona Kimhi wrote on her Facebook page, before erasing it as her comments became " +
            "the talk of the town. \"Only death will save you from yourselves.\"\n";


    private static boolean doSerialize = true;

//
//            "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/" +
//                    "data/source/ENG_DF_001241_20150407_F0000007T.xml";

    // public void testNerReader() {

    /**
     * there are THREE ERE English releases.
     * Regrettably, they do not follow consistent standards for organization or for annotation.
     *
     * LDC2015E29_DEFT_Rich_ERE English V2 has two sets of annotation files: one, used for the Event Argument Extraction
     *    task in TAC that year, includes a small amount of additional markup to make each xml document well-formed.
     *    This changes the annotation offsets. Taggable entities within quoted blocks are annotated.
     *
     * LDC2015E68_DEFT_Rich_ERE_English R2_V2 has as source files excerpts from multi-post discussion forum documents.
     * Taggable entities within quoted blocks are annotated.
     *
     * LDC2016E31_DEFT_Rich_ERE_English ENR3 has -- I believe -- complete threads, where annotation files may be
     *    broken into several chunks. Taggable entities within quoted blocks are NOT marked.
     *
     * There are two Spanish and two Chinese ERE releases (aside from a parallel English-Chinese release).
     * Spanish/Chinese release 1 have the same characteristics as English release 2.
     * Spanish/Chinese release 2 have the same characteristics as English release 3.
     * @param args
     */
    public static void main(String[] args) {

        /*
         * ERE documents in release 2015E29: mainly newswire, some discussion format.
         * This test uses the Event Argument Extraction version of the data, as this includes xml markup that makes
         * the source files well-formed, and we are likely to need this reader for TAC EAE tasks. Moreover, the later
         * ERE release uses this format.
         */
        String corpusDir = "/shared/corpora/corporaWeb/deft/eng/LDC2015E29_DEFT_Rich_ERE_English_Training_Annotation_V2/data/";

        XmlTextAnnotation outputXmlTa = runTest(EreCorpus.ENR1, corpusDir);



        corpusDir = "/shared/corpora/corporaWeb/deft/eng/LDC2015E68_DEFT_Rich_ERE_English_Training_Annotation_R2_V2/data/";

        outputXmlTa = runTest(EreCorpus.ENR2, corpusDir);


        corpusDir =
                "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/";

        outputXmlTa = runTest(EreCorpus.ENR3, corpusDir);

        System.out.println("Testing EREMentionRelationReader...");



        StringTransformation xmlSt = outputXmlTa.getXmlSt();
        String origXml = xmlSt.getOrigText();

        List<XmlDocumentProcessor.SpanInfo> markup = outputXmlTa.getXmlMarkup();
        Map<IntPair, XmlDocumentProcessor.SpanInfo> markupInfo = XmlDocumentProcessor.compileOffsetSpanMapping(markup);
        Map<IntPair, Set<String>> markupAttributes = XmlDocumentProcessor.compileAttributeValues(markup);

        Set<String> dateTimeReported = markupAttributes.get(DATETIMEOFFSETS);
        assert(dateTimeReported.contains(DATETIMEVAL));
        assertEquals(DATETIMEVAL, origXml.substring(DATETIMEOFFSETS.getFirst(), DATETIMEOFFSETS.getSecond()));

//        private static final String ORIGAUTHVAL = "tinydancer";
//        private static final IntPair ORIGAUTHOFFSETS = new IntPair(2943, 2953);
        Set<String> origAuth = markupAttributes.get(ORIGAUTHOFFSETS);
        assert(origAuth.contains(ORIGAUTHVAL));
        assertEquals(ORIGAUTHVAL, origXml.substring(ORIGAUTHOFFSETS.getFirst(), ORIGAUTHOFFSETS.getSecond()));

        Set<String> auth = markupAttributes.get(AUTHOROFFSETS);
        assert(auth.contains(AUTHORVAL));
        assertEquals(AUTHORVAL, origXml.substring(AUTHOROFFSETS.getFirst(), AUTHOROFFSETS.getSecond()));

        /*
         * other values recorded at same offsets are not required to be mapped to xml document char offsets.
         * Since this value is not retained in the cleaned text, there is NO CORRESPONDING CONSTITUENT.
         */
        XmlDocumentProcessor.SpanInfo postSpan = markupInfo.get(POSTOFFSETS);
        String mid = postSpan.attributes.get(ENTITY_MENTION_ID).getFirst();
        assertEquals(MENTION_ID_VAL, mid);

        String nt = markupInfo.get(POSTOFFSETS).attributes.get(NOUN_TYPE).getFirst();
        assertEquals(NOUN_TYPE_VAL, nt);

        String eid = markupInfo.get(POSTOFFSETS).attributes.get(ENTITY_ID).getFirst();
        assertEquals(ENTITY_ID_VAL, eid);

        String spec = markupInfo.get(POSTOFFSETS).attributes.get(SPECIFICITY).getFirst();
        assertEquals(SPECIFICITY_VAL, spec);

        assertEquals(QUOTE, markupInfo.get(QUOTEOFFSETS).label);
        String quoteStr = origXml.substring(QUOTEOFFSETS.getFirst(), QUOTEOFFSETS.getSecond());
        assertEquals(QUOTE_VAL, quoteStr);


        String wantedId = "ENG_DF_000170_20150322_F00000082.xml";
        runRelationReader(corpusDir, wantedId);

        wantedId = "ENG_DF_000170_20150322_F00000082.xml";

        runEventReader(corpusDir, wantedId);

        corpusDir = "/shared/corpora/corporaWeb/deft/event/LDC2016E73_TAC_KBP_2016_Eval_Core_Set_Rich_ERE_Annotation_with_Augmented_Event_Argument_v2/data/eng/nw";
        String newWantedId = "ENG_NW_001278_20131206_F00011WGK.xml";

        XmlTextAnnotation xmlTa = runEventReader(corpusDir, newWantedId);

        List<String> output = Collections.singletonList(SerializationHelper.serializeToJson(xmlTa.getTextAnnotation(), true));
        try {
            LineIO.write("ereOut.json", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static XmlTextAnnotation runRelationReader(String corpusDir, String wantedId) {
        EREMentionRelationReader emr = null;
        try {
            boolean throwExceptionOnXmlTagMismatch = true;
            emr = new EREMentionRelationReader(EreCorpus.ENR3, corpusDir, throwExceptionOnXmlTagMismatch);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        assert (emr.hasNext());

        String posterId = "TheOldSchool";
        XmlTextAnnotation outputXmlTa = null;
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

        return outputXmlTa;
    }


    private static XmlTextAnnotation runEventReader(String corpusDir, String wantedId) {
        EREEventReader emr = null;
        try {
            boolean throwExceptionOnXmlTagMismatch = true;
            emr = new EREEventReader(EreCorpus.ENR3, corpusDir, throwExceptionOnXmlTagMismatch);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        assert (emr.hasNext());

        XmlTextAnnotation outputXmlTa = null;

        do {
            outputXmlTa = emr.next();
        } while (!outputXmlTa.getTextAnnotation().getId().equals(wantedId) && emr.hasNext());

        if (!outputXmlTa.getTextAnnotation().getId().equals(wantedId))
            fail("ERROR: didn't find corpus entry with id '" + wantedId + "'." );

        TextAnnotation output = outputXmlTa.getTextAnnotation();

        assert (output.hasView(ViewNames.MENTION_ERE));

        View nerRelation = output.getView(ViewNames.MENTION_ERE);
        assert (nerRelation.getConstituents().size() > 0);

        assert (output.hasView(ViewNames.EVENT_ERE));
        PredicateArgumentView eventView = (PredicateArgumentView) output.getView(emr.getEventViewName());

        assert (eventView.getConstituents().size() > 0);

        List<Constituent> triggers = eventView.getPredicates();
        assert (triggers.size() > 0);
        List<Relation> args = eventView.getArguments(triggers.get(0));
        assert (args.get(0).getAttribute(ORIGIN) != null);
        assert (args.get(0).getAttribute(REALIS) != null);

        System.out.println(eventView.toString());
        String report = emr.generateReport();

        System.out.println("Event Reader report:\n\n" + report);

        return outputXmlTa;
    }


    private static XmlTextAnnotation runTest(EreCorpus ereCorpus, String corpusRoot) {

        ERENerReader nerReader = null;
        boolean addNominalMentions = true;
        boolean throwExceptionOnXmlTagMismatch = true;
        try {
            nerReader = new EREMentionRelationReader(ereCorpus, corpusRoot, throwExceptionOnXmlTagMismatch);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: " + NAME
                    + ": couldn't instantiate ERENerReader for ERE release " + ereCorpus.name()
                    + ": " + e.getMessage());
        }

        XmlTextAnnotation outputXmlTa = nerReader.next();
        TextAnnotation output = outputXmlTa.getTextAnnotation();

        // Test TextAnnotationUtilities.mapTransformedTextAnnotationToSource()

        TextAnnotation mappedTa = TextAnnotationUtilities.mapTransformedTextAnnotationToSource(output, outputXmlTa.getXmlSt());

        assertEquals(mappedTa.getView(ViewNames.TOKENS).getNumberOfConstituents(), output.getView(ViewNames.TOKENS).getNumberOfConstituents());
        assertEquals(mappedTa.getView(ViewNames.SENTENCE).getNumberOfConstituents(), output.getView(ViewNames.SENTENCE).getNumberOfConstituents());

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
