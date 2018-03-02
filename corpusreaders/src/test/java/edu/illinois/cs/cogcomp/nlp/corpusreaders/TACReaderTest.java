package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Chase Duncan
 *
 * This is a simple test for the nlp.corpusreaders.TACReader.
 * For licensing reasons we cannot include test data in the repository so it must be read from disk. This is why there
 * is a hardcoded path for the corpus root. The structure of the class doesn't provide for unit testing. This is a black
 * box test that checks that the file is parsed as we expect it to be.
 */
public class TACReaderTest {

    private static final String NAME = TACReaderTest.class.getCanonicalName();

    private static final String CORPUS_ROOT =
            "/shared/corpora/corporaWeb/tac/LDC2016E63_TAC_KBP_2016_Evaluation_Source_Corpus_V1.1/data/";
    private static final IntPair IDOFFSETS = new IntPair(12, 44);
    private static final String DATETIMEVAL = "2013-03-18T22:01:44";
    private static final String ID = "ENG_NW_001278_20130318_F00012HTB";
    private static final IntPair DATETIMEOFFSETS = new IntPair(58, 77);
    private static final String AUTHORVAL = "jianghanlu";
    private static final IntPair AUTHOROFFSETS = new IntPair(170, 180);

    public static void main(String[] args){
        TACReader tacReader= null;
        try{
            tacReader = new TACReader(CORPUS_ROOT,true);
        } catch (Exception e){
            e.printStackTrace();
            System.err.println("ERROR: " + NAME
                    + ": couldn't instantiate TACReader: "
                    + e.getMessage());
        }
        String wantedId = "ENG_NW_001278_20130318_F00012HTB.xml";
        XmlTextAnnotation outputXmlTa = null;
        do {
            try {
                outputXmlTa = tacReader.next();
            } catch (IllegalStateException e){
                e.printStackTrace();
            }
        } while(!outputXmlTa.getTextAnnotation().getId().equals(wantedId)&&tacReader.hasNext());

        if (!outputXmlTa.getTextAnnotation().getId().equals(wantedId))
            fail("ERROR: didn't find corpus entry with id '" + wantedId + "'." );

        TextAnnotation output = outputXmlTa.getTextAnnotation();

        StringTransformation xmlSt = outputXmlTa.getXmlSt();
        String origXml = xmlSt.getOrigText();

        List<XmlDocumentProcessor.SpanInfo> markup = outputXmlTa.getXmlMarkup();
        Map<IntPair, XmlDocumentProcessor.SpanInfo> markupInfo = XmlDocumentProcessor.compileOffsetSpanMapping(markup);
        Map<IntPair, Set<String>> markupAttributes = XmlDocumentProcessor.compileAttributeValues(markup);

        Set<String> docIdReported = markupAttributes.get(IDOFFSETS);
        assert(docIdReported.contains(ID));
        assertEquals(DATETIMEVAL, origXml.substring(DATETIMEOFFSETS.getFirst(), DATETIMEOFFSETS.getSecond()));
        assertEquals(AUTHORVAL, origXml.substring(AUTHOROFFSETS.getFirst(), AUTHOROFFSETS.getSecond()));
    }
}
