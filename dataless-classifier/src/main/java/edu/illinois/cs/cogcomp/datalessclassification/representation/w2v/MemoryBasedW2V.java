/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.representation.w2v;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.W2VDatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.representation.AEmbedding;
import edu.illinois.cs.cogcomp.datalessclassification.util.DenseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.DenseVectorOperations;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * Computes Word2Vec Embedding for a query
 * Loads up all the required DataStructures in memory
 * 
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class MemoryBasedW2V extends AEmbedding<Integer> {
    private static Logger logger = LoggerFactory.getLogger(MemoryBasedW2V.class);

    private static Map<String, DenseVector> vectors;

    private int dimensions;

    //TODO: Get default term constant from a config file
    private static final String DEFAULT_TERM = "auto";

    public MemoryBasedW2V() {
        this(new W2VDatalessConfigurator().getDefaultConfig());
    }

    public MemoryBasedW2V(ResourceManager config) {
        this(config.getInt(W2VDatalessConfigurator.W2V_DIM));
    }

    public MemoryBasedW2V(int embSize) {
        dimensions = embSize;
    }

    /**
     * Loads up Word2Vec embeddings lazily
     */
    private void loadVectors() {
        if (vectors == null) {
            File inputFile = null;
            try {
                inputFile = getFile();
            } catch (DatastoreException e) {
                e.printStackTrace();
                logger.error("Error retrieving the embedding file from DataStore");
                throw new RuntimeException("Error retrieving the embedding file from DataStore");
            }

            try(BufferedReader bf = new BufferedReader(new FileReader(inputFile))) {
                logger.info("Reading Word2vec Embeddings from " + inputFile.getAbsolutePath());
                vectors = new HashMap<>();

                String line = bf.readLine();
                String[] tokens = line.split(" ");

                // The first line has the following schema --> #Terms #Vector_Dimensions
                int dimNum = Integer.parseInt(tokens[1].trim());

                if (dimNum != dimensions) {
                    bf.close();
                    throw new IllegalStateException("Number of dimensions in the embeddings file (" + dimNum
                            + ") don't match the one in the config file (" + dimensions + ")");
                }

                int count = 0;

                while ((line = bf.readLine()) != null) {
                    line = line.trim();

                    if (line.length() == 0)
                        continue;

                    tokens = line.trim().split(" ", 2);
                    String[] stringVec = tokens[1].split(" ");

                    if (stringVec.length != dimNum) {
                        bf.close();
                        throw new IllegalStateException(
                                "Possible Error in the embeddings file -- number of dimensions("
                                        + dimNum + ") don't match -->" + tokens[1]);
                    }

                    String word = tokens[0].trim();
                    if (word.length() == 0)
                        continue;

                    double[] scores = new double[dimNum];

                    int i = 0;
                    for (String dim : stringVec) {
                        scores[i] = Double.parseDouble(dim);
                        i++;
                    }

                    DenseVector vec = new DenseVector(scores);
                    vectors.put(word, vec);

                    count++;

                    if (count % 100000 == 0)
                        logger.info("#W2V embeddings read: " + count);

                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("IO Error while reading the W2V Embedding File");
                throw new RuntimeException("IO Error while reading the W2V Embedding File");
            } catch (IllegalStateException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * Retrieves the relevant file from the DataStore
     */
    private File getFile() throws DatastoreException {
        ResourceManager rm = new ResourceConfigurator().getDefaultConfig();
        Datastore ds = new Datastore(rm.getString("datastoreEndpoint"));

        File f = ds.getFile("org.cogcomp.dataless", "word2vec.txt", 1.0);
        return f;
    }

    @Override
    public SparseVector<Integer> getTermConceptVectorMap(String term) {
        loadVectors();

        SparseVector<Integer> vector = new SparseVector<>();

        term = processTerm(term);

        if (vectors.containsKey(term))
            vector = DenseVectorOperations.getSparseVector(vectors.get(term));;

        return vector;
    }

    @Override
    public SparseVector<Integer> getDefaultConceptVectorMap() {
        loadVectors();

        return getTermConceptVectorMap(DEFAULT_TERM);
    }

    /**
     * Returns a DenseVector for the SparseVector obtained from getDefaultConceptVectorMap
     */
    public DenseVector getDefaultDenseTermVector() {
        SparseVector<Integer> conceptMap = getDefaultConceptVectorMap();
        DenseVector vec = DenseVector.createDenseVector(conceptMap);

        return vec;
    }

    /**
     * Returns a DenseVector for the SparseVector obtained from getTermConceptVectorMap
     */
    public DenseVector getDenseTermVector(String term) {
        SparseVector<Integer> conceptMap = getTermConceptVectorMap(term);
        DenseVector vec = DenseVector.createDenseVector(conceptMap);

        return vec;
    }

    @Override
    public SparseVector<Integer> getVector(String query) {
        return getConceptVectorBasedOnSegmentation(query);
    }

    /**
     * Returns a DenseVector for the SparseVector obtained from getConceptVectorBasedOnSegmentation
     */
    public DenseVector getDenseVectorBasedOnSegmentation(String query) {
        return getDenseVectorBasedOnSegmentation(query, false);
    }

    /**
     * Overloads getDenseVectorBasedOnSegmentation to provide support for switching on/off
     * term frequency weighting while composing the term vectors
     */
    public DenseVector getDenseVectorBasedOnSegmentation(String query, boolean ignoreTermFreq) {
        SparseVector<Integer> conceptMap =
                getConceptVectorBasedOnSegmentation(query, ignoreTermFreq);
        DenseVector vec = DenseVector.createDenseVector(conceptMap);

        return vec;
    }

    /**
     * Returns a DenseVector for the SparseVector obtained from getConceptVectorBasedOnTermWeights
     */
    public DenseVector getDenseVectorBasedOnTermWeights(HashMap<String, Double> termWeights) {
        SparseVector<Integer> conceptMap = getConceptVectorBasedOnTermWeights(termWeights);
        DenseVector vec = DenseVector.createDenseVector(conceptMap);

        return vec;
    }

    public static void main(String[] args) {
        String sampleFile = "sampleDocument.txt";

        if (args.length > 0) {
            sampleFile = args[0];
        }

        try(BufferedReader br = new BufferedReader(new FileReader(new File(sampleFile)))) {
            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }

            br.close();

            String text = sb.toString().trim();

            MemoryBasedW2V embedding = new MemoryBasedW2V();

            SparseVector<Integer> vector = embedding.getVector(text);
            Map<Integer, Double> vectorMap = vector.getKeyValueMap();

            for (Integer key : vectorMap.keySet())
                System.out.print(key + "," + vectorMap.get(key) + ";");

            System.out.println();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("Test File not found at " + sampleFile);
            throw new RuntimeException("Test File not found at " + sampleFile);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the test file");
            throw new RuntimeException("IO Error while reading the test file");
        }
    }
}
