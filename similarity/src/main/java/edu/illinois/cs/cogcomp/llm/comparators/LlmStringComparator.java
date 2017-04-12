package edu.illinois.cs.cogcomp.llm.comparators;

import java.io.IOException;
import java.util.Properties;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.align.WordListFilter;
import edu.illinois.cs.cogcomp.llm.config.LlmConfigurator;
import edu.illinois.cs.cogcomp.mrcs.align.Aligner;
import edu.illinois.cs.cogcomp.mrcs.align.GreedyAlignmentScorer;
import edu.illinois.cs.cogcomp.mrcs.comparators.Comparator;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.Alignment;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.EntailmentResult;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;

public class LlmStringComparator
{

    public static final java.lang.String LLM_THRESHOLD = "llmThreshold";
    public static final String DEFAULT_LLM_THRESHOLD = "0.5";

    private Aligner< String, EntailmentResult> aligner;
	private WordListFilter filter;
	private Comparator< String, EntailmentResult > comparator;
	private GreedyAlignmentScorer< String > scorer;
	IllinoisTokenizer tokenizer;

	public LlmStringComparator( ) throws IOException
	{
		initialize( new ResourceManager( new Properties() ) );
	}

	private void initialize(ResourceManager nonDefaultConfig) throws IOException {

		WordComparator wc = new WordComparator( new LlmConfigurator().getConfig( nonDefaultConfig ) );
	    initialize( nonDefaultConfig, wc );
    }


	public LlmStringComparator( ResourceManager rm_ ) throws IllegalArgumentException, IOException
    {
		initialize( rm_ );
    }

    private void initialize( ResourceManager rm_, Comparator< String, EntailmentResult > comparator ) throws IOException {
        ResourceManager fullRm = new LlmConfigurator().getConfig( rm_ );
        double threshold = fullRm.getDouble( LlmConfigurator.LLM_ENTAILMENT_THRESHOLD.key );
        tokenizer = new IllinoisTokenizer();
        this.comparator = comparator;
        filter = new WordListFilter( fullRm );

        aligner = new Aligner< String, EntailmentResult >( comparator, filter );
        scorer = new GreedyAlignmentScorer< String >( threshold );

    }

	public LlmStringComparator( ResourceManager rm_, Comparator< String, EntailmentResult > comparator_  ) throws IOException
	{
        initialize(rm_, comparator_);
	}

	/**
	 * convenience method with sentences as Strings. Tokenizes using LBJTokenizer
	 *   and runs Aligner, Filter specified at instantiation.
	 * 
	 * @param text_ the Text (typically longer text span)
	 * @param hyp_  the Hypothesis (intent: is this true, given the Text?)
	 * @return EntailmentResult (contains a set of parameters describing the entailment decision)
	 * @throws Exception
	 */
	
	public EntailmentResult determineEntailment( String text_, String hyp_ ) throws Exception
	{
		return determineEntailment( getTokens( text_ ), getTokens( hyp_ ) );
	}
	
	/**
	 * determines best lexical level match of two arrays of String, each representing a tokenized
	 *    sentence.
	 * some tokens may be filtered depending on the WordListFilter params specified at construction.
	 * 
	 * @param textToks_
	 * @param hypToks_
	 * @return
	 * @throws Exception
	 */
	
	public EntailmentResult determineEntailment( String[] textToks_, String[] hypToks_ ) 
	throws Exception
	{
		return scoreAlignment( alignStringArrays( textToks_, hypToks_ ) );
	}

	
	/**
	 * generate the result (score, label, etc.) for the given alignment using 
	 *    GreedyAlignmentScorer
	 * 
	 * @param alignment_
	 * @return
	 */
	
	public EntailmentResult scoreAlignment( Alignment< String > alignment_ )
	{
		return scorer.scoreAlignment( alignment_ );
	}

	
	/**
	 * convenience method to generate the alignment for two sentences specified as Strings.
	 * 
	 * @param text_
	 * @param hyp_
	 * @return
	 * @throws Exception
	 */
	
	
	public Alignment< String > alignSentences(String text_, String hyp_ ) throws Exception
	{
		return ( alignStringArrays( getTokens( text_ ), getTokens( hyp_ ) ) );
	}
	
	
	
	private String[] getTokens( String sentence_ )
	{
		Pair< String[], IntPair[] > lbjTokens = tokenizer.tokenizeSentence( sentence_ );

		return lbjTokens.getFirst();
	}
	
	

	/**
	 * generate a lexical alignment using the comparator specified at construction, and the 
	 *   
	 * 
	 * @param textTokens_
	 * @param hypTokens_
	 * @return
	 * @throws Exception
	 */

	public Alignment< String > alignStringArrays( String[] textTokens_,
			String[] hypTokens_ ) throws Exception
	{
		return aligner.align( textTokens_, hypTokens_ );
	}

	/**
	 * convenience method to generate a scalar score for two input sentences passed as Strings..
	 * 
	 * @param source_
	 * @param target_
	 * @return similarity score (double) between 0 and 1.
	 * @throws Exception
	 */

	public double compareStrings( String source_, String target_ )
			throws Exception
	{
		
		return (double) this.determineEntailment( source_, target_ ).getScore();
	}
}
	
	