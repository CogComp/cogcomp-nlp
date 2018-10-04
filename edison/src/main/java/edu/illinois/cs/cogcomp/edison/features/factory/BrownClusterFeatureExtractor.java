/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.annotators.BrownClusterViewGenerator;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@code WordFeatureExtractor} that generates prefixes of brown cluster Ids for each word. Using
 * BrownClusterViewGenerator, which now supports multiple Brown cluster sources to be used
 * simultaneously.
 *
 * @keywords named entity recognizer, ner, brown, embeddings
 * @author Vivek Srikumar
 * @see WordFeatureExtractor
 */
public class BrownClusterFeatureExtractor extends WordFeatureExtractor {

    public static final BrownClusterFeatureExtractor instance100, instance320, instance1000,
            instance3200;

    public final static int[] prefixes = new int[] {4, 6, 10, 20};

    static {
        try {
            instance100 =
                    new BrownClusterFeatureExtractor("100", BrownClusterViewGenerator.file100,
                            prefixes);
            instance320 =
                    new BrownClusterFeatureExtractor("320", BrownClusterViewGenerator.file320,
                            prefixes);
            instance1000 =
                    new BrownClusterFeatureExtractor("1000", BrownClusterViewGenerator.file1000,
                            prefixes);
            instance3200 =
                    new BrownClusterFeatureExtractor("3200", BrownClusterViewGenerator.file3200,
                            prefixes);
        } catch (EdisonException e) {
            throw new RuntimeException(e);
        }
    }

    private final int[] prefixLengths;
    private BrownClusterViewGenerator viewGenerator;
    private String brownClustersFile;
    private String name;

    public BrownClusterFeatureExtractor(String name, String brownClustersFile, int[] prefixLengths)
            throws EdisonException {
        this(name, brownClustersFile, prefixLengths, true);
    }

    /**
     * @see WordFeatureExtractor#WordFeatureExtractor(boolean)
     */
    public BrownClusterFeatureExtractor(String name, String brownClustersFile, int[] prefixLengths,
            boolean useLastWord) throws EdisonException {
        super(useLastWord);
        this.name = name;
        this.brownClustersFile = brownClustersFile;
        this.prefixLengths = new int[prefixLengths.length];
        System.arraycopy(prefixLengths, 0, this.prefixLengths, 0, prefixLengths.length);
    }

    private void lazyLoadClusters(String brownClustersFile) throws EdisonException {
        if (viewGenerator == null) {

            synchronized (BrownClusterFeatureExtractor.class) {
                if (viewGenerator == null) {
                    try {

                        viewGenerator = new BrownClusterViewGenerator(name, brownClustersFile);
                    } catch (final Exception e) {
                        throw new EdisonException(e);
                    }
                }
            }
        }
    }

    public BrownClusterViewGenerator getViewGenerator() throws EdisonException {
        lazyLoadClusters(brownClustersFile);
        return this.viewGenerator;
    }

    @Override
    public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
        lazyLoadClusters(brownClustersFile);

        if (!ta.hasView(viewGenerator.getViewName())) {
            synchronized (BrownClusterFeatureExtractor.class) {
                View view = null;
                try {
                    view = viewGenerator.getView(ta);
                } catch (AnnotatorException e) {
                    e.printStackTrace();
                    throw new EdisonException(e.getMessage());
                }
                ta.addView(viewGenerator.getViewName(), view);
            }
        }

        SpanLabelView view = (SpanLabelView) ta.getView(viewGenerator.getViewName());

        String word = ta.getToken(wordPosition);

        // What follows has a subtle bug: view.getLabel only gets the first
        // label for the word. A word can have multiple brown clusters though!
        // This has been fixed below.
        // String cluster = view.getLabel(wordPosition);
        //
        // return getBrownClusters(word, cluster);

        Set<Feature> features = new LinkedHashSet<>();

        for (Constituent c : view.getConstituentsCoveringToken(wordPosition)) {
            String cluster = c.getLabel();
            features.addAll(getBrownClusters(word, cluster));
        }

        return features;

    }

    private Set<Feature> getBrownClusters(String word, String cluster) {
        Set<Feature> set = new LinkedHashSet<>();
        if (cluster.length() == 0) {
            set.add(DiscreteFeature.create("*"));
            set.add(DiscreteFeature.create("*-" + word));
            return set;
        }

        for (int length : prefixLengths) {
            if (cluster.length() >= length) {
                set.add(DiscreteFeature.create("prefix-" + length + ":"
                        + cluster.substring(0, length)));
            }
        }

        return set;
    }

    public String getName() {
        return "#brwn:" + name + "#";
    }

}
