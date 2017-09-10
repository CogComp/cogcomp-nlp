package edu.illinois.cs.cogcomp.datalessclassification.representation.esa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.ESADatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.representation.AEmbedding;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVectorOperations;

/**
 * Computes ESA Embedding for a query. Loads up all the required DataStructures in memory, and is thus quite fast.
 * 
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class MemoryBasedESA extends AEmbedding<Integer> {
	private static Logger logger = Logger.getLogger(MemoryBasedESA.class);
	
	private static Map<String, SparseVector<Integer>> vectors;
	private static Map<String, Double> wordIDF;
	private static Map<Integer, String> pageIdTitleMapping;
	
	private int dimensions;
	private String embeddingPath;
	private String idConceptMapPath;
	
	public static void main (String[] args) throws Exception {
		String sampleFile = "sampleDocument.txt";
		
		if (args.length > 0) {
			sampleFile = args[0];
		}
		
		BufferedReader br = new BufferedReader(new FileReader(new File(sampleFile)));
		
		StringBuilder sb = new StringBuilder();
		
		String line;
		
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append(" ");
		}
		
		br.close();
		
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
	}
	
	public MemoryBasedESA () {
		this(
				new ESADatalessConfigurator().getDefaultConfig()
			);
	}
	
	public MemoryBasedESA (ResourceManager config) {
		this(
				config.getInt(ESADatalessConfigurator.ESA_DIM.key),
				config.getString(ESADatalessConfigurator.ESA_PATH.key),
				config.getString(ESADatalessConfigurator.ESA_Map_PATH.key)
			);
	}
	
	public MemoryBasedESA (int embSize, String embPath, String conceptMapPath) {
		dimensions = embSize;
		embeddingPath = embPath;
		idConceptMapPath = conceptMapPath;
	}
	
	private void loadVectors () {
		if (vectors == null) {
			File inputFile = new File(embeddingPath);
			
			logger.info("Reading ESA Embeddings from " + embeddingPath);
			
			vectors = new HashMap<String, SparseVector<Integer>>();
			wordIDF = new HashMap<String, Double>();
			
			int count = 0;
			   
			try {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				
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

				        for (int i = 0; i < conceptValues.length; i++) {
				        	String[] tokens = conceptValues[i].split(",");
				        	map.put(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[1]));
				        }
				        
				        SparseVector<Integer> sparseVector = new SparseVector<>(map);
				        vectors.put(word, sparseVector);
					}
					
					count++;
					
					if (count % 100000 == 0) 
						logger.info("#ESA embeddings read: " + count);
				}
				
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadIdTitleMap () {
		if (pageIdTitleMapping == null)
			pageIdTitleMapping = new HashMap<Integer, String>();
		else
			return;
		
		File mappingFile = new File(idConceptMapPath);
		logger.info("Reading mapping file: " + mappingFile.getAbsolutePath());
		
		try {
			BufferedReader bf = new BufferedReader(new FileReader(mappingFile));
			String line;
			
			while ((line = bf.readLine()) != null) {
				if (line.length() == 0) 
					continue;
				
				String[] tokens = line.split("\t");
				
				if (tokens.length != 2)
					continue;
				
				Integer id = Integer.parseInt(tokens[0].trim());
				
				if (pageIdTitleMapping.containsKey(id) == false) {
					pageIdTitleMapping.put(id, tokens[1]);
				}
			}
			
			bf.close();
			logger.info("Done.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("IO Error while reading the mappings file");
			System.exit(-1);
		}
	}
	
	public SparseVector<String> retrieveConceptNames (String query) throws Exception {
		return retrieveConceptNames(query, dimensions);
	}
	
	public SparseVector<String> retrieveConceptNames (String query, int numConcepts) throws Exception {
		SparseVector<Integer> vectorTopic = getVector(query, numConcepts);
		return retrieveConceptNames(vectorTopic);
	}
	
	public SparseVector<String> retrieveConceptNames (SparseVector<Integer> originalVector) throws Exception {
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
	public SparseVector<Integer> getVector (String query) {
		return getVector(query, dimensions);
	}
	
	public SparseVector<Integer> getVector (String query, int numConcepts) {
		return getConceptVectorBasedonSegmentation(query, numConcepts);
	}
	
	public SparseVector<Integer> getConceptVectorBasedonSegmentation (String query) {
		return getConceptVectorBasedonSegmentation(query, dimensions);
	}
	
	public SparseVector<Integer> getConceptVectorBasedonSegmentation (String query, int numConcepts) {
		loadVectors();
		
		Map<String, Double> tfidfMap = new HashMap<>();
		List<String> terms = getTerms(query);
    	
		if (terms.size() == 0)
			return new SparseVector<>();
		
    	for (String term : terms) { 
    		if (tfidfMap.containsKey(term) == false) {
    			tfidfMap.put(term, 1.0);
    		} 
    		else {
    			tfidfMap.put(term, tfidfMap.get(term) + 1);
    		}
    	}
    	
		try {
	    	double vsum = 0;
	    	double norm = 1.0;
	    	
	        for (String strTerm : tfidfMap.keySet()) { 
	        	double tf = tfidfMap.get(strTerm);
	        	
				tf = 1 + Math.log(tf);
				
	            if (wordIDF.containsKey(strTerm)) {
	    			double tfidf = wordIDF.get(strTerm) * tf;
	    			
	    			vsum += tfidf * tfidf;
	    			
	    			tfidfMap.put(strTerm, tfidf);
	            }
	            else
	            	continue;
	        }
	        
	        norm = Math.sqrt(vsum);
	        
	        for (String strTerm : tfidfMap.keySet()) {
	        	double tfidf = tfidfMap.get(strTerm);
	        	tfidfMap.put(strTerm, tfidf / norm);
	        }
	        
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
        	
    	return getConceptVectorBasedonTermWeights(tfidfMap, numConcepts);
	}
	
	public SparseVector<Integer> getConceptVectorBasedonTermWeights (Map<String, Double> termWeights) {
		return getConceptVectorBasedonTermWeights(termWeights, dimensions);
	}
	
	public SparseVector<Integer> getConceptVectorBasedonTermWeights (Map<String, Double> termWeights, int numConcepts) {
		if (termWeights.size() == 0)
			return new SparseVector<>();
		
		List<Map<Integer, Double>> conceptMapList = new ArrayList<Map<Integer, Double>>();
		List<Double> weightList = new ArrayList<Double>();
		List<String> termList = new ArrayList<String>();
		
    	try {
	        for (String strTerm : termWeights.keySet()) {
	        	SparseVector<Integer> sparseVector = getTermConceptVectorMap(strTerm, numConcepts);
	        	
	        	if ((sparseVector.size() > 0) && (termWeights.get(strTerm) > 0)) {
	        		conceptMapList.add(sparseVector.getKeyValueMap());
	    			weightList.add(termWeights.get(strTerm));
	    			termList.add(strTerm);
	        	}
	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		Map<Integer, Double> conceptMap = new HashMap<Integer, Double>();
		
		//TODO: No normalization by the sum of the weights?
		
		conceptMap = SparseVectorOperations.addMultipleMaps(conceptMapList, weightList);
        
		SparseVector<Integer> vec = new SparseVector<Integer>(conceptMap);
        
		SparseVector<Integer> sortedVec = SparseVector.getOrderedSparseVector(vec, SparseVector.<Integer>decreasingScores(), numConcepts);
		
		//Normalization by the length of the document/terms
		sortedVec.scaleAll(1.0 / weightList.size());
		
		return sortedVec;
	}

	//TODO: Get numConcept and default term constants from a config file
	@Override
	public SparseVector<Integer> getDefaultConceptVectorMap () {
		return getDefaultConceptVectorMap(dimensions);
	}
	
	public SparseVector<Integer> getDefaultConceptVectorMap (int numConcepts) {
		loadVectors();
		
		return getTermConceptVectorMap("If", numConcepts);
	}

	@Override
	public SparseVector<Integer> getTermConceptVectorMap (String term) {
		return getTermConceptVectorMap(term, dimensions);
	}

	public SparseVector<Integer> getTermConceptVectorMap (String term, int numConcepts) {
		loadVectors();
		
		SparseVector<Integer> vector = new SparseVector<>();
		
		term = processTerm(term);
		
		if (vectors.containsKey(term))
            vector = vectors.get(term);
		
		SparseVector<Integer> sortedVec = SparseVector.getOrderedSparseVector(vector, SparseVector.<Integer>decreasingScores(), numConcepts);
    	
    	return sortedVec;
	}
	
	public String getConceptFromID (Integer id) {
		if (pageIdTitleMapping == null)
			loadIdTitleMap();
		
		String conceptName = null;
		
		if (pageIdTitleMapping.containsKey(id))
			conceptName = pageIdTitleMapping.get(id).replaceAll(",", "").replaceAll(";", "").replaceAll("\t", "");
		
		return conceptName;
	}
}