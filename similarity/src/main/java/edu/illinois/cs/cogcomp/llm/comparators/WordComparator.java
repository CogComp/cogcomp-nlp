package edu.illinois.cs.cogcomp.llm.comparators;

import java.io.IOException;

import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import edu.illinois.cs.cogcomp.sim.Metric;
import edu.illinois.cs.cogcomp.sim.MetricResponse;

import edu.illinois.cs.cogcomp.sim.WordSim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.mrcs.comparators.Comparator;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.EntailmentResult;

/**
 * 
 * @author mssammon
 *
 */
public class WordComparator implements Comparator< String, EntailmentResult >
{
    private boolean computeSimpleScore;
	private double entailmentThreshold;
	protected boolean defaultUpwardMonotone = true;
	
	private Metric wordSim;
  
	
	private Logger logger = LoggerFactory.getLogger( WordComparator.class );
    //private LlmConstants.WordMetric metric;


    public WordComparator( String configFile_ ) throws IOException
	{
		ResourceManager rm = new ResourceManager( configFile_ );
		configure( rm );
	}
	
	public WordComparator( ResourceManager rm_ ) throws IllegalArgumentException, IOException
	{
		configure( rm_ );
	}
	
	/**
	 * reads parameters from configuration file named by m_propertiesFile
	 * loads stopwords, sets xmlrpc client if appropriate
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
		protected void configure( ResourceManager rm_ ) throws IllegalArgumentException, IOException 
		{
//			boolean useWordSim = rm_.getBoolean(Constants.USE_WORDSIM);  // if false, use WNSim (older package)
			entailmentThreshold = rm_.getDouble( SimConfigurator.WORD_ENTAILMENT_THRESHOLD.key );
			computeSimpleScore = rm_.getBoolean( SimConfigurator.USE_SIMPLE_SCORE.key );

            String wordComparator = rm_.getString(SimConfigurator.WORD_METRIC);

            wordSim = new WordSim(rm_,wordComparator);


		}

	
	@Override
	public EntailmentResult compare( String specific_, String general_ )
	{
		String genTok = StringCleanup.normalizeToLatin1(general_);
		String specTok = StringCleanup.normalizeToLatin1( specific_ );

		double score = 0.0;
		String reason = "default reason";
		String source = WordComparator.class.getSimpleName();
		boolean isEntailed = false;
		boolean isPositivePolarity = true;
		
		if ( specTok.equalsIgnoreCase( genTok ) ){
			score = 1.0;
			reason = "Identity";
		}else{
            MetricResponse result = wordSim.compare(specific_, general_);
            
            score = result.score;
			reason = result.reason;

			isEntailed = (Math.abs(score) > entailmentThreshold);
			isPositivePolarity = (score >= 0);

			if (computeSimpleScore) {
				if (isEntailed) {
					score = 1.0;
				}
			}

		}
		
		return new EntailmentResult( source, (float) score, reason, isEntailed, isPositivePolarity, defaultUpwardMonotone, null );
	}

}
