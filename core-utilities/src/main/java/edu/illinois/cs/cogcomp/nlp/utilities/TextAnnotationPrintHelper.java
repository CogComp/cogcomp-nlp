/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * plaintext display for TextAnnotation-related data structures.
 *
 * @author mssammon
 */
public class TextAnnotationPrintHelper {


    public static final String OUTPUT_SEPARATOR = "\n\n-----------------------\n\n";
    public static final String SMALL_SEPARATOR = "\n-----------\n";

    public static String printRelation(Relation c) {
        StringBuilder output =  new StringBuilder();
        output.append( "Relation has type: " );
        output.append( c.getRelationName() );
        output.append( "Attributes: ");
        for ( String attType : c.getAttributeKeys() ) {
            String val = c.getAttribute( attType );
            output.append( "[").append( attType ).append( ", " ).append( val ).append( "], " );
        }
        output.append("\nFirst argument:\n");
        output.append(printConstituent(c.getSource()));
        output.append("\n\nSecond argument:\n");
        output.append(printConstituent(c.getTarget()));
        return output.toString();
    }

    public static String printConstituent( Constituent c )
    {
        StringBuilder output =  new StringBuilder();
        output.append( "Constituent: Surface form: " ).append("'").append( c.getSurfaceForm() ).append( "'" );
//		output.append( "\ns-exp: " );
//		output.append( c_.toSExpression( true ) );
        output.append( "\nLabel: " ).append( c.getLabel() ).append( "; view: " ).append(  c.getViewName() );
        output.append( "\n" ).append( "char offsets: " ).append( c.getStartCharOffset() ).append( ", " );
        output.append( c.getEndCharOffset() ).append( "\ntoken offsets: " ).append( c.getStartSpan() );
        output.append( ", " ).append( c.getEndSpan() );

        output.append( "\nAttributes: ");

        for ( String attType : c.getAttributeKeys() ) {
            String val = c.getAttribute( attType );
            output.append( "[").append( attType ).append( ", " ).append( val ).append( "], " );
        }
        output.append( "\nLabel Scores: " );

        Map<String, Double> labelsToScores = c.getLabelsToScores();
        if ( null != labelsToScores )
            for ( Map.Entry<String, Double> e : labelsToScores.entrySet() )
                output.append(e.getKey()).append(": ").append(e.getValue()).append(", ");
        else
            output.append(c.getLabel()).append(": ").append(c.getConstituentScore());
        output.append( "\n" );

        return output.toString();
    }



    public static String printView(View view) throws IOException
    {
        StringBuilder bldr = new StringBuilder();
        
        bldr.append( "View '" );
        bldr.append( view.getViewName() );
        bldr.append( "':\n" );

        for ( Constituent c: view.getConstituents() ) {
            bldr.append( printConstituent( c ) );
        }

        bldr.append( OUTPUT_SEPARATOR );
        return bldr.toString();
    }


    public static String printTextAnnotation(TextAnnotation ta ) throws IOException
    {
        StringBuilder bldr = new StringBuilder();
        bldr.append( "TextAnnotation for text: " );
        bldr.append( ta.getText() );

        for ( String vName: ta.getAvailableViews() ) {
            bldr.append(printView( ta.getView( vName )));
        }

        return bldr.toString();
    }


    public static String printCoreferenceView(CoreferenceView cView) {

        StringBuilder bldr = new StringBuilder();
        StringBuilder chainBuilder = new StringBuilder();

        Set<Constituent> canons = cView.getCanonicalEntitiesViaRelations();

        for (Constituent c : canons) {
            Set<Constituent> mentions = cView.getCoreferentMentionsViaRelations(c);
            bldr.append("Canonical Mention:\n").append(printConstituent(c));
            chainBuilder.append("** ").append(c.getSurfaceForm()).append(" **: ");
            bldr.append("\nChain:\n");
            int i = 1;
            for (Constituent m : mentions) {
                bldr.append("\n").append(i++).append(": ").append(printConstituent(m));
                chainBuilder.append("[").append(m.getSurfaceForm()).append("] ");
            }
            bldr.append(SMALL_SEPARATOR);
            chainBuilder.append("\n");
        }

        bldr.append("Chain forms: ").append(chainBuilder.toString());

        return bldr.toString();
    }
}
