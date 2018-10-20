/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;

import java.util.*;

/**
 * This class is used to compare features extracted by the new SRL in Edison and extracted by
 * illinois.srl. This class also have methods for comparing feature extractors in new SRL in Edison
 * and illinois.srl. Because the illinois.srl caches extracted features, but the new SRL does not,
 * this class contains two kinds of methods for checking equality: a regular kind methods used when
 * cache does not matter for feature extraction, and the other kind of method used when cache does
 * matter for features extraction.
 *
 * @author Xinbo Wu
 */
public class SRLFeaturesComparator {

    /**
     * Check if two sets of SRL feature are equal, when cache does not matter for feature
     * extraction.
     *
     * @param feats_1 a set of SRL features
     * @param feats_2 another set SRL of features
     * @return if the two set of SRL features are equal
     */
    public static boolean isEqual(Set<Feature> feats_1, Set<Feature> feats_2) {

        boolean isEqual = true;

        if (feats_1.size() != feats_2.size())
            return false;


        Iterator<Feature> iter_1 = feats_1.iterator();
        Iterator<Feature> iter_2 = feats_2.iterator();


        while (iter_1.hasNext()) {

            ArrayList<String> splited_1 =
                    new ArrayList(Arrays.asList(iter_1.next().getName().split(":")));
            ArrayList<String> splited_2 =
                    new ArrayList(Arrays.asList(iter_2.next().getName().split(":")));

            splited_1.removeAll(Collections.singleton(""));
            splited_2.removeAll(Collections.singleton(""));

            for (int i = 1; i < splited_1.size(); i++) {
                if (splited_1.get(i).equals(splited_2.get(i)))
                    continue;
                else if (splited_1.get(i).equals("#word#") && splited_2.get(i).equals("#wd"))
                    continue;
                else
                    return false;
            }

        }

        return isEqual;
    }

    /**
     * Check if two sets of SRL feature are equal, when cache does matter for feature extraction.
     *
     * @param feats_1 a set of SRL features
     * @param feats_2 another set SRL of features
     * @return if the two set of SRL features are equal
     */
    public static boolean isNoCacheEqual(Set<Feature> feats_1, Set<Feature> feats_2) {
        ArrayList<Feature> feats_long;
        ArrayList<Feature> feats_short;

        if (feats_1.size() > feats_2.size()) {
            feats_long = new ArrayList(feats_1);
            feats_short = new ArrayList(feats_2);

        } else if (feats_1.size() < feats_2.size()) {
            feats_long = new ArrayList(feats_2);
            feats_short = new ArrayList(feats_1);

        } else
            return isEqual(feats_1, feats_2);


        boolean hold = false;

        ArrayList<String> splited_long;
        ArrayList<String> splited_short = null;

        for (int i = 0, j = 0; i < feats_short.size();) {
            if ((feats_long.size() - j - 1) < (feats_short.size() - i - 1))
                return false;

            if (!hold) {
                splited_long = new ArrayList(Arrays.asList(feats_long.get(j).getName().split(":")));
                splited_short =
                        new ArrayList(Arrays.asList(feats_short.get(i).getName().split(":")));

                splited_long.removeAll(Collections.singleton(""));
                splited_short.removeAll(Collections.singleton(""));
            } else {
                splited_long = new ArrayList(Arrays.asList(feats_long.get(j).getName().split(":")));

                splited_long.removeAll(Collections.singleton(""));
            }


            for (int k = 1; k < splited_short.size(); k++) {
                if (splited_short.get(k).equals(splited_long.get(k))) {
                    hold = false;
                    continue;

                } else if (splited_short.get(k).equals("#word#")
                        && splited_long.get(k).equals("#wd")) {
                    hold = false;
                    continue;

                } else {
                    hold = true;
                    break;

                }
            }

            if (!hold) {
                i++;
                j++;

            } else {
                j++;

            }

        }
        return !hold;
    }

    /**
     * Check if two SRL feature extractors are equal, when cache does not matter for feature
     * extraction.
     *
     * @param c a constituent
     * @param fex_1 a SRL feature extractor
     * @param fex_2 another SRL feature extractor
     * @return if the features extracted by two SRL feature extractors for one single constituent
     *         are equal
     * @throws Exception
     */
    public static boolean isEqual(Constituent c, FeatureExtractor fex_1, FeatureExtractor fex_2)
            throws Exception {
        Set<Feature> feats_1 = fex_1.getFeatures(c);
        Set<Feature> feats_2 = fex_2.getFeatures(c);

        return isEqual(feats_1, feats_2);
    }

    /**
     * Check if two SRL feature extractors are equal, when cache does matter for feature extraction.
     *
     * @param c a constituent
     * @param fex_1 a SRL feature extractor
     * @param fex_2 another SRL feature extractor
     * @return if the features extracted by two SRL feature extractors for one single constituent
     *         are equal
     * @throws Exception
     */
    public static boolean isNoCacheEqual(Constituent c, FeatureExtractor fex_1,
            FeatureExtractor fex_2) throws Exception {
        Set<Feature> feats_1 = fex_1.getFeatures(c);
        Set<Feature> feats_2 = fex_2.getFeatures(c);

        return isNoCacheEqual(feats_1, feats_2);
    }

}
