package edu.illinois.cs.cogcomp.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class SimConfigurator extends Configurator{

	public static final Property USE_SIMPLE_SCORE = new Property( "useSimpleScore", Configurator.FALSE);
    public static final Property STOPWORD_FILE = new Property( "stopwordFile", "llmStopwords.txt");
    public static final Property WORD_METRIC = new Property( "wordMetric", "wordnet" ) ;
    public static final Property WORD_ENTAILMENT_THRESHOLD = new Property( "wordEntailmentThreshold", "0.001" );
    public static final Property LLM_ENTAILMENT_THRESHOLD = new Property( "llmThreshold", "0.5" );
	public static final Property WORD2VEC= new Property( "word2vec", "");
	public static final Property PARAGRAM= new Property( "paragram", "data/paragram_vectors.txt");
	public static final Property GLOVE= new Property( "glove", "");
	public static final Property PHRASE2VEC= new Property( "phrase2vec", "");
	public static final Property MEMORYBASEDESA= new Property( "memorybasedESA", ""); 
	public static final Property PARAGRAM_DIM= new Property( "paragram_dim", "25");
	public static final Property PAGE_ID_MAPPING = new Property( "pageIDMapping", "");
	public static final Property EMBEDDING_DIM= new Property( "embedding_dim", "200");
	
	@Override
	public ResourceManager getDefaultConfig() {
		Property[] props = { WORD2VEC, PARAGRAM, GLOVE, PHRASE2VEC, MEMORYBASEDESA, PARAGRAM_DIM, PAGE_ID_MAPPING, EMBEDDING_DIM, USE_SIMPLE_SCORE, STOPWORD_FILE, WORD_METRIC, WORD_ENTAILMENT_THRESHOLD, LLM_ENTAILMENT_THRESHOLD};
		return new ResourceManager( generateProperties( props ) );
	}

}
