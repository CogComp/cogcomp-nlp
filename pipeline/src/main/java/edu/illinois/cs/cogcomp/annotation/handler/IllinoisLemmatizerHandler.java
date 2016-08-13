package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * wraps Illinois Lemmatizer in an illinois-core-utilities Annotator,
 *    for use as a Pipeline component.
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

    private final Logger logger = LoggerFactory.getLogger( IllinoisLemmatizerHandler.class );

    public IllinoisLemmatizerHandler( String configFile_ ) throws IOException
    {
        this( new ResourceManager( configFile_ ) );
    }
    
    public IllinoisLemmatizerHandler( ResourceManager rm_ ) throws IllegalArgumentException, IOException
    {
        super( PUBLIC_NAME, VERSION, PUBLIC_NAME + "-" + VERSION, ViewNames.LEMMA, new String[]{ ViewNames.POS } );

        lemmatizer = new IllinoisLemmatizer( rm_ );
    }



    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException {
        lemmatizer.addView( textAnnotation );
    }
}
