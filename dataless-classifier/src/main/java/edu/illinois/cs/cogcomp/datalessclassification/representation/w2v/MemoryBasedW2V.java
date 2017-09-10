package edu.illinois.cs.cogcomp.datalessclassification.representation.w2v;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.W2VDatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.representation.AEmbedding;
import edu.illinois.cs.cogcomp.datalessclassification.util.DenseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.DenseVectorOperations;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * Computes Word2Vec Embedding for a query. Loads up all the required DataStructures in memory, and is thus quite fast.
 * 
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class MemoryBasedW2V extends AEmbedding<Integer> {
	private static Logger logger = Logger.getLogger(MemoryBasedW2V.class);

    public Map<String, DenseVector> vectors;
    
    private int dimensions;
    private String embeddingPath;
    
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
		
		MemoryBasedW2V embedding = new MemoryBasedW2V();
		
		SparseVector<Integer> vector = embedding.getVector(text);
		Map<Integer, Double> vectorMap = vector.getKeyValueMap();
		
		for (Integer key : vectorMap.keySet()) 
			System.out.print(key + "," + vectorMap.get(key) + ";");
		
		System.out.println();
	}
	
	public MemoryBasedW2V () {
		this(
				new W2VDatalessConfigurator().getDefaultConfig()
			);
	}
	
	public MemoryBasedW2V (ResourceManager config) {
		this(
				config.getInt(W2VDatalessConfigurator.W2V_DIM.key),
				config.getString(W2VDatalessConfigurator.W2V_PATH.key)
			);
	}
	
	public MemoryBasedW2V (int embSize, String embPath) {
		dimensions = embSize;
		embeddingPath = embPath;
	}
	
	public void loadVectors () {
		if (vectors == null) {
			File inputFile = new File(embeddingPath);
			
			logger.info("Reading Word2vec Embeddings from " + embeddingPath);
			
			vectors = new HashMap<String, DenseVector>();
		    
			try {
				BufferedReader bf = new BufferedReader(new FileReader(inputFile));
				
				String line = bf.readLine();
				String[] tokens = line.split(" ");
				
				//The first line has the following schema --> #Terms #Vector_Dimensions
				int dimNum = Integer.parseInt(tokens[1].trim());
				
				if (dimNum != dimensions) {
					bf.close();
					throw new Exception("Number of dimensions in the embeddings file (" + dimNum + ") don't match the one in the config file (" + dimensions + ")");
				}
				
				int count = 0;
				
				while ((line = bf.readLine()) != null) {
					line = line.trim();
					
					if (line.length() ==  0)
						continue;
					
					tokens = line.trim().split(" ", 2);
					String[] stringVec = tokens[1].split(" ");
					
					if (stringVec.length != dimNum) {
						bf.close();
						throw new Exception("Possible Error in the embeddings file -- number of dimensions(" + dimNum + ") don't match -->" + tokens[1]);
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
				
				bf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public SparseVector<Integer> getTermConceptVectorMap (String term) {
		loadVectors();
		
		SparseVector<Integer> vector = new SparseVector<>();
		
		term = processTerm(term);
		
		if (vectors.containsKey(term))
            vector = DenseVectorOperations.getSparseVector(vectors.get(term));;
		
		return vector;
	}
	
	@Override
	public SparseVector<Integer> getDefaultConceptVectorMap () {
		loadVectors();
		
		return getTermConceptVectorMap("auto");
	}

	public DenseVector getDefaultDenseTermVector () {
		SparseVector<Integer> conceptMap = getDefaultConceptVectorMap();
		DenseVector vec = DenseVector.createDenseVector(conceptMap);
    	
    	return vec;
	}
	
	public DenseVector getDenseTermVector (String term) {
		SparseVector<Integer> conceptMap = getTermConceptVectorMap(term);
		DenseVector vec = DenseVector.createDenseVector(conceptMap);
    	
    	return vec;
	}
	
	@Override
	public SparseVector<Integer> getVector (String query) {
		return getConceptVectorBasedonSegmentation(query);
	}
	
	public DenseVector getDenseVectorBasedonSegmentation (String query) {
		return getDenseVectorBasedonSegmentation(query, false);
	}
	
	/**
	 * Switching off ignoreTermFreq will return a simple averaging over all the terms found in the index
	 * @throws IOException 
	 */
	public DenseVector getDenseVectorBasedonSegmentation (String query, boolean ignoreTermFreq) {
		SparseVector<Integer> conceptMap = getConceptVectorBasedonSegmentation(query, ignoreTermFreq);
		DenseVector vec = DenseVector.createDenseVector(conceptMap);
    	
    	return vec;
	}
	
	public DenseVector getDenseVectorBasedonTermWeights (HashMap<String, Double> termWeights) {
		SparseVector<Integer> conceptMap = getConceptVectorBasedonTermWeights(termWeights);
		DenseVector vec = DenseVector.createDenseVector(conceptMap);
    	
    	return vec;
	}
}