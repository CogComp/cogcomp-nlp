package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A replacement for Curator's AbstractHandler
 * Created by mssammon on 8/24/15.
 */
abstract public class PipelineAnnotator implements Annotator
{
    private static final String NAME = PipelineAnnotator.class.getCanonicalName();

    private final String fullName;
    private final String version;
    private final String shortName;
    private final String identifier;

    private Logger logger = LoggerFactory.getLogger( PipelineAnnotator.class );

    public PipelineAnnotator(String fullName, String version, String shortName) {

        this.fullName = fullName;
        this.version = version;
        this.shortName = shortName;
        this.identifier = shortName + "-" + version;
    }

    public String getAnnotatorName()
    {
        return shortName;
    }

    public String getVersion()
    {
        return version;
    }

    /**
     * return an identifier (short name + identifier) with no whitespace
     * @return
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * return a descriptive name, may contain whitespace
     * @return
     */
    public String getFullName()
    {
        return fullName;
    }

    public static boolean checkRequiredViews( String[] requiredViews, TextAnnotation ta )
    {
        for ( String rv : requiredViews )
        {
            if ( !ta.hasView( rv ) )
                return false;
        }
        return true;
    }

    public TextAnnotation labelRecord(TextAnnotation record) throws AnnotatorException
    {
        long startTime = System.currentTimeMillis();
        logger.debug( NAME + ".labelRecord() (" + getIdentifier() + "): raw text is '" + record.getText() + "'" );

        if ( !checkRequiredViews( this.getRequiredViews(), record ) )
        {
            String msg = getIdentifier() + ".getView(): Record is missing a required view (one of " +
                    StringUtils.join(this.getRequiredViews(), ", ") + ").";
            logger.error( msg );
            throw new AnnotatorException( msg );
        }

        View v = getView(record);

        if ( !record.hasView( v.getViewName() ) )
            record.addView( v.getViewName(), v );

        long endTime = System.currentTimeMillis();
        logger.debug( getIdentifier() + ": Tagged input in {}ms", endTime - startTime);

        return record;
    }

}
