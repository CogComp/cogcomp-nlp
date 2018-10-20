/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.RealPrimitiveFeature;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.util.ByteString;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Vivek Srikumar
 */
public class FeatureUtilities {
    private static final ByteString trueValue = new ByteString("true");

    /**
     * Convert a set of strings into a set of discrete features.
     */
    public static Set<Feature> getFeatures(Set<String> features) {
        Set<Feature> feats = new LinkedHashSet<>();

        for (String s : features)
            feats.add(new DiscreteFeature(s));
        return feats;
    }

    /**
     * Convert a map from feature names to values into a set of real valued features.
     */
    public static Set<Feature> getFeatures(Map<String, Float> features) {
        Set<Feature> feats = new LinkedHashSet<>();

        for (Entry<String, Float> s : features.entrySet())
            feats.add(new RealFeature(s.getKey(), s.getValue()));
        return feats;
    }

    /**
     * Convert a counter over strings into a set of real valued features.
     */
    public static Set<Feature> getFeatures(Counter<String> counter) {
        Set<Feature> feats = new LinkedHashSet<>();

        for (String s : counter.items())
            feats.add(new RealFeature(s, (float) counter.getCount(s)));
        return feats;
    }

    /**
     * Add a prefix to all the features in the set.
     *
     * @param prefix The prefix to be added.
     * @param features The feature set
     */
    public static Set<Feature> prefix(String prefix, Set<Feature> features) {
        Set<Feature> feats = new LinkedHashSet<>();

        for (Feature s : features)
            feats.add(s.prefixWith(prefix));
        return feats;
    }

    /**
     * Converts a Set of features into an LBJ friendly feature vector.
     */
    public synchronized static FeatureVector getLBJFeatures(Set<Feature> features) {
        FeatureVector feats = new FeatureVector();
        for (Feature feat : features) {
            feats.addFeature(getLBJFeature(feat));
        }
        return feats;
    }

    /**
     * Convert an edison feature into an LBJ feature
     */
    public static edu.illinois.cs.cogcomp.lbjava.classify.Feature getLBJFeature(Feature feature) {
        if (feature instanceof DiscreteFeature)
            return new DiscretePrimitiveFeature("", "", new ByteString(feature.getName()),
                    trueValue);
        else
            return new RealPrimitiveFeature("", "", new ByteString(feature.getName()),
                    feature.getValue());
    }

    /**
     * Convert an edison feature extractor into an LBJ classifier
     */
    @SuppressWarnings("serial")
    public static Classifier getLBJFeatureExtractor(final FeatureExtractor fex) {
        return new Classifier() {
            @Override
            public FeatureVector classify(Object arg0) {
                assert arg0 instanceof Constituent;
                try {
                    return getLBJFeatures(fex.getFeatures((Constituent) arg0));
                } catch (EdisonException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Create a feature map using the specified feature extractor on the input constituent.
     */
    public static Map<String, Float> getFeatureMap(final FeatureExtractor fex, Constituent c)
            throws EdisonException {
        Map<String, Float> map = new HashMap<>();

        for (Feature f : fex.getFeatures(c)) {
            map.put(f.getName(), f.getValue());
        }

        return map;
    }

    /**
     * Create a feature set using the specified feature extractor on the input constituent.
     * <b>Note</b>: If there are any real valued features, they will be ignored.
     */
    public static Set<String> getFeatureSet(final FeatureExtractor fex, Constituent c)
            throws EdisonException {
        Set<String> set = new LinkedHashSet<>();

        for (Feature f : fex.getFeatures(c)) {
            if (f instanceof DiscreteFeature)
                set.add(f.getName());
        }

        return set;
    }

    public static List<String> getFeaturesFromTextAnnotation(final FeatureExtractor fex, TextAnnotation s) {
        List<Constituent> cons = s.getView(ViewNames.TOKENS).getConstituents();
        List<String> features = new ArrayList<>();
        for(Constituent c : cons) {
            try {
                features.addAll(getFeatureSet(fex, c));
            } catch (EdisonException e) {
                e.printStackTrace();
            }
        }
        return features;
    }

    public static List<String> getFeatureSet(Set<Feature> features) {
        List<String> set = new ArrayList<>();
        for (Feature f : features) {
            if (f instanceof DiscreteFeature)
                set.add(f.getName());
        }
        return set;
    }

    /**
     * Convert a feature set into a pair of arrays of integers and doubles by looking up the feature
     * name in the provided lexicon.
     *
     * @param features The feature set
     * @param lexicon The lexicon
     * @param trainingMode Should an unseen feature string be added to the lexicon? If this is
     *        false, unseen features will be given an ID whose value is one more than the number of
     *        features.
     * @return a pair of int[] and double[], representing the feature ids and values.
     */
    public static Pair<int[], double[]> convert(Set<Feature> features, Lexicon lexicon,
            boolean trainingMode) {

        TIntDoubleHashMap fMap = new TIntDoubleHashMap(features.size());

        for (Feature feature : features) {
            final int featureId = FeatureUtilities.getFeatureId(lexicon, trainingMode, feature);

            if (featureId < 0)
                continue;

            double value = feature.getValue() + fMap.get(featureId);
            fMap.put(featureId, value);
        }

        int[] idsOriginal = fMap.keys();
        int[] ids = new int[idsOriginal.length];
        System.arraycopy(idsOriginal, 0, ids, 0, ids.length);

        Arrays.sort(ids);

        double[] vals = new double[fMap.size()];

        int count = 0;
        for (int key : ids) {
            vals[count++] = fMap.get(key);
        }

        return new Pair<>(ids, vals);
    }

    /**
     * This function gets the feature from the lexicon using the id. Use this function if you use
     * either getFeatureId or convert to avoid off-by-one errors.
     */
    public static edu.illinois.cs.cogcomp.lbjava.classify.Feature getFeature(Lexicon lexicon, int id) {
        return lexicon.lookupKey(id - 1);
    }

    /**
     * Returns the id of the feature according to the lexicon. If trainingMode is set to false and
     * the feature is not found in the lexicon, then the function returns -1.
     */
    public static int getFeatureId(Lexicon lexicon, boolean trainingMode, Feature feature) {

        edu.illinois.cs.cogcomp.lbjava.classify.Feature f = getLBJFeature(feature);

        // Get the feature key only for real features because for discrete
        // features, this will return the original feature itself
        if (f instanceof RealPrimitiveFeature)
            f = f.getFeatureKey(lexicon, trainingMode, -1);

        // System.out.print(featureKey + "\t");

        int id;
        synchronized (lexicon) {
            id = lexicon.lookup(f, trainingMode, -1);
        }
        // if it is not in the training mode, check if this feature is pruned
        // off. if so, return -1 so that the caller knows and does something
        // about it.
        if (!trainingMode) {
            if (id == lexicon.getCutoff())
                return -1;
            else {
                // WHy the +1? To get LBJ to play well with JLIS. JLIS
                // *mandates*
                // that
                // all feature indices should be greater than zero.
                return id + 1;
            }
        } else
            return id + 1;
    }

    /**
     * Conjoins two feature extractors. This creates a new feature extractor that produces conjoined
     * features.
     *
     * @param f1 One feature extractor
     * @param f2 Another feature extractor
     * @return A new feature extractor that produces feature conjunctions
     */
    public static FeatureExtractor conjoin(final FeatureExtractor f1, final FeatureExtractor f2) {
        return new FeatureExtractor() {

            @Override
            public String getName() {
                if (f1.getName().length() > 0 && f2.getName().length() > 0)
                    return f1.getName() + "&" + f2.getName();
                else if (f1.getName().length() > 0)
                    return f1.getName();
                else if (f2.getName().length() > 0)
                    return f2.getName();
                else
                    return "";
            }

            @Override
            public Set<Feature> getFeatures(Constituent c) throws EdisonException {

                Set<Feature> feats1 = f1.getFeatures(c);
                if (f2 == f1) {
                    return conjoin(feats1, feats1);
                } else {
                    Set<Feature> feats2 = f2.getFeatures(c);

                    return conjoin(feats1, feats2);
                }
            }

        };
    }

    /**
     * Conjoins two discrete features corresponding to the strings left and right.
     */
    public static Feature conjunct(String left, String right) {
        return DiscreteFeature.create(left + "&" + right);
    }

    /**
     * Conjoins two feature sets. This produces a cross product of features. That is, every feature
     * from the first parameter is conjoined with every feature of the second one. If either of the
     * input sets are empty, this function returns the empty set.
     *
     * @param feats1 The first feature set
     * @param feats2 The second feature set
     * @return A new feature set that contains feature conjunctions
     */
    public static Set<Feature> conjoin(Set<Feature> feats1, Set<Feature> feats2) {

        if (feats1.size() == 0 || feats2.size() == 0)
            return new LinkedHashSet<>();

        Set<Feature> features = new LinkedHashSet<>(feats1.size() * feats2.size());

        for (Feature a : feats1) {
            for (Feature b : feats2) {

                /**
                 * this next equality is correct: checking for literally the same Feature object, in
                 * which case only return the single object. Regular equality filters out instances
                 * where two identical feature strings occur next to each other -- e.g. the words
                 * "buffalo buffalo" -- in which case you want the conjoined feature
                 * "buffalo&buffalo" and not just "buffalo".
                 */
                if (a == b)
                    features.add(a);
                else
                    features.add(a.conjoinWith(b));
            }
        }
        return features;
    }

    /**
     * Serializes a set of features into a byte array.
     */
    public static byte[] serializeFeatureSet(Set<Feature> features) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

        writer.write(features.size() + "\n");

        for (Feature feature : features) {

            if (feature instanceof DiscreteFeature) {
                DiscreteFeature d = (DiscreteFeature) feature;
                String s = d.getName();

                writer.write("d:" + s + "\n");

            } else if (feature instanceof RealFeature) {
                RealFeature r = (RealFeature) feature;
                String s = r.getName() + "\t" + r.getValue();
                writer.write("r:" + s + "\n");
            } else {
                throw new RuntimeException("Invalid feature!");
            }
        }

        writer.flush();
        return baos.toByteArray();
    }

    /**
     * Deserializes a byte array into a set of features
     */
    public static Set<Feature> deserializeFeatureSet(byte[] bytes) throws IOException {

        BufferedReader in =
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

        Set<Feature> features = new LinkedHashSet<>();

        // loop over and get this list
        int numFeatures = Integer.parseInt(in.readLine());
        for (int i = 0; i < numFeatures; i++) {
            String s = in.readLine().trim();

            if (s.startsWith("d:")) {
                features.add(DiscreteFeature.create(s.substring(2)));
            } else if (s.startsWith("r")) {
                s = s.substring(2);
                String[] parts = s.split("\t");

                assert parts.length == 2 : s + " is not in the valid format";
                String name = parts[0];
                float value = Float.parseFloat(parts[1]);

                features.add(RealFeature.create(name, value));
            }

        }

        in.close();
        return features;

    }
}
