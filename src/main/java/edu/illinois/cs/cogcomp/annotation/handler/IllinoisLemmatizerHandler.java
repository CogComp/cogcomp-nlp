package edu.illinois.cs.cogcomp.annotation.handler;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;

import edu.illinois.cs.cogcomp.transitional.CuratorDataStructureInterface;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
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

public class IllinoisLemmatizerHandler extends PipelineAnnotator
{
    private static final String NAME = IllinoisLemmatizerHandler.class.getCanonicalName();
    private static final String PUBLIC_NAME = "IllinoisLemmatizer";
    private static final String VERSION = "0.3";
    private static final String[] REQUIRED_VIEWS = {ViewNames.POS};

    private IllinoisLemmatizer lemmatizer;
//    private List< LemmaType > lemmaTypes;
    
    private final Logger logger = LoggerFactory.getLogger( IllinoisLemmatizerHandler.class );
	private String corpusId;
	private String textId;
    
    public IllinoisLemmatizerHandler( String configFile_ ) throws FileNotFoundException, IOException
    {
        this( new ResourceManager( configFile_ ) );
    }
    
    public IllinoisLemmatizerHandler( ResourceManager rm_ ) throws IllegalArgumentException, IOException
    {
        super( PUBLIC_NAME, VERSION, PUBLIC_NAME + "-" + VERSION );

//        lemmaTypes = new LinkedList< LemmaType >();
//        
//        lemmaTypes.add( LemmaType.WNPLUS );
//        lemmaTypes.add( LemmaType.WORDNET );
//        lemmaTypes.add( LemmaType.PORTER );
//        lemmaTypes.add( LemmaType.KP );

//        AugmentedLemmatizer.init( rm_ );
        lemmatizer = new IllinoisLemmatizer( rm_ );
    }


    @Override
    public String getViewName() {
        return ViewNames.LEMMA;
    }

    @Override
    public View getView(TextAnnotation textAnnotation) throws AnnotatorException {
        return lemmatizer.getView( textAnnotation );
    }

    @Override
    public String[] getRequiredViews() {
        return REQUIRED_VIEWS;
    }
}
