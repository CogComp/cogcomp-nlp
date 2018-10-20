/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.IO.ResourceUtilities;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

/*
 * This class is a legacy class, I keep it to make the current code compatible with the models I've
 * trained in the past
 */
public class WordEmbeddings {

    public enum NormalizationMethod {
        INDEPENDENT, OVERALL
    }

    public static Vector<Boolean> isLowecasedEmbeddingByResource = null;
    public static Vector<String> resources = null;
    public static Vector<Integer> dimensionalities = null;
    public static int dimensionalitiesSum = 0;
    public static Vector<HashMap<String, double[]>> embeddingByResource = null;

    /*
     * For now, the parameter minWordAppearanceThres is not used, but I'm planning to use it like I
     * was using the word appearance thresholds on Brown Clusters
     */
    public static void init(Vector<String> filenames, Vector<Integer> embeddingDimensionality,
            Vector<Integer> minWordAppearanceThres, Vector<Boolean> isLowecasedEmbedding,
            Vector<Double> normalizationConstant, Vector<NormalizationMethod> methods) {
        dimensionalitiesSum = 0;
        dimensionalities = new Vector<>();
        resources = new Vector<>();
        embeddingByResource = new Vector<>();
        isLowecasedEmbeddingByResource = new Vector<>();
        for (int resourceId = 0; resourceId < filenames.size(); resourceId++) {
            HashMap<String, double[]> embedding = new HashMap<>();
            InFile in = new InFile(ResourceUtilities.loadResource(filenames.elementAt(resourceId)));
            String line = in.readLine();
            double maxAbsValueInAnyDimension = 0;
            while (line != null) {
                StringTokenizer st = new StringTokenizer(line, " ");
                String token = st.nextToken();
                Vector<String> v = new Vector<>();
                while (st.hasMoreTokens())
                    v.addElement(st.nextToken());
                if (v.size() != embeddingDimensionality.elementAt(resourceId))
                    throw new IllegalArgumentException("Warning: unexpected dimensionality of "
                            + v.size() + " for token " + token);
                double[] arr = new double[v.size()];
                double maxInThisDimension = 0;
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = Double.parseDouble(v.elementAt(i));
                    if (maxAbsValueInAnyDimension < Math.abs(arr[i]))
                        maxAbsValueInAnyDimension = Math.abs(arr[i]);
                    if (maxInThisDimension < Math.abs(arr[i]))
                        maxInThisDimension = Math.abs(arr[i]);
                }
                if (maxInThisDimension > 0
                        && methods.elementAt(resourceId).equals(NormalizationMethod.INDEPENDENT))
                    for (int i = 0; i < arr.length; i++)
                        arr[i] =
                                arr[i]
                                        / (normalizationConstant.elementAt(resourceId) * maxInThisDimension);
                embedding.put(token, arr);
                line = in.readLine();
            }
            in.close();
            if (maxAbsValueInAnyDimension > 0
                    && methods.elementAt(resourceId).equals(NormalizationMethod.OVERALL))
                for (String s : embedding.keySet()) {
                    double[] arr = embedding.get(s);
                    for (int j = 0; j < arr.length; j++)
                        arr[j] =
                                arr[j]
                                        / (normalizationConstant.elementAt(resourceId) * maxAbsValueInAnyDimension);
                }
            embeddingByResource.addElement(embedding);
            dimensionalitiesSum += embeddingDimensionality.elementAt(resourceId);
            dimensionalities.addElement(embeddingDimensionality.elementAt(resourceId));
            resources.addElement(filenames.elementAt(resourceId));
            isLowecasedEmbeddingByResource.addElement(isLowecasedEmbedding.elementAt(resourceId));
        }
    }

    public static double[] getEmbedding(NEWord w) {
        double[] res = new double[dimensionalitiesSum];
        int pos = 0;
        for (int resourceId = 0; resourceId < embeddingByResource.size(); resourceId++) {
            String word = w.form;
            if (isLowecasedEmbeddingByResource.elementAt(resourceId))
                word = word.toLowerCase();
            double[] v = new double[dimensionalities.elementAt(resourceId)];
            for (int i = 0; i < v.length; i++)
                v[i] = 0;
            HashMap<String, double[]> embedding = embeddingByResource.elementAt(resourceId);
            if (embedding.containsKey(word))
                v = embedding.get(word);
            else {
                if (embedding.containsKey("*UNKNOWN*"))
                    v = embedding.get("*UNKNOWN*");
                else if (embedding.containsKey("*unknown*"))
                    v = embedding.get("*unknown*");
            }
            for (double aV : v) {
                res[pos] = aV;
                pos++;
            }
        }
        return res;
    }

    public static void printOovData(Data data) {
        HashMap<String, Boolean> tokensHash = new HashMap<>();
        HashMap<String, Boolean> tokensHashIC = new HashMap<>();
        for (int docid = 0; docid < data.documents.size(); docid++) {
            NERDocument doc = data.documents.get(docid);
            for (int i = 0; i < doc.sentences.size(); i++)
                for (int j = 0; j < doc.sentences.get(i).size(); j++) {
                    String form = ((NEWord) doc.sentences.get(i).get(j)).form;
                    tokensHash.put(form, true);
                    tokensHashIC.put(form.toLowerCase(), true);
                }
        }
        // logger.info("Data statistics:");
        // logger.info("\t\t- Total tokens with repetitions ="+ totalTokens);
        // logger.info("\t\t- Total unique tokens  ="+ tokensHash.size());
        // logger.info("\t\t- Total unique tokens ignore case ="+ tokensHashIC.size());
        for (int resourceId = 0; resourceId < resources.size(); resourceId++) {
            HashMap<String, double[]> embedding = embeddingByResource.elementAt(resourceId);
            HashMap<String, Boolean> oovCaseSensitiveHash = new HashMap<>();
            HashMap<String, Boolean> oovAfterLowercasingHash = new HashMap<>();
            for (int docid = 0; docid < data.documents.size(); docid++) {
                NERDocument doc = data.documents.get(docid);
                for (int i = 0; i < doc.sentences.size(); i++)
                    for (int j = 0; j < doc.sentences.get(i).size(); j++) {
                        String form = ((NEWord) doc.sentences.get(i).get(j)).form;
                        if (!embedding.containsKey(form)) {
                            oovCaseSensitiveHash.put(form, true);
                        }
                        if ((!embedding.containsKey(form))
                                && (!embedding.containsKey(form.toLowerCase()))) {
                            oovAfterLowercasingHash.put(form.toLowerCase(), true);
                        }
                    }
            }
            // logger.info("\t\t- Total OOV tokens, Case Sensitive ="+ oovCaseSensitive);
            // logger.info("\t\t- OOV tokens, no repetitions, Case Sensitive ="+
            // oovCaseSensitiveHash.size());
            // logger.info("\t\t- Total OOV tokens even after lowercasing  ="+
            // oovAfterLowercasing);
            // logger.info("\t\t- OOV tokens even after lowercasing, no repetition  ="+
            // oovAfterLowercasingHash.size());
        }
    }
}
