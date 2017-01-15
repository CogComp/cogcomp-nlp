package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ERENerReader;

/**
 * Tests for ERE reader. NOT a unit test as it requires actual corpus files.
 *
 * @author mssammon
 */
public class EREReaderTest {
    private static final String NAME = EREReaderTest.class.getCanonicalName();



//    public void testNerReader() {
    public static void main(String[] args) {
        String corpusDir = "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/";

        ERENerReader nerReader = null;
        try {
            boolean addNominalMentions = false;
            nerReader = new ERENerReader("ERE", corpusDir, addNominalMentions);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: " + NAME + ": couldn't instantiate ERENerReader with corpus dir '" + corpusDir +
            ": " + e.getMessage() );
        }

        TextAnnotation output = nerReader.next();

        assert(output.hasView(ViewNames.NER_ERE));
        View nerEre = output.getView(ViewNames.NER_ERE);
        assert(nerEre.getConstituents().size() > 0);

        System.err.println("ERENerReader found " + nerEre.getConstituents().size() + " NER constituents: ");
        for (Constituent c : nerEre.getConstituents())
            System.err.println(c.toString());
    }
}
