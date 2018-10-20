/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.representation.esa;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.ESADatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.representation.AEmbedding;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVectorOperations;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes ESA Embedding for a query.
 * Loads up all the required DataStructures in memory.
 * 
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class MemoryBasedESA extends AEmbedding<Integer> {
    private static Logger logger = LoggerFactory.getLogger(MemoryBasedESA.class);

    private static Map<String, SparseVector<Integer>> vectors;
    private static Map<String, Double> wordIDF;
    private static Map<Integer, String> pageIdTitleMapping;

    //TODO: Get default term constant from a config file
    private static final String DEFAULT_TERM = "if";

    private int dimensions;

    private enum FileType {
        Embedding, Mapping
    }

    public MemoryBasedESA() {
        this(new ESADatalessConfigurator().getDefaultConfig());
    }

    public MemoryBasedESA(ResourceManager config) {
        this(config.getInt(ESADatalessConfigurator.ESA_DIM));
    }

    public MemoryBasedESA(int embSize) {
        dimensions = embSize;
    }

    /**
     * Loads up ESA embeddings lazily
     */
    private void loadVectors() {
        if (vectors == null) {
            File inputFile = null;

            try {
                inputFile = getFile(FileType.Embedding);
            } catch (DatastoreException e) {
                e.printStackTrace();
                logger.error("Error obtaining embeddings file from Datastore");
                throw new RuntimeException("Error obtaining embeddings file from Datastore");
            }

            try(BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                logger.info("Reading ESA Embeddings from " + inputFile.getAbsolutePath());

                vectors = new HashMap<>();
                wordIDF = new HashMap<>();

                int count = 0;

                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.length() > 0) {
                        String[] arr = line.split("\t");

                        String word = arr[0];

                        double idf = Double.parseDouble(arr[1]);
                        wordIDF.put(word, idf);

                        String[] conceptValues = arr[2].split(";");

                        Map<Integer, Double> map = new HashMap<>();

                        for (String conceptValue : conceptValues) {
                            String[] tokens = conceptValue.split(",");
                            map.put(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[1]));
                        }

                        SparseVector<Integer> sparseVector = new SparseVector<>(map);
                        vectors.put(word, sparseVector);
                    }

                    count++;

                    if (count % 100000 == 0)
                        logger.info("#ESA embeddings read: " + count);
                }

                logger.info("Done.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                logger.error("ESA embedding file not found at " + inputFile);
                throw new RuntimeException("ESA embedding file not found at " + inputFile);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("IO Error while reading the ESA Embeddings");
                throw new RuntimeException("IO Error while reading the ESA Embeddings");
            }
        }
    }

    /**
     * Loads up ESA's "ID - Title" mapping lazily
     */
    private void loadIdTitleMap() {
        if (pageIdTitleMapping == null) {
            File mappingFile = null;

            try {
                mappingFile = getFile(FileType.Mapping);
            } catch (DatastoreException e) {
                e.printStackTrace();
                logger.error("Error obtaining Name-mapping file from Datastore");
                throw new RuntimeException("Error obtaining Name-mapping file from Datastore");
            }

            try(BufferedReader bf = new BufferedReader(new FileReader(mappingFile))) {
                logger.info("Reading mapping file: " + mappingFile.getAbsolutePath());

                pageIdTitleMapping = new HashMap<>();

                String line;

                while ((line = bf.readLine()) != null) {
                    if (line.length() == 0)
                        continue;

                    String[] tokens = line.split("\t");

                    if (tokens.length != 2)
                        continue;

                    Integer id = Integer.parseInt(tokens[0].trim());

                    if (!pageIdTitleMapping.containsKey(id)) {
                        pageIdTitleMapping.put(id, tokens[1]);
                    }
                }

                logger.info("Done.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                logger.error("Name-mapping file not found");
                throw new RuntimeException("Name-mapping file not found");
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("IO Error while reading the name-mapping file");
                throw new RuntimeException("IO Error while reading the name-mapping file");
            }
        }
    }

    /**
     * Retrieves the relevant file from the DataStore
     */
    private File getFile(FileType type) throws DatastoreException {
        ResourceManager rm = new ResourceConfigurator().getDefaultConfig();
        Datastore ds = new Datastore(rm.getString("datastoreEndpoint"));

        if (type.equals(FileType.Embedding)) {
            File f = ds.getFile("org.cogcomp.dataless", "memorybasedESA.txt", 1.0);
            return f;
        } else {
            File f = ds.getFile("org.cogcomp.dataless", "pageIDMapping.txt", 1.0);
            return f;
        }
    }

    /**
     * - Returns the vector where the conceptIDs are replaced with conceptNames
     * - Allows for greater readibility of the vector
     */
    public SparseVector<String> retrieveConceptNames(String query) {
        return retrieveConceptNames(query, dimensions);
    }

    /**
     * Overloads retrieveConceptNames to allow support for retrieving a fixed number of dimensions.
     */
    public SparseVector<String> retrieveConceptNames(String query, int numConcepts) {
        SparseVector<Integer> vectorTopic = getVector(query, numConcepts);
        return retrieveConceptNames(vectorTopic);
    }

    /**
     * Converts the ConceptID vector to the corresponding ConceptName vector
     */
    public SparseVector<String> retrieveConceptNames(SparseVector<Integer> originalVector) {
        Map<Integer, Double> map = originalVector.getKeyValueMap();

        SparseVector<String> sparseVector = new SparseVector<>();
        Map<String, Double> outMap = new LinkedHashMap<>();

        for (Integer key : map.keySet()) {
            String concept = getConceptFromID(key);

            if (concept != null)
                outMap.put(concept, map.get(key));
        }

        sparseVector.setVector(outMap);
        return sparseVector;
    }

    @Override
    public SparseVector<Integer> getVector(String query) {
        return getVector(query, dimensions);
    }

    @Override
    public SparseVector<Integer> getVector(String query, int numConcepts) {
        return getConceptVectorBasedOnSegmentation(query, numConcepts);
    }

    @Override
    public SparseVector<Integer> getConceptVectorBasedOnSegmentation(String query) {
        return getConceptVectorBasedOnSegmentation(query, dimensions);
    }

    /**
     * This function overloads getConceptVectorBasedOnSegmentation to provide support for
     * limiting the number of dimensions
     */
    public SparseVector<Integer> getConceptVectorBasedOnSegmentation(String query, int numConcepts) {
        loadVectors();

        Map<String, Double> tfidfMap = new HashMap<>();
        List<String> terms = getTerms(query);

        if (terms.size() == 0)
            return new SparseVector<>();

        for (String term : terms) {
            if (!tfidfMap.containsKey(term)) {
                tfidfMap.put(term, 1.0);
            } else {
                tfidfMap.put(term, tfidfMap.get(term) + 1);
            }
        }

        double vsum = 0;
        double norm;

        for (String strTerm : tfidfMap.keySet()) {
            double tf = tfidfMap.get(strTerm);

            tf = 1 + Math.log(tf);

            if (wordIDF.containsKey(strTerm)) {
                double tfidf = wordIDF.get(strTerm) * tf;

                vsum += tfidf * tfidf;

                tfidfMap.put(strTerm, tfidf);
            }
        }

        norm = Math.sqrt(vsum);

        for (String strTerm : tfidfMap.keySet()) {
            double tfidf = tfidfMap.get(strTerm);
            tfidfMap.put(strTerm, tfidf / norm);
        }

        return getConceptVectorBasedonTermWeights(tfidfMap, numConcepts);
    }

    @Override
    public SparseVector<Integer> getConceptVectorBasedOnTermWeights(Map<String, Double> termWeights) {
        return getConceptVectorBasedonTermWeights(termWeights, dimensions);
    }

    /**
     * This function overloads getConceptVectorBasedOnTermWeights to provide support for
     * limiting the number of dimensions
     */
    public SparseVector<Integer> getConceptVectorBasedonTermWeights(
            Map<String, Double> termWeights, int numConcepts) {
        if (termWeights.size() == 0)
            return new SparseVector<>();

        List<Map<Integer, Double>> conceptMapList = new ArrayList<>();
        List<Double> weightList = new ArrayList<>();

        for (String strTerm : termWeights.keySet()) {
            SparseVector<Integer> sparseVector = getTermConceptVectorMap(strTerm, numConcepts);

            if ((sparseVector.size() > 0) && (termWeights.get(strTerm) > 0)) {
                conceptMapList.add(sparseVector.getKeyValueMap());
                weightList.add(termWeights.get(strTerm));
            }
        }

        Map<Integer, Double> conceptMap;

        // TODO: No normalization by the sum of the weights?

        conceptMap = SparseVectorOperations.addMultipleMaps(conceptMapList, weightList);

        SparseVector<Integer> vec = new SparseVector<>(conceptMap);

        SparseVector<Integer> sortedVec =
                SparseVector.getOrderedSparseVector(vec, SparseVector.decreasingScores(),
                        numConcepts);

        // Normalization by the length of the document/terms
        sortedVec.scaleAll(1.0 / weightList.size());

        return sortedVec;
    }


    @Override
    public SparseVector<Integer> getDefaultConceptVectorMap() {
        return getDefaultConceptVectorMap(dimensions);
    }

    /**
     * This function overloads getDefaultConceptVectorMap to provide support for
     * limiting the number of dimensions
     */
    public SparseVector<Integer> getDefaultConceptVectorMap(int numConcepts) {
        loadVectors();

        return getTermConceptVectorMap(DEFAULT_TERM, numConcepts);
    }

    @Override
    public SparseVector<Integer> getTermConceptVectorMap(String term) {
        return getTermConceptVectorMap(term, dimensions);
    }

    /**
     * This function overloads getTermConceptVectorMap to provide support for
     * limiting the number of dimensions
     */
    public SparseVector<Integer> getTermConceptVectorMap(String term, int numConcepts) {
        loadVectors();

        SparseVector<Integer> vector = new SparseVector<>();

        term = processTerm(term);

        if (vectors.containsKey(term))
            vector = vectors.get(term);

        SparseVector<Integer> sortedVec =
                SparseVector.getOrderedSparseVector(vector,
                        SparseVector.decreasingScores(), numConcepts);

        return sortedVec;
    }

    /**
     * Returns the ConceptName from the ConceptID
     */
    public String getConceptFromID(Integer id) {
        if (pageIdTitleMapping == null)
            loadIdTitleMap();

        String conceptName = null;

        if (pageIdTitleMapping.containsKey(id))
            conceptName =
                    pageIdTitleMapping.get(id).replaceAll(",", "").replaceAll(";", "")
                            .replaceAll("\t", "");

        return conceptName;
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

            String text = sb.toString().trim();

            MemoryBasedESA esa = new MemoryBasedESA();

            SparseVector<Integer> vector = esa.getVector(text);
            Map<Integer, Double> vectorMap = vector.getKeyValueMap();

            for (Integer key : vectorMap.keySet())
                System.out.print(key + "," + vectorMap.get(key) + ";");

            System.out.println();
            System.out.println("Corresponding Concepts:");

            SparseVector<String> vectorTopic = esa.retrieveConceptNames(vector);
            Map<String, Double> vectorTopicMap = vectorTopic.getKeyValueMap();

            for (String key : vectorTopicMap.keySet())
                System.out.print(key + "," + vectorTopicMap.get(key) + ";");

            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the test file");
            throw new RuntimeException("IO Error while reading the test file");
        }
    }
}
