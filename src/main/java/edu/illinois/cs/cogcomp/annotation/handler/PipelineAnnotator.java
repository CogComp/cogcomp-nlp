package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A replacement for Curator's AbstractHandler; provides simple behavior shared by pipeline components.
 *
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
     * @return  an identifier (short name + identifier) with no whitespace
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * @return   a descriptive name, may contain whitespace
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

    /**
     * add a view to a TextAnnotation argument.
     *
     * @param ta    the TextAnnotation to augment
     * @return  the augmented TextAnnotation
     * @throws AnnotatorException   if prerequisite views are not present.
     */
    public TextAnnotation labelTextAnnotation(TextAnnotation ta) throws AnnotatorException
    {
        long startTime = System.currentTimeMillis();
        logger.debug( NAME + ".labelTextAnnotation() (" + getIdentifier() + "): raw text is '" + ta.getText() + "'" );

        if ( !checkRequiredViews( this.getRequiredViews(), ta ) )
        {
            String msg = getIdentifier() + ".getView(): Record is missing a required view (one of " +
                    StringUtils.join(this.getRequiredViews(), ", ") + ").";
            logger.error( msg );
            throw new AnnotatorException( msg );
        }

        View v = getView(ta);

        if ( !ta.hasView( v.getViewName() ) )
            ta.addView( v.getViewName(), v );

        long endTime = System.currentTimeMillis();
        logger.debug( getIdentifier() + ": Tagged input in {}ms", endTime - startTime);

        return ta;
    }

}
