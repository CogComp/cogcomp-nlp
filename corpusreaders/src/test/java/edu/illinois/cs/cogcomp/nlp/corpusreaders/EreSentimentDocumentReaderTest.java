/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for EreDocumentReader that also exercise functionality from AbstractCorpusReader
 *
 * @author mssammon
 */
public class EreSentimentDocumentReaderTest {

    private final static String REF_FILE_ONE = "59f8514f6db132207ba9e5828f73d706.cmp.txt";
    private final static String REF_FILE_TWO = "593cb5020613a4695859130542f7fc94.cmp.txt";

    private static final String RAW_FILE_DIR = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/ereSentimentDocuments";
//    private static final String REF_FILE_DIR = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/ereReferenceDocuments";

    private static String REF_TEXT_ONE;
    private static String REF_TEXT_TWO;

    private static String readReferenceText(String dir, String referenceFile) throws FileNotFoundException {
        return LineIO.slurp( dir + "/" + referenceFile );
    }



    private final static String TEST_DIR = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/ereSentimentDocuments";

    @Test
    public void testReader()
    {
        EreSentimentDocumentReader reader = null;
        try {
            reader = new EreSentimentDocumentReader( TEST_DIR );
        } catch (IOException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        List<Path> files = null;
        try {
            files = reader.getFileListing();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


        assertEquals( 2, files.size() );


        Set< String > names = new TreeSet<>();
        for ( Path file : files )
            names.add( file.getName( file.getNameCount() - 1 ).toString() );

        assertTrue( names.contains( REF_FILE_ONE ) );
        assertTrue( names.contains( REF_FILE_TWO ) );


        Map< String, TextAnnotation> tas = new HashMap<>();
        for ( Path file : files )
        {
            try {
                tas.put( file.getName( file.getNameCount() - 1 ).toString(),
                        reader.getTextAnnotationsFromFile( file ).get(0) );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                fail( e.getMessage());
            }
        }
        System.out.println( "----\n" +  tas.get( REF_FILE_ONE ).getText() + "----\n" );
        System.out.println( "----\n" +  tas.get( REF_FILE_TWO ).getText() + "----\n" );


        String FIRST_ERE_FILE = RAW_FILE_DIR + "/" + REF_FILE_ONE;
        String firstRawText = null;
        try {
            firstRawText = LineIO.slurp( FIRST_ERE_FILE );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }


        Pattern sun = Pattern.compile( "\\w*Sun\\w*" );
        Matcher sunMatcher = sun.matcher( firstRawText );

        Set<IntPair> sunSpans = new HashSet<>();
        while( sunMatcher.find() )
            sunSpans.add( new IntPair(sunMatcher.start(), sunMatcher.end() ) );

        TextAnnotation ta = tas.get( REF_FILE_ONE );

        for ( Constituent c : ta.getView(ViewNames.TOKENS ).getConstituents() )
        {
            if ( c.getSurfaceForm().contains( "Sun") )
            {
                IntPair cCharSpan = new IntPair( c.getStartCharOffset(), c.getEndCharOffset() );
                assertTrue( sunSpans.contains( cCharSpan ) );
                sunSpans.remove( cCharSpan );

                System.err.println( "FOUND OVERLAPPING SPAN: '" + printSpanInContext( firstRawText, cCharSpan ));
            }
        }
        for ( IntPair missedSpan : sunSpans )
            System.err.println( "MISSED SPAN: '" + printSpanInContext( firstRawText, missedSpan ) + "'." );
        assertTrue( sunSpans.isEmpty());

    }

    private String printSpanInContext(String rawText, IntPair span) {
        int start = span.getFirst();
        int contextStart = Math.max(0, start - 15);
        int end = span.getSecond();
        int contextEnd = Math.min( rawText.length(), end + 15 );
        return rawText.substring( contextStart, start ) + "###" +
                rawText.substring( start, end ) + "###" + rawText.substring( end, contextEnd ) + "'." ;

    }

}
