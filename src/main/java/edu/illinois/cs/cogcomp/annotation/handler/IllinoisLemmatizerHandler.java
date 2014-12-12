package edu.illinois.cs.cogcomp.annotation.handler;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.didion.jwnl.JWNLException;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.AugmentedLemmatizer;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.thrift.labeler.Labeler;

/**
 * wraps Illinois Lemmatizer for use as Curator Component. 
 * The Lemmatizer adds 4 views: WNPLUS, WORDNET, PORTER, and KP. 
 * 
 * The config file for Curator will contain a MultiLabeler entry for this 
 *    annotator, and the field names should reflect these lemmatizer types 
 *    and respect their order.
 *    
 * @author mssammon
 *
 */

public class IllinoisLemmatizerHandler extends IllinoisAbstractHandler
        implements Labeler.Iface
{
    private static final String NAME = IllinoisLemmatizerHandler.class.getCanonicalName();
    private static final String PUBLIC_NAME = "IllinoisLemmatizer";
    private static final String VERSION = "0.2";
    
    private AugmentedLemmatizer lemmatizer;
//    private List< LemmaType > lemmaTypes;
    
    private final Logger logger = LoggerFactory.getLogger( IllinoisLemmatizerHandler.class );
	private String corpusId;
	private String textId;
    
    public IllinoisLemmatizerHandler( String configFile_ ) throws FileNotFoundException, JWNLException, IOException
    {
        this( new ResourceManager( configFile_ ) );
    }
    
    public IllinoisLemmatizerHandler( ResourceManager rm_ ) throws JWNLException, IllegalArgumentException, IOException
    {
        super( PUBLIC_NAME, VERSION, PUBLIC_NAME + "-" + VERSION );

//        lemmaTypes = new LinkedList< LemmaType >();
//        
//        lemmaTypes.add( LemmaType.WNPLUS );
//        lemmaTypes.add( LemmaType.WORDNET );
//        lemmaTypes.add( LemmaType.PORTER );
//        lemmaTypes.add( LemmaType.KP );

        AugmentedLemmatizer.init( rm_ );
        
    }
    
    /**
     * Returns a Labeling containing spans labeled with word lemmas based on 
     *   a re-implementation of the Edison WNPlus lemmatizer (wordnet lemmas
     *   plus verb and deverbal noun lemmas).
     */
    
    public Labeling labelRecord( Record record ) 
        throws AnnotationFailedException, TException
    {
        Labeling lemmaView = null;
        
        String errMsg = null;
        
        try
        {
	        lemmaView = AugmentedLemmatizer.createLemmaRecordView( record, this.corpusId, this.textId );
//			lemmaViews = new LinkedList< Labeling >();
//			lemmaViews.add( lemmaView );
        }
        catch ( IOException e )
        {
	        e.printStackTrace();
	        errMsg = e.getMessage();
        }

        if ( null != errMsg )
        {
        	throw new AnnotationFailedException( "ERROR: " + NAME + ".labelRecord(): " +
        			"caught exception while requesting AugmentedLemmatizer view: " + 
        			errMsg );
        }
        
//        record.labelViews.put( AugmentedLemmatizer.LEMMA_VIEW, lemmaView );
        return lemmaView;
    }

}
