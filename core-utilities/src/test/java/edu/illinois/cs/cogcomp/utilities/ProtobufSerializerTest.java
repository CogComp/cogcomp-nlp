/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.protobuf.ProtobufSerializer;
import edu.illinois.cs.cogcomp.core.utilities.protobuf.TextAnnotationImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProtobufSerializerTest {
    private static Logger logger = LoggerFactory.getLogger(ProtobufSerializerTest.class);

    TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[] {
            ViewNames.POS, ViewNames.NER_CONLL, ViewNames.SRL_VERB}, false, 3); // no noise

    @Test
    public void testSerializerWithCharOffsets() throws Exception {
        View rhymeView = new View("rhyme", "test", ta, 0.4 );

        Map< String, Double > newLabelsToScores = new TreeMap< String, Double >();
        String[] labels = { "eeny", "meeny", "miny", "mo" };
        double[] scores = { 0.15, 0.15, 0.3, 0.4 };

        for ( int i = 0; i < labels.length; ++i )
            newLabelsToScores.put(labels[i], scores[i]);

        Constituent first = new Constituent( newLabelsToScores, "rhyme", ta, 2, 4 );
        rhymeView.addConstituent(first);

        /**
         * no constraint on scores -- don't have to sum to 1.0
         */
        for ( int i = labels.length -1; i > 0; --i )
            newLabelsToScores.put( labels[i], scores[3-i] );

        Constituent second = new Constituent( newLabelsToScores, "rhyme", ta, 2, 4 );
        rhymeView.addConstituent(second);

        Map<String, Double> relLabelsToScores = new TreeMap<>();
        relLabelsToScores.put( "Yes", 0.8 );
        relLabelsToScores.put( "No", 0.2 );

        Relation rel = new Relation( relLabelsToScores, first, second );
        rhymeView.addRelation(rel);

        ta.addView("rhyme", rhymeView);

        // Serialize to protocol buffers format
        TextAnnotationImpl.TextAnnotationProto textAnnotationProto = ProtobufSerializer.writeTextAnnotation(ta);
        byte[] protoSerializedData = textAnnotationProto.toByteArray();

        TextAnnotationImpl.TextAnnotationProto protoRead =
                TextAnnotationImpl.TextAnnotationProto.parseFrom(protoSerializedData);
        TextAnnotation parsedTA = ProtobufSerializer.readTextAnnotation(protoRead);

        // Convert to JSON and verify content.
        String taJson = SerializationHelper.serializeToJson(parsedTA, true);

        JsonObject jobj = (JsonObject) new JsonParser().parse(taJson);
        JsonSerializerTest.verifySerializedJSONObject(jobj, ta);
    }

    @Test
    public void testSerializabilityWithOffsets() throws Exception {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);

        // making sure serialization does not fail, when some views (possibly by mistake) are null
        ta.addView("nullView", null);

        // Serialize to protocol buffers format
        TextAnnotationImpl.TextAnnotationProto textAnnotationProto = ProtobufSerializer.writeTextAnnotation(ta);
        byte[] protoSerializedData = textAnnotationProto.toByteArray();

        TextAnnotationImpl.TextAnnotationProto protoRead =
                TextAnnotationImpl.TextAnnotationProto.parseFrom(protoSerializedData);
        TextAnnotation parsedTA = ProtobufSerializer.readTextAnnotation(protoRead);

        // Convert to JSON and verify content.
        String json = SerializationHelper.serializeToJson(parsedTA, true);
        JsonSerializerTest.verifyDeserializedJsonString(json, ta);
    }
}
