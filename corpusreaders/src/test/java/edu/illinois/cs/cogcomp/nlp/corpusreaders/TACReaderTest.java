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
 * Created by cddunca2 on 2/12/18.
 */
public class TACReaderTest {

    /*
    e.g.
    <DOC    id="ENG_NW_001278_20130318_F00012HTB">
    <DATE_TIME>2013-03-18T22:01:44</DATE_TIME>
    <HEADLINE>
    U.S. stocks open sharply lower on Cyprus worries
    </HEADLINE>
    <AUTHOR>jianghanlu</AUTHOR>
    <TEXT>
    U.S. stocks open sharply lower on Cyprus worries

    NEW YORK, March 18 (Xinhua) -- U.S. stocks opened sharply lower on Monday as investors worried about a re-emerging eurozone crisis following reports that Cyprus planned to vote on a one-off levy on bank deposits as part of a financial bailout.

    Cyprus postponed for 24 hours on Sunday an emergency session of parliament to debate legislation imposing a one-off levy on bank deposits, rejecting a request by the European Central Bank to proceed with the vote on the legislation without any delay.

    Financial stocks were hit hard after the opening bell on Monday. Shares of Bank of America slumped 1.63 percent to trade at 12.37 dollars a share. Citigroup shares fell 2.56 percent to 46.05 dollars a share, while Barclays shares sank 3.54 percent to 18.55 dollars a share.

    In addition, the Federal Open Market Committee will hold a two-day policy meeting on Tuesday, during which Federal Reserve Chairman Ben Bernanke is expected to keep the on-going asset-purchases plan at a pace of 85-billion-U.S. dollars a month and interest rates at a near-zero level.

    Meanwhile, the U.S. National Association of Home Builders is due to release the Housing Market Index for March later in the morning session. The index, which measures confidence among homebuilders, declined one point in the previous month to 46.

    The Dow Jones Industrial Average dropped 109.61, or 0.76 percent, to 14,404.50, The S&amp;P 500 Index sank 15.54, or 1.00 percent, to 1,545.16. The Nasdaq Composite Index fell 36.16, or 1.11 percent, to 3,212.91.  Enditem
    </TEXT>
    </DOC>
    */

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
