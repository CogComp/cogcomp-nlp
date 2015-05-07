package edu.illinois.cs.cogcomp.nlp.pipeline;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorViewNames;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.common.AdditionalViewNames;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Forest;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.util.CuratorDataUtils;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IllinoisPreprocessorTest
{

	private static final String CONFIG = "src/test/resources/testConfig.txt";

	private IllinoisPreprocessor prep;

	@Before
	public void setUp() throws Exception
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
		prep = new IllinoisPreprocessor( rm );
	}

	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * tests whether a two-sentence text span is processed to create the expected views: tokens, sentences, 
	 *    pos tags, ner and stanford parse/dependency trees.  It verifies that the views agree by checking whether
	 *    there are the same number of tokens, pos tags, and parse tree leaves. Dependency parse skips prepositions
	 *    and possessive markers -- and maybe other tokens too -- so only bounds are checked.  
	 */
	@Test
	public void testProcessText()
	{

		String text = "The CIA leaders thought Marin's father had left the building already. (But the humans were wrong.)" ;

		Record rec = null;
		try
		{
			rec = prep.processText( text, false );
		}
		catch ( AnnotationFailedException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}
		catch ( TException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}
		boolean isPosOk = false;
		boolean isChunkOk = false;
		boolean isLemmaOk = false;
		boolean isNerOk = false;
		boolean isNerExtOk = false;
		boolean isParseOk = false;
        boolean isDepOk = false;
//		Set< String > views = rec.getLabelViews().keySet();

//		if ( views.contains( CuratorViewNames.pos ) && views.contains( CuratorViewNames.tokens ) )
//		{
			Labeling lab = rec.getLabelViews().get( CuratorViewNames.pos );
			Labeling toks = rec.getLabelViews().get( CuratorViewNames.tokens );

			List< Span > tokens = toks.getLabels();
			List< Span >  posTags = lab.getLabels();

			if ( ( tokens.size() != posTags.size() ) || tokens.size() != 21 )
			{
				System.err.println( "ERROR: tokens, pos have different/incorrect sizes." );
			}
			else 
				isPosOk = true;

			assertTrue( isPosOk );

			CuratorDataUtils.printLabeling( System.out, lab, text );
			System.out.println();
//		}   

//		if ( views.contains( CuratorViewNames.chunk ) )
//		{
			Labeling chunk = rec.getLabelViews().get( CuratorViewNames.chunk );
			List< Span > chunkTags = chunk.getLabels();

			if ( chunkTags.size() > 0 )
				isChunkOk = true;

			assertTrue( isChunkOk );

			CuratorDataUtils.printLabeling( System.out, chunk, text );
			System.out.println();
//		}
//
//		if ( views.contains( AdditionalViewNames.ccgLemma ) && views.contains( CuratorViewNames.tokens ) )
//		{
//			Labeling toks = rec.getLabelViews().get( CuratorViewNames.tokens );
			Labeling lemma = rec.getLabelViews().get( CuratorViewNames.lemma );

//			List< Span > tokens = toks.getLabels();

			int numSpans = lemma.getLabelsSize();

			if ( numSpans == tokens.size() )
				isLemmaOk = true;

			assertTrue( isLemmaOk );

			System.out.println( "found " + numSpans + " lemma nodes, "  + tokens.size() + " tokens." );

			System.out.println( CuratorViewNames.lemma + " VIEW: " );
			CuratorDataUtils.printLabeling( System.out, lemma, text );

//		}  

//		if ( views.contains( CuratorViewNames.ner ) )
//		{
			Labeling ner = rec.getLabelViews().get( CuratorViewNames.ner );

			List< Span > nerTags = ner.getLabels();

			if ( nerTags.size() > 0 )
				isNerOk = true;

			assertTrue( isNerOk );

			CuratorDataUtils.printLabeling( System.out, ner, text );
			System.out.println();


		Labeling nerExt = rec.getLabelViews().get( AdditionalViewNames.nerExt );
		List< Span > nerExtTags = nerExt.getLabels();

		if ( nerExtTags.size() > 0 )
			isNerExtOk = true;

		assertTrue( isNerExtOk );

		CuratorDataUtils.printLabeling( System.out, ner, text );
		System.out.println();


//		}
//		
//		if ( views.contains( CuratorViewNames.stanfordParse ) )
//		{
			Forest parseView = rec.getParseViews().get( CuratorViewNames.stanfordParse );
            Forest depView = rec.getParseViews().get( CuratorViewNames.stanfordDep );
			Labeling sentView = rec.getLabelViews().get( CuratorViewNames.sentences );
			
			// Check that we have as many trees as sentences
			isParseOk = parseView.getTreesSize() == sentView.getLabelsSize();
//			isDepOk = depView.getTreesSize() == sentView.getLabelsSize();
			assertTrue( isParseOk );
//		}

		TextAnnotation ta = null;

		try {
			ta = prep.processTextToTextAnnotation( "test", "test", text, false);
		} catch (AnnotationFailedException e) {
			e.printStackTrace();
			fail( "Exception creating TextAnnotation for text '" + text + "'." );
		} catch (TException e) {
			e.printStackTrace();
			fail( "Exception creating TextAnnotation for text '" + text + "'." );
		}

		List< Constituent > lemmaConstituents = ta.getView( CuratorViewNames.lemma ).getConstituents();

		for ( Constituent c: lemmaConstituents )
		{
			System.out.println( "word: " + ta.getToken( c.getStartSpan() ) + ", lemma: " + c.getLabel() );
		}
	}



	/**
	 * tests whether a two-sentence text span is processed to create the expected views: tokens, sentences, 
	 *    pos tags, and stanford parse/dependency trees.  It verifies that the views agree by checking whether
	 *    there are the same number of tokens, pos tags, and parse tree leaves. Dependency parse skips prepositions
	 *    and possessive markers -- and maybe other tokens too -- so only bounds are checked.
	 * This method uses pre-tokenized inputs and tests the Preprocessor's whitespace mode.  
	 */
	@Test
	public void testProcessWsText()
	{

		//        String text = "The CIA thought Marin 's father had left the building already. But they were wrong . " ;
		String text = "The CIA leaders thought Marin 's father had left the building already ." + 
				System.getProperty( "line.separator" ) + "( But the humans were wrong . )" ;

		Record rec = null;
		try
		{
			rec = prep.processText( text, true );
		}
		catch ( AnnotationFailedException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}
		catch ( TException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}
		boolean isPosOk = false;
		//        boolean isDepOk = false;
		//        boolean isParseOk = false;

		if ( rec.getLabelViews().containsKey( CuratorViewNames.pos ) &&
				rec.getLabelViews().containsKey( CuratorViewNames.tokens ) 
				//                &&
				//                rec.getParseViews().containsKey( CuratorViewNames.stanfordDep )
				)
		{
			Labeling lab = rec.getLabelViews().get( CuratorViewNames.pos );
			Labeling toks = rec.getLabelViews().get( CuratorViewNames.tokens );

			List< Span > tokens = toks.getLabels();
			List< Span >  posTags = lab.getLabels();


			if ( ( tokens.size() != posTags.size() ) ||
					tokens.size() != 21 )
			{
				System.err.println( "ERROR: tokens, pos have different/incorrect sizes." );
			}
			else 
				isPosOk = true;


			CuratorDataUtils.printLabeling( System.out, lab, text );
			System.out.println();

			//            Forest parse = rec.getParseViews().get( CuratorViewNames.stanfordParse );
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
			//            Forest dep = rec.getParseViews().get( CuratorViewNames.stanfordDep );
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


	/**
	 * tests whether a record created from a text string has the right number of 
	 *   sentences and tokens. 
	 */
	@Test
	public void testCreateRecord()
	{
		String text = "The CIA leaders thought Marin's father had left the building already. (But the humans were wrong.)" ;

		Record rec = null;
		try
		{
			rec = prep.createRecord( text, false );
		}
		catch ( AnnotationFailedException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}
		catch ( TException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}

		boolean isRecOk = checkRecord( rec );

		assertTrue( isRecOk );

	}


	/**
	 * tests whether a record created from a text string has the right number of 
	 *   sentences and tokens. 
	 */
	@Test
	public void testWsCreateRecord()
	{
		String text = "The CIA leaders thought Marin 's father had left the building already ." + 
				System.getProperty( "line.separator" ) + "( But the humans were wrong . )" ;

		Record rec = null;
		try
		{
			rec = prep.createRecord( text, true );
		}
		catch ( AnnotationFailedException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}
		catch ( TException e )
		{
			e.printStackTrace();
			fail( e.getMessage() );
		}

		boolean isRecordOk = checkRecord( rec );

		assertTrue( isRecordOk );

	}

	private boolean checkRecord( Record rec )
	{
		boolean isTokViewOk = false;
		if ( rec.getLabelViews().containsKey( CuratorViewNames.tokens ) )
		{
			Labeling toks = rec.getLabelViews().get( CuratorViewNames.tokens );
			System.err.println( "## tokens view has " + toks.getLabelsSize() + " tokens." );

			if ( toks.getLabels().size() == 21 )
				isTokViewOk = true;
		}


		boolean isSentViewOk = false;

		if ( rec.getLabelViews().containsKey( CuratorViewNames.sentences ) )
		{
			Labeling sents = rec.getLabelViews().get( CuratorViewNames.sentences );

			System.err.println( "## sentences view has " + sents.getLabelsSize() + " sentences." );

			if ( sents.getLabelsSize() == 2 )
				isSentViewOk = true;
		}

		return ( isTokViewOk && isSentViewOk );
	}


}
