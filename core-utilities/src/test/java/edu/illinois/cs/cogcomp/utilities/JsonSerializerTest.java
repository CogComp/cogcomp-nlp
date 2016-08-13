/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.JsonSerializer;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Simple sanity tests for JsonSerializer.
 * @author mssammon
 */
public class JsonSerializerTest {

    TextAnnotation ta =
            DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(
                    new String[]{ ViewNames.POS, ViewNames.NER_CONLL, ViewNames.SRL_VERB },
                    false, 3); // no noise

    @Test
    public void testSerializerWithCharOffsets()
    {
        String taJson = SerializationHelper.serializeToJson(ta, true );
        System.err.println( taJson );

        JsonObject jobj = (JsonObject) new JsonParser().parse(taJson);

        assertNotNull( jobj );

        JsonArray jsonTokenOffsets = jobj.get( JsonSerializer.TOKENOFFSETS ).getAsJsonArray();

        assertNotNull( jsonTokenOffsets );
        assertEquals( ta.getTokens().length, jsonTokenOffsets.size() );

        Map<IntPair, String > offsetForms = new HashMap<>();

        for ( int i = 0; i < jsonTokenOffsets.size(); ++i )
        {
            JsonObject offset = (JsonObject) jsonTokenOffsets.get(i);
            int start = offset.get( JsonSerializer.STARTCHAROFFSET ).getAsInt();
            int end = offset.get( JsonSerializer.ENDCHAROFFSET ).getAsInt();
            String form = offset.get( JsonSerializer.FORM ).getAsString();
            offsetForms.put( new IntPair(start, end), form );
        }

        Constituent seventhToken = ta.getView( ViewNames.TOKENS ).getConstituents().get( 6 );
        IntPair tokCharOffsets = new IntPair(seventhToken.getStartCharOffset(), seventhToken.getEndCharOffset());
        String seventhTokenForm = seventhToken.getSurfaceForm();

        String deserializedForm = offsetForms.get( tokCharOffsets );

        assertNotNull( deserializedForm );
        assertEquals( seventhTokenForm, deserializedForm );


    }

    @Test
    public void testJsonSerializabilityWithOffsets() throws Exception {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);

        // making sure serialization does not fail, when some views (possibly by mistake) are null
        ta.addView("nullView", null);

        // create (redundant) token offset info in output for non-CCG readers
        String json = SerializationHelper.serializeToJson(ta, true);



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

        Constituent seventhToken = ta.getView( ViewNames.TOKENS ).getConstituents().get( 6 );
        IntPair tokCharOffsets = new IntPair(seventhToken.getStartCharOffset(), seventhToken.getEndCharOffset());
        String seventhTokenForm = seventhToken.getSurfaceForm();

        Constituent seventhTokenCopy = ta2.getView( ViewNames.TOKENS ).getConstituents().get( 6 );
        IntPair tokCharOffsets2 = new IntPair(seventhTokenCopy.getStartCharOffset(), seventhTokenCopy.getEndCharOffset());
        String seventhTokenForm2 = seventhTokenCopy.getSurfaceForm();
        assertEquals( seventhTokenForm, seventhTokenForm2 );
        assertEquals( tokCharOffsets, tokCharOffsets2 );

    }



}
