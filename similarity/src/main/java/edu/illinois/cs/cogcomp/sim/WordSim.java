package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;

import edu.cmu.lti.ws4j.WS4J;
import edu.illinois.cs.cogcomp.wsim.embedding.EmbeddingConstant;
import edu.illinois.cs.cogcomp.wsim.embedding.TruncatedWord2vec;
import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;
import edu.illinois.cs.cogcomp.wsim.esa.ResourcesConfig;
import edu.illinois.cs.cogcomp.wsim.embedding.Embedding;
import edu.illinois.cs.cogcomp.wsim.wn.WNSim;


public class WordSim implements Metric<String> {
	WNSim wnsim;
	Embedding paragram;
	Embedding word2vec;
	TruncatedWord2vec truncated;
	Embedding phrase2vec;
	MemoryBasedESA esa;
	String method;
	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException {
		return compare(arg1,arg2, method);
	}
	
	
	public WordSim(){
		ResourcesConfig config=new ResourcesConfig();
		Embedding paragram = new Embedding(config.paragram, config.dimension);
		Embedding word2vec = new Embedding(config.word2vec, config.dimension);
		Embedding truncated =new Embedding(config.truncated, config.dimension);
		Embedding phrase2vec=new Embedding(config.phrase2vec, config.dimension);
		MemoryBasedESA esa=new MemoryBasedESA(config);
		try {
			wnsim = new WNSim();
		} catch (IOException e) {
		}
	}
	
	public MetricResponse compare(String small, String big, String method) {
		double score = 0;

		if(method.equals(EmbeddingConstant.word2vec)) {
			score = word2vec.simScore(small, big);
		}
		else if(method.equals(EmbeddingConstant.paragram)) {
			score = paragram.simScore(small, big);
		}
		else if(method.equals(EmbeddingConstant.truncated)) {
			score = truncated.simScore(small, big);
		}
		else if(method.equals(EmbeddingConstant.phrase2vec)) {
			score = phrase2vec.simScore(small, big);
		}
		else if(method.equals(EmbeddingConstant.esa)) {
			score = esa.cosin(small, big);
		}
		else if(method.equals(EmbeddingConstant.wordnet)) {

			return wnsim.compare(small, big);
		}
		else
			throw new IllegalArgumentException("Requires an argument");

		return new MetricResponse(score,  method);
	}

}
