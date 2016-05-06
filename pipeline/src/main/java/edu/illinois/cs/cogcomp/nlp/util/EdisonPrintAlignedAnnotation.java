package edu.illinois.cs.cogcomp.nlp.util;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;

import java.io.PrintStream;

/**
 * Utility class to print output in a way that helps debugging misaligned views (currently, parse trees are
 *    the problem)
 * Created by mssammon on 6/16/15.
 */

public class EdisonPrintAlignedAnnotation
{
    public static void printViewWithTargetAlignmentTreeView( TextAnnotation ta, TreeView view, PrintStream out )
    {
        int index = 0;

        for ( int sentenceId = 0; sentenceId < ta.getNumberOfSentences(); ++sentenceId )
        {
            printSentenceWithAlignedTree( ta, view, out, sentenceId );
        }
    }

    private static void printSentenceWithAlignedTree(TextAnnotation ta, TreeView view, PrintStream out, int sentenceId) {

        Sentence s = ta.getSentence( sentenceId );
        int start = s.getStartSpan();
        int end = s.getEndSpan();

        out.println( "Index\tToken\tTargetTok\tTargetLabel" );
        for ( int i = start; i < end; ++i )
        {
            String tok = ta.getToken( i );

        }

    }
}
