/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.JsonSerializer;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * Simple sanity tests for JsonSerializer.
 * 
 * @author mssammon
 */
public class JsonSerializerTest {
    private static final String RHYME_VIEW_NAME = "rhyme";
    private static Logger logger = LoggerFactory.getLogger(JsonSerializerTest.class);

    TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[] {
            ViewNames.POS, ViewNames.NER_CONLL, ViewNames.SRL_VERB}, false, 3); // no noise

    /** Behavior specific to unit tests only. Use with caution */
    public static void verifySerializedJSONObject(JsonObject jobj, TextAnnotation ta) {
        assertNotNull(jobj);

        JsonArray jsonTokenOffsets = jobj.get(JsonSerializer.TOKENOFFSETS).getAsJsonArray();

        assertNotNull(jsonTokenOffsets);
        assertEquals(ta.getTokens().length, jsonTokenOffsets.size());

        Map<IntPair, String> offsetForms = new HashMap<>();

        for (int i = 0; i < jsonTokenOffsets.size(); ++i) {
            JsonObject offset = (JsonObject) jsonTokenOffsets.get(i);
            int start = offset.get(JsonSerializer.STARTCHAROFFSET).getAsInt();
            int end = offset.get(JsonSerializer.ENDCHAROFFSET).getAsInt();
            String form = offset.get(JsonSerializer.FORM).getAsString();
            offsetForms.put(new IntPair(start, end), form);
        }

        Constituent seventhToken = ta.getView(ViewNames.TOKENS).getConstituents().get(6);
        IntPair tokCharOffsets =
                new IntPair(seventhToken.getStartCharOffset(), seventhToken.getEndCharOffset());
        String seventhTokenForm = seventhToken.getSurfaceForm();

        String deserializedForm = offsetForms.get(tokCharOffsets);

        assertNotNull(deserializedForm);
        assertEquals(seventhTokenForm, deserializedForm);

        Constituent thirdPos = ta.getView(ViewNames.POS).getConstituents().get(3);

        assertEquals(null, thirdPos.getLabelsToScores());

        View rhymeRecons = ta.getView("rhyme");
        assertNotNull(rhymeRecons);
        Relation r = rhymeRecons.getRelations().get(0);
        Map<String, Double> relLabelScores = r.getLabelsToScores();
        assertNotNull(relLabelScores);
        assertEquals(2, relLabelScores.size());

        Constituent c = r.getSource();
        Map<String, Double> cLabelScores = c.getLabelsToScores();
        assertNotNull(cLabelScores);
        assertEquals(4, cLabelScores.size());
    }

    /** Behavior specific to unit tests only. Use with caution */
    public static void verifyDeserializedJsonString(String json, TextAnnotation ta) throws Exception {
        TextAnnotation ta2 = SerializationHelper.deserializeFromJson(json);
        assertEquals(ta2.getCorpusId(), ta.getCorpusId());
        assertEquals(ta2.getId(), ta.getId());
        assertEquals(ta2.getNumberOfSentences(), ta.getNumberOfSentences());
        assertEquals(ta2.getSentence(1), ta.getSentence(1));
        assertEquals(ta2.getSentenceFromToken(2), ta.getSentenceFromToken(2));
        assertEquals(ta2.getTokenIdFromCharacterOffset(5), ta.getTokenIdFromCharacterOffset(5));
        assertEquals(ta2.getToken(4), ta.getToken(4));
        assertEquals(ta2.getAvailableViews(), ta.getAvailableViews());
        assertEquals(Arrays.toString(ta2.getTokensInSpan(1, 3)),
                Arrays.toString(ta.getTokensInSpan(1, 3)));
        assertEquals(ta2.getText(), ta.getText());

        Constituent seventhToken = ta.getView(ViewNames.TOKENS).getConstituents().get(6);
        IntPair tokCharOffsets =
                new IntPair(seventhToken.getStartCharOffset(), seventhToken.getEndCharOffset());
        String seventhTokenForm = seventhToken.getSurfaceForm();

        Constituent seventhTokenCopy = ta2.getView(ViewNames.TOKENS).getConstituents().get(6);
        IntPair tokCharOffsets2 =
                new IntPair(seventhTokenCopy.getStartCharOffset(),
                        seventhTokenCopy.getEndCharOffset());
        String seventhTokenForm2 = seventhTokenCopy.getSurfaceForm();
        assertEquals(seventhTokenForm, seventhTokenForm2);
        assertEquals(tokCharOffsets, tokCharOffsets2);
    }

    @Test
    public void testSerializerWithCharOffsets() {

        addRhymeViewToTa(ta);

        String taJson = SerializationHelper.serializeToJson(ta, true);
        logger.info(taJson);

        JsonObject jobj = (JsonObject) new JsonParser().parse(taJson);
        JsonSerializerTest.verifySerializedJSONObject(jobj, ta);
    }


    private static void addRhymeViewToTa(TextAnnotation someTa) {
        View rhymeView = new View(RHYME_VIEW_NAME, "test", someTa, 0.4);

        Map< String, Double > newLabelsToScores = new TreeMap< String, Double >();
        String[] labels = { "eeny", "meeny", "miny", "mo" };
        double[] scores = { 0.15, 0.15, 0.3, 0.4 };

        for ( int i = 0; i < labels.length; ++i )
            newLabelsToScores.put(labels[i], scores[i]);

        Constituent first = new Constituent(newLabelsToScores, RHYME_VIEW_NAME, someTa, 2, 4);
        rhymeView.addConstituent(first);

        /**
         * no constraint on scores -- don't have to sum to 1.0
         */
        for ( int i = labels.length -1; i > 0; --i )
            newLabelsToScores.put(labels[i], scores[3-i]);

        Constituent second = new Constituent(newLabelsToScores, RHYME_VIEW_NAME, someTa, 2, 4);
        rhymeView.addConstituent(second);

        Map<String, Double> relLabelsToScores = new TreeMap<>();
        relLabelsToScores.put("Yes", 0.8);
        relLabelsToScores.put("No", 0.2);

        Relation rel = new Relation( relLabelsToScores, first, second );
        rhymeView.addRelation(rel);

        someTa.addView(RHYME_VIEW_NAME, rhymeView);
    }

    @Test
    public void testJsonSerializabilityWithOffsets() throws Exception {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);

        // making sure serialization does not fail, when some views (possibly by mistake) are null
        ta.addView("nullView", null);

        // create (redundant) token offset info in output for non-CCG readers
        String json = SerializationHelper.serializeToJson(ta, true);

        JsonSerializerTest.verifyDeserializedJsonString(json, ta);
    }

    /**
     * make sure that if an already serialized TextAnnotation object is modified and reserialized,
     *    (and written to the same target file), that the file is updated correctly
     */
    @Test
    public void testJsonSerializedTaUpdate() {

        // make sure we aren't using a TA already updated with "rhyme" view
        TextAnnotation localTa = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[] {
                ViewNames.POS, ViewNames.NER_CONLL, ViewNames.SRL_VERB}, false, 3); // no noise

        String serTestDir = "serTestDir";
        if(!IOUtils.exists(serTestDir))
            IOUtils.mkdir(serTestDir);
        else if (IOUtils.isFile(serTestDir))
            throw new IllegalStateException("ERROR: test directory " + serTestDir + " already exists as file.");
        else
            try {
                IOUtils.cleanDir(serTestDir);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("ERROR: test directory " + serTestDir + " could not be cleaned. Permissions?");
            }
        if (!IOUtils.getListOfFilesInDir(serTestDir).isEmpty())
            throw new IllegalStateException("ERROR: test directory " + serTestDir + " already contains files even after cleaning.");

        String fileName = serTestDir + "/arbitrary.json";
        boolean forceOverwrite = true;
        boolean useJson = true;
        try {
            SerializationHelper.serializeTextAnnotationToFile(localTa, fileName, forceOverwrite, useJson);
        } catch (IOException e) {
            e.printStackTrace();
            fail("error trying to serialize json file " + fileName + ".");
        }

        TextAnnotation taDeser = null;
        try {
            taDeser = SerializationHelper.deserializeTextAnnotationFromFile(fileName, useJson);
        } catch (Exception e) {
            e.printStackTrace();
            fail("error trying to deserialize json file " + fileName + ".");
        }
        assertTrue(taDeser.hasView(ViewNames.SRL_VERB));
        assertFalse(taDeser.hasView(RHYME_VIEW_NAME));
        addRhymeViewToTa(taDeser);
        assertTrue(taDeser.hasView(RHYME_VIEW_NAME));

        try {
            SerializationHelper.serializeTextAnnotationToFile(taDeser, fileName, forceOverwrite, useJson);
        } catch (IOException e) {
            e.printStackTrace();
            fail("error trying to serialize json file " + fileName + " for second time.");
        }

        TextAnnotation taDeserDeser = null;
        try {
            taDeserDeser = SerializationHelper.deserializeTextAnnotationFromFile(fileName, useJson);
        } catch (Exception e) {
            e.printStackTrace();
            fail("error trying to deserialize json file " + fileName + " for second time.");
        }

        assertTrue(taDeserDeser.hasView(RHYME_VIEW_NAME));
        assertTrue(taDeserDeser.getView(RHYME_VIEW_NAME).getConstituents().size() > 0);
    }
}
