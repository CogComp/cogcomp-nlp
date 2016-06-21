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
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.JsonSerializer;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import org.json.simple.JSONObject;
import org.junit.Test;

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
                    false ); // no noise

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

}
