package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TextAnnotationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IllinoisPreprocessorTest
{

	private static final String CONFIG = "src/test/resources/testConfig.txt";

	private static IllinoisPreprocessor prep;

    @BeforeClass
    public static void setUpBeforeClass()
    {

        ResourceManager rm = null;
        try
        {
            rm = new ResourceManager( CONFIG );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
        try {
            TextAnnotationBuilder textAnnotationBuilder = new TextAnnotationBuilder(new IllinoisTokenizer());
            prep = new IllinoisPreprocessor( rm, textAnnotationBuilder);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

	@Before
	public void setUp() throws Exception
	{}

	@After
	public void tearDown() throws Exception
	{
	}

    //TODO There is currently no way to test a WS version of the preprocessor

	/**
	 * tests whether a two-sentence text span is processed to create the expected views: tokens, sentences, 
	 *    pos tags, and stanford parse/dependency trees.  It verifies that the views agree by checking whether
	 *    there are the same number of tokens, pos tags, and parse tree leaves. Dependency parse skips prepositions
	 *    and possessive markers -- and maybe other tokens too -- so only bounds are checked.
	 * This method uses pre-tokenized inputs and tests the Preprocessor's whitespace mode.  
	 */
	@Test
	public void testProcessText()
	{
        String text = "The CIA leaders thought Marin's father had left the building already. (But the humans were wrong.)" ;

		TextAnnotation ta = null;
		try {
			ta = prep.processText( text );
		} catch (AnnotatorException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		}

        checkTextAnnotation(ta);

		boolean isPosOk = false;
		//        boolean isDepOk = false;
		//        boolean isParseOk = false;

		if ( ta.hasView( ViewNames.POS ) && ta.hasView(ViewNames.TOKENS)
//				                &&
//				                ta.hasView( ViewNames.DEPENDENCY_STANFORD )
				)
		{
			View poss = ta.getView(ViewNames.POS);
			View toks = ta.getView(ViewNames.TOKENS);

			List< Constituent > tokens = toks.getConstituents();
			List< Constituent >  posTags = poss.getConstituents();


			if ( ( tokens.size() != posTags.size() ) ||
					tokens.size() != 21 )
			{
				System.err.println( "ERROR: tokens, pos have different/incorrect sizes." );
			}
			else 
				isPosOk = true;

            System.out.println(poss);
            System.out.println();

			//            Forest parse = ta.getParseViews().get( ViewNames.stanfordParse );
			//            
			//            int numLeaves = 0;
			//            
			//            if ( parse.getTrees().size() > 0 )
			//                for ( Tree parseTree : parse.getTrees() )
			//                    numLeaves += CuratorDataUtils.printTree( System.out, parseTree, text );
			//            
			//            if ( numLeaves == tokens.size() )
			//                isParseOk = true;
			//
			//            System.out.println( "Found " + numLeaves + " parse leaves, " + tokens.size() + " tokens." );
			//
			//            Forest dep = ta.getParseViews().get( ViewNames.stanfordDep );
			//            
			//            int numDepNodes = 0;
			//            
			//            if ( dep.getTrees().size() > 0 )
			//                for ( Tree depTree : dep.getTrees() )
			//                    numDepNodes += CuratorDataUtils.printTree( System.out, depTree, text );
			//
			//            System.out.println( "found " + numDepNodes + " dependency nodes, "  + tokens.size() + " tokens." );
			//    
			//            if ( numDepNodes <= tokens.size() && numDepNodes > 0 )
			//                isDepOk = true;
			//
			//            
		}        
		assertTrue( isPosOk );
		//        assertTrue( isParseOk );
		//        assertTrue( isDepOk );
	}

	private boolean checkTextAnnotation(TextAnnotation ta)
	{
		boolean isTokViewOk = false;
		if ( ta.hasView(ViewNames.TOKENS) )
		{
			View toks = ta.getView(ViewNames.TOKENS);
			System.err.println( "## tokens view has " + toks.getNumberOfConstituents() + " tokens." );

			if ( toks.getNumberOfConstituents() == 21 )
				isTokViewOk = true;
		}


		boolean isSentViewOk = false;

		if ( ta.hasView(ViewNames.SENTENCE) )
		{
			View sents = ta.getView(ViewNames.SENTENCE);

			System.err.println( "## sentences view has " + sents.getNumberOfConstituents() + " sentences." );

			if ( sents.getNumberOfConstituents() == 2 )
				isSentViewOk = true;
		}

		return ( isTokViewOk && isSentViewOk );
	}


}
