/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 * <p>
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.ner;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureCreatorUtil;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.apache.commons.lang.StringUtils;
import org.cogcomp.DatastoreException;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Standard unit test for WordEmbeddingsWindow ConstituentFeatureExtractor. Currently inactive because not
 * active in NER, needs embedding resource.
 *
 * @author mssammon
 */
public class TestWordEmbeddingWindow {
    @Test
    public final void test() throws InvalidPortException, DatastoreException, InvalidEndpointException {
        boolean withNoise = false;
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(withNoise, 3);

        int targetIndex = 6;

        Constituent c = ta.getView(ViewNames.TOKENS).getConstituents().get(targetIndex);

        boolean ignoreSentenceBoundaries = false;
        WordEmbeddingWindow wew = null;
        try {
            wew = new WordEmbeddingWindow(2, ignoreSentenceBoundaries);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        String expectedFeatures = "";

        String actualFeatStr = "";

        try {
            Set<Feature> actualFeatures = wew.getFeatures(c);

            actualFeatStr = StringUtils.join(actualFeatures, ",");
        } catch (EdisonException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertEquals(expectedFeatures, actualFeatStr);
    }

}
