/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;

import java.util.HashMap;
import java.util.Hashtable;

public class ContextAggregation {

    /*
     * Make sure to call this function as a last possible function: this function already assumes
     * that the data was annotated with dictionaries etc.
     */
    public static void annotate(NEWord word) {
        if (word.params.featuresToUse.containsKey("aggregateContext")
                || word.params.featuresToUse
                        .containsKey("aggregateGazetteerMatches")) {
            int i = 0;
            NEWord w = word, last = word.nextIgnoreSentenceBoundary;

            Hashtable<NEWord, Boolean> takenWords = new Hashtable<>();
            takenWords.put(word, true);
            NEWord temp = word.nextIgnoreSentenceBoundary;
            int k = 0;
            while (temp != null && k < 3) {
                takenWords.put(temp, true);
                temp = temp.nextIgnoreSentenceBoundary;
                k++;
            }
            temp = word.previousIgnoreSentenceBoundary;
            k = 0;
            while (temp != null && k < 3) {
                takenWords.put(temp, true);
                temp = temp.previousIgnoreSentenceBoundary;
                k++;
            }



            for (i = 0; i < 200 && last != null; ++i)
                last = last.nextIgnoreSentenceBoundary;
            for (i = 0; i > -200 && w.previousIgnoreSentenceBoundary != null; --i)
                w = w.previousIgnoreSentenceBoundary;

            do {
                if (w.form.equalsIgnoreCase(word.form)
                        && Character.isUpperCase(word.form.charAt(0))
                        && Character.isLowerCase(w.form.charAt(0)))
                    updateFeatureCounts(word, "appearsDownCased");
                if (w.form.equalsIgnoreCase(word.form) && Character.isUpperCase(w.form.charAt(0))
                        && Character.isUpperCase(word.form.charAt(0)) && word != w) {
                    if (word.params.featuresToUse
                            .containsKey("aggregateContext")) {
                        if (w.previous == null)
                            updateFeatureCounts(word, "appearancesUpperStartSentence");
                        if (w.previous != null)
                            if (((NEWord) w.previous).form.endsWith("."))
                                updateFeatureCounts(word, "appearancesUpperStartSentence");
                        if (w.previous != null && (!((NEWord) w.previous).form.endsWith(".")))
                            updateFeatureCounts(word, "appearancesUpperMiddleSentence");

                        NEWord wtemp = w, lastTemp = w.nextIgnoreSentenceBoundary;
                        int j = 0;
                        for (j = 0; j < 2 && lastTemp != null; ++j)
                            lastTemp = lastTemp.nextIgnoreSentenceBoundary;
                        for (j = 0; j > -2 && wtemp.previousIgnoreSentenceBoundary != null; --j)
                            wtemp = wtemp.previousIgnoreSentenceBoundary;
                        do {
                            updateFeatureCounts(word, "context:" + j + ":" + wtemp.form);
                            if (word.params.brownClusters.getResources() != null) {
                                String[] brownPaths = word.params.brownClusters.getPrefixes(wtemp);
                                // for(int k=0;k<brownPaths.length;k++)
                                // updateFeatureCounts(word,"contextPath:"+j+":"+brownPaths[k]);
                                if (brownPaths.length > 0)
                                    updateFeatureCounts(word, "contextPath:" + j + ":"
                                            + brownPaths[0]);
                            }
                            wtemp = wtemp.nextIgnoreSentenceBoundary;
                            j++;
                        } while (wtemp != lastTemp);
                    }
                }
                w = w.nextIgnoreSentenceBoundary;
            } while (w != last);
        }
    }

    private static void updateFeatureCounts(NEWord w, String feature) {
        HashMap<String, Integer> nonlocal = w.getNonLocalFeatures();
        if (nonlocal.containsKey(feature)) {
            int i = nonlocal.get(feature) + 1;
            nonlocal.remove(feature);
            nonlocal.put(feature, i);
        } else
            nonlocal.put(feature, 1);
    }

}
