/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FeatureManifestTest {
    public static final String file = "test.fex";
    private static List<TextAnnotation> tas;

    @Before
    public void setUp() throws Exception {
        tas = IOUtils.readObjectAsResource(FeatureManifestTest.class, "test.ta");
    }

    @Test
    public void testCreateFex() throws Exception {
        FeatureManifest featureManifest = new FeatureManifest(file);

        featureManifest.useCompressedName();
        featureManifest.setVariable("*default-parser*", ViewNames.PARSE_STANFORD);

        FeatureExtractor fex = featureManifest.createFex();
        Constituent c = tas.get(0).getView(ViewNames.TOKENS).getConstituents().get(0);
        assertEquals("My", c.getSurfaceForm());
        Set<Feature> features = fex.getFeatures(c);
        Iterator<Feature> iterator = features.iterator();
        Feature feature = iterator.next();
        assertEquals("f:#ctxt#:context1::#wd:mother-in-law", feature.getName());
    }
}
