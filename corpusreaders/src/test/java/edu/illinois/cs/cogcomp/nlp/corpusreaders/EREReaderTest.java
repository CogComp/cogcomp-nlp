/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ERENerReader;
import edu.illinois.cs.cogcomp.nlp.utilities.TextAnnotationPrintHelper;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * Tests for ERE reader. NOT a unit test as it requires actual corpus files.
 *
 * @author mssammon
 */
public class EREReaderTest {
    private static final String NAME = EREReaderTest.class.getCanonicalName();

    private static boolean doSerialize = true;

    // public void testNerReader() {
    public static void main(String[] args) {
        String corpusDir =
                "/shared/corpora/corporaWeb/deft/eng/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/";

        ERENerReader nerReader = null;
        boolean addNominalMentions = true;
        try {
            nerReader = new ERENerReader("ERE", corpusDir, addNominalMentions);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: " + NAME
                    + ": couldn't instantiate ERENerReader with corpus dir '" + corpusDir + ": "
                    + e.getMessage());
        }

        TextAnnotation output = nerReader.next();
        View nerEre = null;
        if (addNominalMentions) {
            assert (output.hasView(ViewNames.MENTION_ERE));
            nerEre = output.getView(ViewNames.MENTION_ERE);
        } else {
            assert (output.hasView(ViewNames.NER_ERE));
            nerEre = output.getView(ViewNames.NER_ERE);
        }

        assert (nerEre.getConstituents().size() > 0);

        System.out.println("ERENerReader found " + nerEre.getConstituents().size()
                + " NER constituents: ");
        for (Constituent c : nerEre.getConstituents())
            System.out.println(TextAnnotationPrintHelper.printConstituent(c));

        System.out.println("Report: " + nerReader.generateReport());

        System.out.println("Testing EREMentionRelationReader...");

        EREMentionRelationReader emr = null;
        try {
            emr = new EREMentionRelationReader("ERE", corpusDir);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        assert (emr.hasNext());

        output = emr.next();
        assert (output.hasView(ViewNames.MENTION_ERE));

        View nerRelation = output.getView(ViewNames.MENTION_ERE);
        assert (nerRelation.getConstituents().size() > 0);

        System.out.println("EREMentionRelationReader found " + nerRelation.getRelations().size()
                + " relations: ");
        for (Relation r : nerRelation.getRelations())
            System.out.println(TextAnnotationPrintHelper.printRelation(r));

        System.out.println(TextAnnotationPrintHelper.OUTPUT_SEPARATOR);
        System.out.println("ERE Coreference chains:");

        assert (output.hasView(ViewNames.COREF_ERE));

        CoreferenceView cView = (CoreferenceView) output.getView(ViewNames.COREF_ERE);

        assert (cView.getConstituents().size() > 0);

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
}
