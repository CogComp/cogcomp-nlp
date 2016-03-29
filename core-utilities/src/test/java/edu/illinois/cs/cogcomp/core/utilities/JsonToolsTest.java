package edu.illinois.cs.cogcomp.core.utilities;

import com.google.gson.JsonArray;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by ryan on 3/28/16.
 */
public class JsonToolsTest {

    @Test
    public void testStringArray(){
        String[] array = new String[] {"a", "b", "c"};
        JsonArray jsonArray = JsonTools.createJsonArrayFromArray(array);

        assertEquals(3, jsonArray.size());
        assertEquals("a", jsonArray.get(0).getAsString());
    }

    @Test
    public void testTextAnnotationArray(){
        TextAnnotation[] array = new TextAnnotation[]{DummyTextAnnotationGenerator.generateBasicTextAnnotation(2), DummyTextAnnotationGenerator.generateBasicTextAnnotation(1)};
        JsonArray jsonArray = JsonTools.createJsonArrayFromArray(array);

        assertEquals(2, jsonArray.size());
        assertTrue(jsonArray.get(0).getAsJsonObject().has("views"));
    }
}
