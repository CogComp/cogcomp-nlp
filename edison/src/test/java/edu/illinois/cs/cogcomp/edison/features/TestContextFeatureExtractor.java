/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Vivek Srikumar
 */
public class TestContextFeatureExtractor {

    @Test
    public void testGetFeaturesIndexWithoutConstituent() throws EdisonException {
        ContextFeatureExtractor fex = new ContextFeatureExtractor(2, true, true);

        fex.addFeatureExtractor(new WordFeatureExtractor() {

            @Override
            public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition)
                    throws EdisonException {
                String s = WordHelpers.getWord(ta, wordPosition).toLowerCase();

                Set<Feature> ss = new HashSet<>();
                ss.add(DiscreteFeature.create(s));
                return ss;
            }
        });

        TextAnnotation ta =
                TextAnnotationUtilities
                        .createFromTokenizedString("This is a test for the feature extractor .");

        Constituent c1 = new Constituent("", "", ta, 2, 3);

        Set<String> c1fs = new HashSet<>();
        c1fs.addAll(Arrays.asList("context-2:#word#:this", "context-1:#word#:is",
                "context1:#word#:test", "context2:#word#:for"));

        Set<Feature> c1f = FeatureUtilities.getFeatures(c1fs);
        Set<Feature> features = fex.getFeatures(c1);

        c1f.removeAll(features);
        assertEquals(0, c1f.size());

        Constituent c2 = new Constituent("", "", ta, 2, 4);

        Set<String> c2fs = new HashSet<>();
        c2fs.addAll(Arrays.asList("context-2:#word#:this", "context-1:#word#:is",
                "context1:#word#:for", "context2:#word#:the"));

        Set<Feature> c2f = FeatureUtilities.getFeatures(c2fs);
        c2f.removeAll(fex.getFeatures(c2));
        assertEquals(0, c2f.size());

    }

    @Test
    public void testGetFeaturesIndexWithConstituent() throws EdisonException {
        ContextFeatureExtractor fex = new ContextFeatureExtractor(2, true, false);

        fex.addFeatureExtractor(new WordFeatureExtractor() {

            @Override
            public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition)
                    throws EdisonException {
                String s = WordHelpers.getWord(ta, wordPosition).toLowerCase();

                Set<Feature> ss = new HashSet<>();
                ss.add(DiscreteFeature.create(s));
                return ss;
            }
        });

        TextAnnotation ta =
                TextAnnotationUtilities
                        .createFromTokenizedString("This is a test for the feature extractor .");

        Constituent c1 = new Constituent("", "", ta, 2, 3);

        Set<String> c1fs = new HashSet<>();
        c1fs.addAll(Arrays.asList("context-2:#word#:this", "context-1:#word#:is",
                "context*:#word#:a", "context1:#word#:test", "context2:#word#:for"));
        Set<Feature> c1f = FeatureUtilities.getFeatures(c1fs);

        Set<Feature> features = fex.getFeatures(c1);

        c1f.removeAll(features);
        assertEquals(0, c1f.size());

        Constituent c2 = new Constituent("", "", ta, 2, 4);

        Set<String> c2fs = new HashSet<>();
        c2fs.addAll(Arrays.asList("context-2:#word#:this", "context-1:#word#:is",
                "context*:#word#:a", "context*:#word#:test", "context1:#word#:for",
                "context2:#word#:the"));

        Set<Feature> c2f = FeatureUtilities.getFeatures(c2fs);

        c2f.removeAll(fex.getFeatures(c2));
        assertEquals(0, c2f.size());

    }

    @Test
    public void testGetFeaturesNoIndexWithoutConstituent() throws EdisonException {
        ContextFeatureExtractor fex = new ContextFeatureExtractor(2, false, true);

        fex.addFeatureExtractor(new WordFeatureExtractor() {

            @Override
            public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition)
                    throws EdisonException {
                String s = WordHelpers.getWord(ta, wordPosition).toLowerCase();

                Set<String> ss = new HashSet<>();
                ss.add(s);
                return FeatureUtilities.getFeatures(ss);
            }
        });

        TextAnnotation ta =
                TextAnnotationUtilities
                        .createFromTokenizedString("This is a test for the feature extractor .");

        Constituent c1 = new Constituent("", "", ta, 2, 3);

        Set<String> c1fs = new HashSet<>();
        c1fs.addAll(Arrays.asList("context:#word#:this", "context:#word#:is",
                "context:#word#:test", "context:#word#:for"));

        Set<Feature> c1f = FeatureUtilities.getFeatures(c1fs);
        c1f.removeAll(fex.getFeatures(c1));
        assertEquals(0, c1f.size());

        Constituent c2 = new Constituent("", "", ta, 2, 4);

        Set<String> c2fs = new HashSet<>();
        c2fs.addAll(Arrays.asList("context:#word#:this", "context:#word#:is", "context:#word#:for",
                "context:#word#:the"));

        Set<Feature> c2f = FeatureUtilities.getFeatures(c2fs);

        c2f.removeAll(fex.getFeatures(c2));
        assertEquals(0, c2f.size());

    }

    @Test
    public void testGetFeaturesNoIndexWithConstituent() throws EdisonException {
        ContextFeatureExtractor fex = new ContextFeatureExtractor(2, false, false);

        fex.addFeatureExtractor(new WordFeatureExtractor() {

            @Override
            public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition)
                    throws EdisonException {
                String s = WordHelpers.getWord(ta, wordPosition).toLowerCase();

                Set<String> ss = new HashSet<>();
                ss.add(s);
                return FeatureUtilities.getFeatures(ss);
            }
        });

        TextAnnotation ta =
                TextAnnotationUtilities
                        .createFromTokenizedString("This is a test for the feature extractor .");

        Constituent c1 = new Constituent("", "", ta, 2, 3);

        Set<String> c1fs = new HashSet<>();
        c1fs.addAll(Arrays.asList("context:#word#:this", "context:#word#:is", "context:#word#:a",
                "context:#word#:test", "context:#word#:for"));

        Set<Feature> c1f = FeatureUtilities.getFeatures(c1fs);

        c1f.removeAll(fex.getFeatures(c1));
        assertEquals(0, c1f.size());

        Constituent c2 = new Constituent("", "", ta, 2, 4);

        Set<String> c2fs = new HashSet<>();
        c2fs.addAll(Arrays.asList("context:#word#:this", "context:#word#:is", "context:#word#:a",
                "context:#word#:test", "context:#word#:for", "context:#word#:the"));

        Set<Feature> c2f = FeatureUtilities.getFeatures(c2fs);

        c2f.removeAll(fex.getFeatures(c2));
        assertEquals(0, c2f.size());

    }
}
