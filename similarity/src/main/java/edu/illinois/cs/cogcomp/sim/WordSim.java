package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;

import edu.cmu.lti.ws4j.WS4J;
import edu.illinois.cs.cogcomp.wsim.embedding.EmbeddingConstant;
import edu.illinois.cs.cogcomp.wsim.embedding.TruncatedWord2vec;
import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;
import edu.illinois.cs.cogcomp.wsim.esa.ResourcesConfig;
import edu.illinois.cs.cogcomp.wsim.wordnet.WNSim;
import edu.illinois.cs.cogcomp.wsim.embedding.Embedding;


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
		paragram = new Embedding(config.paragram, config.paragram_dimension);
		//word2vec = new Embedding(config.word2vec, config.embedding_dimension);
		//truncated = new TruncatedWord2vec(config.word2vec, config.embedding_dimension,8);
		//phrase2vec=new Embedding(config.phrase2vec, config.embedding_dimension);
		//esa=new MemoryBasedESA(config);
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
	
	
	public WordSim(String method) {
		this.method=method;
		ResourcesConfig config=new ResourcesConfig();

		if(method.equals(EmbeddingConstant.word2vec)) {
			word2vec = new Embedding(config.word2vec, config.embedding_dimension);
		}
		else if(method.equals(EmbeddingConstant.paragram)) {
			paragram = new Embedding(config.paragram, config.paragram_dimension);
		}
		else if(method.equals(EmbeddingConstant.truncated)) {
			truncated = new TruncatedWord2vec(config.word2vec, config.embedding_dimension,8);
		}
		else if(method.equals(EmbeddingConstant.phrase2vec)) {
			phrase2vec=new Embedding(config.phrase2vec, config.embedding_dimension);
		}
		else if(method.equals(EmbeddingConstant.esa)) {
			esa=new MemoryBasedESA(config);
		}
		else if(method.equals(EmbeddingConstant.wordnet)) {
			try {
				wnsim = new WNSim();
			} catch (IOException e) {
			}
		} else
			throw new IllegalArgumentException("Requires an legal word comparison metric");
 
	}
	

}
