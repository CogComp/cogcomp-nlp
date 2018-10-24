/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREEventReader;
import edu.illinois.cs.cogcomp.tokenizer.MultiLingualTokenizer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test ERE reader with Spanish and Chinese text. NOTE that these are NOT unit tests, as they require the source
 *    corpora.
 *
 * @author mssammon
 */
public class MultilingualEreReaderTest {

    private static final String spanishPathA = "/shared/corpora/corporaWeb/deft/spa/LDC2015E107_DEFT_Rich_ERE_Spanish_Annotation_V1/data/";
    private static final String spanishPathB = "/shared/corpora/corporaWeb/deft/spa/LDC2016E34_DEFT_Rich_ERE_Spanish_Annotation_R2/data/";
    private static final String chinesePathB = "/shared/corpora/corporaWeb/deft/chi/LDC2015E112_DEFT_Rich_ERE_Chinese_Training_Annotation_R2/data/";


    public static void main(String[] args) {
        testSpanish();
        testChinese();
    }


    public static void testSpanish() {
//        String lang = "es"; //spanish
//        TextAnnotationBuilder taBldr = MultiLingualTokenizer.getTokenizer(lang);

        EREEventReader reader = null;
        try {
            boolean throwExceptionOnXmlParseFail = true;
            TextAnnotationBuilder spanishTaBldr = MultiLingualTokenizer.getTokenizer(Language.Spanish.getCode());
            reader = new EREEventReader(EREDocumentReader.EreCorpus.ENR2, spanishTaBldr, spanishPathA, throwExceptionOnXmlParseFail);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        testReader(reader);
    }



    public static void testChinese() {

        EREEventReader reader = null;
        try {
            boolean throwExceptionOnXmlParseFail = true;
            TextAnnotationBuilder chineseTaBldr = MultiLingualTokenizer.getTokenizer(Language.Chinese.getCode());
            reader = new EREEventReader(EREDocumentReader.EreCorpus.ENR3,  chineseTaBldr, chinesePathB, throwExceptionOnXmlParseFail);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


        testReader(reader);
    }


    private static void testReader(EREEventReader reader) {
        assertTrue(reader.hasNext());

        XmlTextAnnotation xmlTa = reader.next();

        TextAnnotation ta = xmlTa.getTextAnnotation();
        assertTrue(ta.hasView(ViewNames.MENTION_ERE));
        assertTrue(ta.getView(ViewNames.MENTION_ERE).getConstituents().size() > 5 );
        assertTrue(ta.getView(ViewNames.MENTION_ERE).getRelations().size() > 0 );

        assertTrue(ta.hasView(ViewNames.EVENT_ERE));
        assertTrue(ta.getView(ViewNames.EVENT_ERE).getConstituents().size() > 1 );
        assertTrue(ta.getView(ViewNames.EVENT_ERE).getRelations().size() > 2 );
    }

}
