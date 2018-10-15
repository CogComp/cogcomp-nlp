/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/*
 * This class is a legacy class, I keep it to make the current code compatible with the models I've
 * trained in the past
 */
public class WordEmbeddings {

    public enum NormalizationMethod {
        INDEPENDENT, OVERALL
    }


    static class WordEmbeddingsConfigurator extends Configurator {
        public static final Property fileNames =
                new Property(
                        "pathsToWordEmbeddings",
                        "WordEmbedding/model-2280000000.LEARNING_RATE=1e-08.EMBEDDING_LEARNING_RATE=1e-07.EMBEDDING_SIZE=50.gz");
        public static final Property dimensionalities = new Property("embeddingDimensionalities",
                "50");
        public static final Property wordNumThreshold = new Property(
                "minWordAppThresholdsForEmbeddings", "0");
        public static final Property normalizationConstants = new Property(
                "normalizationConstantsForEmbeddings", "1.0");

        public static final Property normalizationMethods = new Property(
                "normalizationMethodsForEmbeddings", "OVERALL");
        public static final Property isLowercase = new Property("isLowercaseWordEmbeddings",
                Configurator.FALSE);

        /**
         * get a ResourceManager object with the default key/value pairs for this configurator
         *
         * @return a non-null ResourceManager with appropriate values set.
         */
        @Override
        public ResourceManager getDefaultConfig() {
            Property[] props =
                    {fileNames, dimensionalities, wordNumThreshold, normalizationConstants,
                            normalizationMethods, isLowercase};
            return new ResourceManager(generateProperties(props));
        }
    }

    public static List<Boolean> isLowercasedEmbeddingByResource = null;
    public static List<String> resources = null;
    public static List<Integer> embeddingDimensionalities = null;
    public static int dimensionalitiesSum = 0;
    public static List<HashMap<String, double[]>> embeddingByResource = null;

    public static void initWithDefaults() throws IOException {
        ResourceManager rm = (new WordEmbeddingsConfigurator()).getDefaultConfig();

        List<String> fileNames = new LinkedList<>();
        fileNames.add(rm.getString(WordEmbeddingsConfigurator.fileNames.key));

        List<Integer> embeddingDimensionality = new LinkedList<>();
        embeddingDimensionality.add(rm.getInt(WordEmbeddingsConfigurator.dimensionalities.key));

        List<Integer> minWordAppearanceThres = new LinkedList<>();
        minWordAppearanceThres.add(rm.getInt(WordEmbeddingsConfigurator.wordNumThreshold.key));

        List<Boolean> isLowercasedEmbedding = new LinkedList<>();
        isLowercasedEmbedding.add(rm.getBoolean(WordEmbeddingsConfigurator.isLowercase.key));

        List<Double> normalizationConstant = new LinkedList<>();
        normalizationConstant.add(rm
                .getDouble(WordEmbeddingsConfigurator.normalizationConstants.key));

        List<NormalizationMethod> normalizationMethods = new LinkedList<>();
        normalizationMethods.add(NormalizationMethod.valueOf(rm
                .getString(WordEmbeddingsConfigurator.normalizationMethods.key)));

        init(fileNames, embeddingDimensionality, minWordAppearanceThres, isLowercasedEmbedding,
                normalizationConstant, normalizationMethods);
    }

    /*
     * For now, the parameter minWordAppearanceThres is not used, but I'm planning to use it like I
     * was using the word appearance thresholds on Brown Clusters
     */
    public static void init(List<String> filenames, List<Integer> embeddingDimensionality,
            List<Integer> minWordAppearanceThres, List<Boolean> isLowecasedEmbedding,
            List<Double> normalizationConstant, List<NormalizationMethod> methods)
            throws IOException {
        dimensionalitiesSum = 0;
        embeddingDimensionalities = new LinkedList<>();
        resources = new LinkedList<>();
        embeddingByResource = new LinkedList<>();
        isLowercasedEmbeddingByResource = new LinkedList<>();
        for (int resourceId = 0; resourceId < filenames.size(); resourceId++) {
            HashMap<String, double[]> embedding = new HashMap<>();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(LineIO.getInputStream(filenames
                            .get(resourceId))));
            double maxAbsValueInAnyDimension = 0;
            String line = in.readLine();
            while (line != null) {
                StringTokenizer st = new StringTokenizer(line, " ");
                String token = st.nextToken();
                List<String> v = new LinkedList<>();
                while (st.hasMoreTokens())
                    v.add(st.nextToken());
                if (v.size() != embeddingDimensionality.get(resourceId))
                    throw new IllegalArgumentException("Warning: unexpected dimensionality of "
                            + v.size() + " for token " + token);
                double[] arr = new double[v.size()];
                double maxInThisDimension = 0;
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = Double.parseDouble(v.get(i));
                    if (maxAbsValueInAnyDimension < Math.abs(arr[i]))
                        maxAbsValueInAnyDimension = Math.abs(arr[i]);
                    if (maxInThisDimension < Math.abs(arr[i]))
                        maxInThisDimension = Math.abs(arr[i]);
                }
                if (maxInThisDimension > 0
                        && methods.get(resourceId).equals(NormalizationMethod.INDEPENDENT))
                    for (int i = 0; i < arr.length; i++)
                        arr[i] =
                                arr[i]
                                        / (normalizationConstant.get(resourceId) * maxInThisDimension);
                embedding.put(token, arr);
                line = in.readLine();
            }
            in.close();
            if (maxAbsValueInAnyDimension > 0
                    && methods.get(resourceId).equals(NormalizationMethod.OVERALL))
                for (String s : embedding.keySet()) {
                    double[] arr = embedding.get(s);
                    for (int j = 0; j < arr.length; j++)
                        arr[j] =
                                arr[j]
                                        / (normalizationConstant.get(resourceId) * maxAbsValueInAnyDimension);
                }
            embeddingByResource.add(embedding);
            dimensionalitiesSum += embeddingDimensionality.get(resourceId);
            embeddingDimensionalities.add(embeddingDimensionality.get(resourceId));
            resources.add(filenames.get(resourceId));
            isLowercasedEmbeddingByResource.add(isLowecasedEmbedding.get(resourceId));
        }
    }

    public static double[] getEmbedding(Constituent w) {
        double[] res = new double[dimensionalitiesSum];
        int pos = 0;
        for (int resourceId = 0; resourceId < embeddingByResource.size(); resourceId++) {
            String word = w.getSurfaceForm();
            if (isLowercasedEmbeddingByResource.get(resourceId))
                word = word.toLowerCase();
            double[] v = new double[embeddingDimensionalities.get(resourceId)];
            for (int i = 0; i < v.length; i++)
                v[i] = 0;
            HashMap<String, double[]> embedding = embeddingByResource.get(resourceId);
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

}
