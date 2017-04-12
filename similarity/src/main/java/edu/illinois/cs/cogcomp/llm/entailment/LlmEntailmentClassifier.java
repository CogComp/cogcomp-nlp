package edu.illinois.cs.cogcomp.llm.entailment;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.lang.Double;


import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.common.ScoredItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.math.ArgMax;
import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;
import edu.illinois.cs.cogcomp.mrcs.common.XmlTags;
import edu.illinois.cs.cogcomp.mrcs.dataReaders.Rte5CorpusHandler;

public class LlmEntailmentClassifier {

	protected 	HashMap< String, Pair< String, String > > 		m_trainEntailmentPairs;
	protected 	HashMap< String, HashMap< String, String > > 	m_trainEntailmentPairInfo;
	protected 	HashMap< String, Double > 						m_thresholds;
	protected 	HashMap< String, Pair< String, String > > 		m_testEntailmentPairs;
	protected 	HashMap< String, HashMap< String, String > > 	m_testEntailmentPairInfo;
	protected 	HashMap< String, Double > 						m_trainLlmScores;
	protected 	HashMap< String, Double >						m_testLlmScores;
	
	protected 	LlmStringComparator								m_llmComparator;
	private 	Double 											m_allTaskThreshold;
	private 	double 											m_allTaskAccuracy;
	private 	double											m_perTaskAvgAccuracy;
	private 	HashMap< String, Double > 						m_taskAccuracy;
	private 	TreeMap< String, String > 						m_taskLlmLabels;
	private 	TreeMap< String, String > 						m_allTaskLlmLabels;
	
	private 	final boolean m_DEBUG = false;

	static Logger	logger	= LoggerFactory.getLogger(LlmEntailmentClassifier.class);

	
/**
 * Example is a convenience class for storing LLM-friendly information about entailment
 *   pairs
 *   
 * @author mssammon
 *
 */
	
	public class Example
	{
		public String[] textTokens;
		public String[] hypTokens;
		public String task;
		public String id;
		public String goldLabel;
		public double llmScore;
		
		public Example() 
		{
			textTokens = null;
			hypTokens = null;
			task = null;
			id = null;
			goldLabel = null;
			llmScore = 0.0;
		}
	}
	
	
	/**
	 * An Entailment classifier that uses LLM as its similarity measure, 
	 *    that learns a threshold to separate positive from negative examples. 
	 *   
	 * @param configFile_
	 */
	
	public LlmEntailmentClassifier( String configFile_ ) throws IOException {
		initialize( new ResourceManager( configFile_ ) );

	}

    /**
     * instantiate with default options
     *
     * @throws IOException
     */
    public LlmEntailmentClassifier() throws IOException {
        initialize( null );
    }

	private void initialize(ResourceManager resourceManager)
    {
		m_trainEntailmentPairs = new HashMap< String, Pair< String, String > >();
		m_testEntailmentPairs = new HashMap< String, Pair< String, String > >();
		m_thresholds = new HashMap< String, Double >();
		m_testEntailmentPairInfo = new HashMap< String, HashMap< String, String> >();
		m_trainEntailmentPairInfo = new HashMap< String, HashMap< String, String > >();
		m_trainLlmScores = new HashMap< String, Double >();
		m_testLlmScores = new HashMap< String, Double >();
		m_allTaskAccuracy = 0.0;
		m_perTaskAvgAccuracy = 0.0;
		m_taskAccuracy = new HashMap< String, Double >();
		m_taskLlmLabels = new TreeMap< String, String >();
		m_allTaskLlmLabels = new TreeMap< String, String >();



		if ( null != resourceManager ) {
			try {
				m_llmComparator = new LlmStringComparator(resourceManager);
			} catch (IOException | IllegalArgumentException e) {

				logger.error("ERROR: LlmEntailmentClassifier: couldn't instantiate LlmComparator.");
				e.printStackTrace();
				System.exit(1);
			}
		}
		else
            try {
                m_llmComparator = new LlmStringComparator();
            } catch (IOException e) {
                logger.error("ERROR: LlmEntailmentClassifier: couldn't instantiate LlmComparator.");
                e.printStackTrace();
                System.exit(1);
            }
    }


	/**
	 *  Using LLM, compute an optimal similarity threshold for separating positive 
	 *   from negative entailment examples using a training corpus, and apply it
	 *   to a test corpus, printing results to STDOUT
	 *   
	 * @param trainFile_
	 * @param trainCorpusId_
	 * @param testFile_
	 * @param testCorpusId_
	 * @throws Exception 
	 */
	
	
	public void runLlmOnTeCorpus( String trainFile_, 
								  String trainCorpusId_,
								  String testFile_,
								  String testCorpusId_
								  ) throws Exception
	{
		processTrainingCorpus( trainFile_, trainCorpusId_ );
		processTestingCorpus( testFile_, testCorpusId_ );
	}
	
	
	/**
	 * Using LLM, compute a threshold using the corpus specified by fileName_ that
	 *   separates positive from negative entailment examples. 
	 *   
	 * @param fileName_ filename of the training corpus.
	 * @param corpusId_ a unique identifier to be used internally to 
	 *    identify members of the training corpus; will be used in result
	 *    output.
	 * @throws Exception 
	 */
	
	public void processTrainingCorpus( String fileName_, String corpusId_ ) throws Exception
	{
		try 
		{
			processCorpus( fileName_, corpusId_, m_trainEntailmentPairs, m_trainEntailmentPairInfo, m_trainLlmScores );
		} 
		catch (IOException e) 
		{
			logger.error( "ERROR: LlmEntailmentClassifier::processTrainingCorpus(): "
						  + "failed to process corpus file " + fileName_ );
			e.printStackTrace();
			System.exit( 1 );
		}
		logger.debug( "processTrainingCorpus: now have " + m_trainLlmScores + " scores." );
		logger.debug( "processTrainingCorpus: now have " + m_trainEntailmentPairInfo.size() + " epInfos." );
		
		findThresholds( m_trainEntailmentPairInfo, m_trainLlmScores );
		computePerformance( m_trainEntailmentPairInfo, m_trainLlmScores, m_thresholds );

		displayPerformance( "Training data (" + fileName_ + ")" );
	}

	
	

	/**
	 * process the test corpus, generating entailment classifications by 
	 *    applying a threshold learned from a previous training step. 
	 *    
	 * @param fileName_ filename of the test corpus.
	 * @param corpusId_ a unique identifier to be used internally to 
	 *    identify members of the test corpus; will be used in result
	 *    output.
	 * @throws Exception 
	 */
	
	public void processTestingCorpus( String fileName_, String corpusId_ ) throws Exception
	{
		try 
		{
			processCorpus( fileName_, corpusId_, m_testEntailmentPairs, m_testEntailmentPairInfo, m_testLlmScores );
		} 
		catch (IOException e) 
		{
			logger.debug( "ERROR: LlmEntailmentClassifier::processTestingCorpus(): "
						  + "failed to process corpus file " + fileName_ );
			e.printStackTrace();
			System.exit( 1 );
		}
		
		logger.debug( "processTestingCorpus: now have scores: " + m_testLlmScores );

		
		computePerformance( m_testEntailmentPairInfo, m_testLlmScores, m_thresholds );

		displayPerformance( fileName_ );	
	}
	
	
	
	
	/**
	 * apply LLM to all examples in the corpus specified in fileName_.
	 * 
	 * @param fileName_
	 * @param corpusId_
	 * @param entailmentPairs_
	 * @param entailmentInfo_
	 * @param llmScores_
	 * @throws Exception 
	 */
	
	public void processCorpus( String fileName_,
								  String corpusId_, 
								  HashMap< String, Pair< String, String> > entailmentPairs_,
								  HashMap< String, HashMap< String, String> > entailmentInfo_,
								  HashMap< String, Double > llmScores_ ) throws Exception
	{
		FileReader inputStream = null;

		XMLReader reader = null;
		InputSource data = null;
		try 
		{
			inputStream = new FileReader( fileName_ );
			data = new InputSource( inputStream );
			reader  = XMLReaderFactory.createXMLReader();
		} 
		catch (SAXException e) 
		{
			logger.error( "failed to instantiate sax parser." + e.getMessage() );
			e.printStackTrace();
			System.exit( 1 );
		}
			
		Rte5CorpusHandler handler = new Rte5CorpusHandler( corpusId_ );
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
			
		
		
		try {
			reader.parse(data);
		} 
		catch (SAXException e) 
		{
			logger.error( "ERROR: LlmEntailmentClassifier: couldn't parse data: " 
					+ e.getMessage() );
			e.printStackTrace();
			System.exit( -1 );
		}
		

		HashMap< String, Pair< String, String > > corpus = handler.getEntailmentCorpus();
		HashMap< String, HashMap< String, String > > info = handler.getEntailmentInfo();

		Iterator< String > it_pair = corpus.keySet().iterator();
			
		while ( it_pair.hasNext() ) {
			String id = it_pair.next();
			entailmentPairs_.put( id, corpus.get( id ) );
			entailmentInfo_.put( id, info.get( id ) );
		}

		logger.debug( "parser found " + entailmentPairs_.size() + " edu.illinois.cs.cogcomp.mrcs.entailment pairs for corpus " + corpusId_ );
			
		Set< String > ids = entailmentPairs_.keySet();
		Iterator<String> it_id = ids.iterator();
			
		while ( it_id.hasNext() ) 
		{
			String id = it_id.next();
			Pair< String, String > ep = entailmentPairs_.get( id );
				
			double score = m_llmComparator.compareStrings( ep.getFirst(), ep.getSecond() );
			llmScores_.put( id, new Double( score ) );
		}

		return;	
	}
	
	
	
	/**
	 * display results for most recently processed corpus.  If only a training corpus
	 *   has been processed, this will output the results obtained when applying
	 *   the learned threshold to the training corpus.
	 *   
	 * @param title_
	 */
	
	public void displayPerformance( String title_ ) 
	{
		System.out.println( "Accuracy for ***" + title_ + " ***:" );
		System.out.println( "SUBSET\t\tTHRESHOLD\t\tACCURACY" );
		
		Iterator< String > it_task = m_taskAccuracy.keySet().iterator();

		
		while ( it_task.hasNext() ) {
			String task = it_task.next();
			
			System.out.println( task + "\t\t" + m_thresholds.get( task ).toString() 
					            + "\t\t" + m_taskAccuracy.get( task ).toString() );
		}
	
		System.out.println( "ALL\t\t" + m_allTaskThreshold + "\t\t" + m_allTaskAccuracy );
		System.out.println( "Weighted Average Per-Task Accuracy:\t-\t\t" + m_perTaskAvgAccuracy );
		System.out.println( "\n\nSingle Threshold Labels: " );
		
		Iterator< String > it_lab = m_taskLlmLabels.keySet().iterator();
		while( it_lab.hasNext() )
		{
			String id = it_lab.next();
			System.out.println( id + "\t" + m_taskLlmLabels.get( id ) );
		}
		
		System.out.println("\n\nTask-Based Threshold Labels: " );
		Iterator< String > it_all = m_allTaskLlmLabels.keySet().iterator();
		TreeMap< Integer, String > orderedLabels = new TreeMap< Integer, String > ();
		
		while( it_all.hasNext() )
		{
			String id = it_all.next();
			Integer idInt = new Integer( id );
			orderedLabels.put( idInt, m_allTaskLlmLabels.get( id ) );
		}

		for ( Integer id: orderedLabels.keySet() )
			System.out.println( id + "\t" + orderedLabels.get( id ) );
		
	}


	private void computePerformance( HashMap<String, HashMap< String, String > > entailmentPairInfo_,
									 HashMap<String, Double> llmScores_,
									 HashMap<String, Double> thresholds_ ) 
	{
		Set< String > infoKeys = entailmentPairInfo_.keySet();
		Iterator< String > it_info = infoKeys.iterator();
		HashMap< String, Double > taskCorrect = new HashMap< String, Double >();
		HashMap< String, Double > taskNumber = new HashMap< String, Double >();
		double allCorrect = 0.0;
		double allNumber = 0.0;
		
		while ( it_info.hasNext() ) 
		{
			String id = it_info.next();
			HashMap< String, String > pairInfo = entailmentPairInfo_.get( id );
			String task = pairInfo.get( XmlTags.TASK );
			logger.debug( "computePerformance: id is " + id + ", task is " + task );
			
			Double threshold = thresholds_.get( task );
			Double currentScore = llmScores_.get( id );
			String goldLabel = pairInfo.get( XmlTags.ENTAIL );
			String llmLabel = XmlTags.NO;
	
			logger.debug( "task " + task + " threshold is " + threshold + ", score is " + currentScore );
			if ( currentScore > threshold ) 
			{
				llmLabel = XmlTags.YES;
			}
			logger.debug( "task llmLabel: " + llmLabel + "; goldLabel: " + goldLabel );
			
			m_taskLlmLabels.put( id, llmLabel );
			
			if ( llmLabel.equals( goldLabel ) )
			{
				Double prevTaskCorrectTotal = taskCorrect.get( task );
				if ( null == prevTaskCorrectTotal )
					prevTaskCorrectTotal = new Double( 0.0 );
				logger.debug( "task " + task + " correct total was: " + prevTaskCorrectTotal );
				
				Double taskCorrectTotal = new Double( prevTaskCorrectTotal.doubleValue() + 1.0 );
				taskCorrect.put( task, taskCorrectTotal );
				logger.debug( "task " + task + " correct total is now: " + taskCorrectTotal );
			}
		// compute all task label
			llmLabel = XmlTags.NO;
			
			if ( currentScore > m_allTaskThreshold ) {
				llmLabel = XmlTags.YES;
			}
			logger.debug( "all llmLabel: " + llmLabel + "; goldLabel: " + goldLabel );
			
			m_allTaskLlmLabels.put( id, llmLabel );
			
			if ( llmLabel.equals( goldLabel ) )
				allCorrect += 1.0;
			
			//update task total
			Double taskPrevTotal = taskNumber.get( task );
			if ( null == taskPrevTotal ) 
				taskPrevTotal = new Double( 0.0 );
			
			Double taskNumberTotal = new Double( taskPrevTotal.doubleValue() + 1.0 );
			taskNumber.put( task, taskNumberTotal );
			allNumber += 1.0;
		}

		m_allTaskAccuracy = allCorrect / allNumber;

		Set< String > taskKeys = thresholds_.keySet();		
		Iterator< String > it_task = taskKeys.iterator();

		m_perTaskAvgAccuracy = 0.0;
		
		while( it_task.hasNext() ) 
		{
			String task = it_task.next();
			Double numCorrect = taskCorrect.get( task ); 
			Double numTask = taskNumber.get( task );
			Double accuracy = new Double( numCorrect.doubleValue() / numTask.doubleValue() );
			m_taskAccuracy.put( task, accuracy );
			logger.debug( "Per task avg. accuracy is: " + m_perTaskAvgAccuracy );
			logger.debug( "Task " + task + " numCorrect: " + numCorrect + "; numTotal: " + numTask + "; acc: " + accuracy );
			m_perTaskAvgAccuracy += (  numTask.doubleValue() * accuracy.doubleValue() );
			logger.debug( "per task avg. accuracy is: " + m_perTaskAvgAccuracy );
		}
		
		m_perTaskAvgAccuracy /= allNumber;

		logger.debug( "per task accuracy = " + m_perTaskAvgAccuracy + ", total number of examples being " + allNumber );
	}


	private void findThresholds( HashMap< String, HashMap< String, String > > entailmentPairInfo_,
								 HashMap< String, Double > llmScores_ ) 
	{
		logger.debug( "findThresholds: epInfo has " + entailmentPairInfo_.size() + " entries." );
		logger.debug( "llmScores_ has " + llmScores_.size() + " entries." );
		
		HashMap< String, HashMap< String, String > > taskToSubset = new HashMap< String, HashMap< String, String > >();
		HashMap< String, String > allLabels = new HashMap< String, String >();
		
		Iterator< String > it_info = entailmentPairInfo_.keySet().iterator();
		
		while ( it_info.hasNext() ) 
		{
			String id = it_info.next();
			HashMap< String, String > info = entailmentPairInfo_.get( id );

			Iterator< String > it_in = info.keySet().iterator();
			
			if ( m_DEBUG ) {
				while ( it_in.hasNext() ) {
					String key = it_in.next();
					logger.debug( "info key: " + key + "; value: " + info.get( key ) );
				}
			}
			
			String goldLabel = info.get( XmlTags.ENTAIL );
			String task = info.get( XmlTags.TASK );
			HashMap< String, String > subset = taskToSubset.get( task );
			
			logger.debug( "id: " + id + "; task: " + task + "; label: " + goldLabel );
			
			if ( null == subset ) {
				subset = new HashMap< String, String >();
				taskToSubset.put( task, subset );
			}
			logger.debug( "adding ex " + id + " to task subset for " + task );
			subset.put( id, goldLabel );
			allLabels.put( id, goldLabel );
		}

		m_allTaskThreshold = findBestThreshold( llmScores_, allLabels );
	
		logger.debug ( "set allTaskThreshold to: " + m_allTaskThreshold );
		
		Iterator< String > it_task = taskToSubset.keySet().iterator();
		
		while ( it_task.hasNext() ) 
		{	
			String task = it_task.next();
			HashMap< String, String > subset = taskToSubset.get( task );
			double threshold = findBestThreshold( llmScores_, subset );
			m_thresholds.put( task, threshold );

			logger.debug( "added task " + task + " threshold " + threshold );
		}
		
		return;
	}
	
	/**
	 * finds threshold that gives best accuracy on TE 2-way labeling for specified set of 
	 *   id-label pairs, given LLM scores for all ids
	 *   
	 * @param scores_ set of all LLM scores for a given corpus
	 * @param goldLabels_ set of id-label pairs drawn from the same corpus
	 * @return threshold giving best performance
	 */
	
	@SuppressWarnings("unchecked")
	protected double findBestThreshold( HashMap< String, Double > scores_, 
										HashMap< String, String > goldLabels_ )
	{
		int numItems = goldLabels_.size();
		
		PriorityQueue< ScoredItem > queue = new PriorityQueue< ScoredItem >( scores_.size() );
		Iterator< String > it_lab = goldLabels_.keySet().iterator();
		
//		TreeMap< String, Integer > labelCounts = new TreeMap< String, Integer >();
		
		// sort id/score pairs by score
		
		while( it_lab.hasNext() ) 
		{
			String id = it_lab.next();
			Double score = scores_.get( id );
			queue.add( new ScoredItem( id, score ) );
		}
		
		String[] idsInScoreOrder = new String[ ( int ) numItems ];
		double[] scoresInOrder = new double[ ( int ) numItems ];
		String[] goldLabelsInScoreOrder = new String[ ( int ) numItems ];
		
		// ArgMax( accuracy, threshold )
		ArgMax< Double, Double > bestAcc = new ArgMax< Double, Double >( 0.0, 0.0 );
		
		// scan items, storing scores and ids in score order; count number of each label type
		
		for ( int i = 0; i < numItems; ++i ) 
		{
			ScoredItem idScore = queue.remove();
			idsInScoreOrder[ i ] = (String) idScore.getItem();
			scoresInOrder[ i ] = idScore.getScore().doubleValue();
			
			if ( m_DEBUG ) 
			{
				logger.debug( "popped queue head: id : " + (String) idScore.getItem() 
						+ "; score: " + idScore.getScore() + ", gold label: " +
						goldLabels_.get( idScore.getItem() ) );
			}
			
			goldLabelsInScoreOrder[ i ] = goldLabels_.get( idScore.getItem() );
			
		}
		
		double lastValue = 0.0;

		if ( m_DEBUG ) {
			logger.debug( "numItems: " + numItems + "; labels: " );
    	
			for ( int i = 0; i < numItems; ++i ) 
			{
				logger.debug( "index " + i + ": "+ goldLabelsInScoreOrder[ i ] );
			}

			logger.debug( "looking for entailment label...");

		}

    	for ( int i = 0; i < numItems; ++i ) 
		{	
    		logger.debug( "item " + i + 
    				": (theoretically, ordered) score is " + scoresInOrder[i] );
    		
		    if ( scoresInOrder[ i ] != lastValue ) 
		    {

		    	double currentThreshold = scoresInOrder[ i ];
		    	int numTrueNeg = countNumInRange( XmlTags.NO, 0, i, goldLabelsInScoreOrder );
		    	int numFalseNeg = i - numTrueNeg; 
		    	int numTruePos = countNumInRange( XmlTags.YES, i, numItems, goldLabelsInScoreOrder ); // all those not POS
		    	int numFalsePos = ( int ) numItems - numTruePos - numTrueNeg - numFalseNeg;
			
		    	double acc = ( double ) ( numTruePos + numTrueNeg ) / ( double ) numItems;
		    	bestAcc.update( acc, currentThreshold );

		    	if ( m_DEBUG ) {
		    		logger.debug( "tp: " + numTruePos );
		    		logger.debug( "fp: " + numFalsePos );
		    		logger.debug( "fn: " + numFalseNeg );
		    		logger.debug( "tn: " + numTrueNeg );
		    		logger.debug( "acc: " + acc );
		    	}
		    }
			lastValue = scoresInOrder[ i ];
		}
    	logger.debug( "best accuracy: " + bestAcc.getMaxValue() 
    			+ "; best threshold: " + bestAcc.getArgmax() );
    	
		return bestAcc.getArgmax().doubleValue();
	}


	protected int countNumInRange( String valueToCount_, int startIndex, int endIndex, String[] valueList_ )
	{
		if ( m_DEBUG ) {
			logger.debug( "LlmEntailmentClassifier::countNumInRange(): "
					+ " startIndex is '" + startIndex + ", endIndex is " + endIndex 
					+ ", value to find is: " + valueToCount_ + ", list size is " 
					+ valueList_.length + ". (values are: " + valueList_ + ")." );
		}
		
		if ( startIndex > valueList_.length ) {
			logger.debug( "ERROR: LlmEntailmentClassifier::countNumInRange(): "
					+ " startIndex is '" + startIndex + ", endIndex is " + endIndex 
					+ ", but list size is " 
					+ valueList_.length + ". (values are: " + valueList_ + ")." );
			System.exit( 1 );
		}
		
		if ( endIndex > valueList_.length ) {
			logger.debug( "ERROR: LlmEntailmentClassifier::countNumInRange(): "
					+ " endIndex is '" + startIndex + ", but list size is " 
					+ valueList_.length + " (values are: " + valueList_ + ")." );
			System.exit( 1 );
		}

		int numFound = 0;
		
		for ( int i = startIndex; i < endIndex; ++i ) 
		{
			if ( valueToCount_.equals( valueList_[ i ] ) ) 
			{
				numFound++;
			}
		}
		
		return numFound; 
	}
	
	
	
}
