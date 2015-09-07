package edu.illinois.cs.cogcomp.comma;

import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Test;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.nlp.utilities.BasicTextAnnotationBuilder;

public class CommaTest extends TestCase{
	private Comma[] commas;
	@Override
	public void setUp() throws Exception {
        super.setUp();
        String[] sentence = "Mary , the clever scientist , was walking .".split("\\s+");
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections.singletonList(sentence));
        
        TokenLabelView tlv = new TokenLabelView(ViewNames.POS, "Test", ta, 1.0);
        tlv.addTokenLabel(0, "NNP", 1d);
        tlv.addTokenLabel(1, ",", 1d);
        tlv.addTokenLabel(2, "DT", 1d);
        tlv.addTokenLabel(3, "JJ", 1d);
        tlv.addTokenLabel(4, "NN", 1d);
        tlv.addTokenLabel(5, ",", 1d);
        tlv.addTokenLabel(6, "VBD", 1d);
        tlv.addTokenLabel(7, "VBG", 1d);
        tlv.addTokenLabel(8, ". ", 1d);


        TreeView parse = new TreeView(CommaProperties.getInstance().getConstituentParser(), "Test", ta, 1.0);
        String treeString = "(ROOT (S (NP (NP (NNP Mary)) (, ,) (NP (DT the) (JJ clever) (NN scientist)) (, ,)) " +
                "(VP (VBD was) (VP (VBG walking)))  (. .)))";
        parse.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(treeString));
        
        SpanLabelView ner = new SpanLabelView(ViewNames.NER_CONLL, "Test", ta, 1.0);
        ner.addSpanLabel(0, 1, "PER", 1.0);
        
        SpanLabelView shallowParse = new SpanLabelView(ViewNames.SHALLOW_PARSE, "Test", ta, 1.0);
        shallowParse.addSpanLabel(0, 1, "NP", 1.0);
        shallowParse.addSpanLabel(2, 5, "NP", 1.0);
        shallowParse.addSpanLabel(6, 8, "VP", 1.0);
        
        //TODO dependency parse
        //TODO SRL view
        
        ta.addView(tlv.getViewName(), tlv);
        ta.addView(parse.getViewName(), parse);
        ta.addView(ner.getViewName(), ner);
        ta.addView(shallowParse.getViewName(), shallowParse);
        
        commas = CommaLabeler.getCommas(ta).toArray(new Comma[2]);
        commas[0].setRole("Substitute");
        commas[1].setRole("Substitute");
    }
	
	@Test
	public void testLabels() {
		assertEquals("Substitute", commas[0].getBayraktarLabel());
		assertEquals("Substitute", commas[0].getVivekRole());
		assertEquals("Substitute", commas[1].getBayraktarLabel());
		assertEquals("Substitute", commas[1].getVivekRole());
		//TODO test VivekNaveen role
	}

	@Test
	public void testBayraktarPattern(){
		assertEquals("NP --> NP , NP ,", commas[0].getBayraktarPattern());
		assertEquals("NP --> NP , NP ,", commas[1].getBayraktarPattern());
	}
	
	@Test
	public void testGetWord(){
		assertEquals("Mary", commas[0].getWordToLeft(1));
		assertEquals("$$$", commas[0].getWordToLeft(2));
		assertEquals(",", commas[0].getWordToLeft(0));
		assertEquals("clever", commas[0].getWordToRight(2));
		assertEquals("###", commas[0].getWordToRight(1000));
	}
	
	@Test
	public void testGetPOS(){
		assertEquals("NNP", commas[0].getPOSToLeft(1));
		assertEquals("", commas[0].getPOSToLeft(2));
		assertEquals("DT", commas[0].getPOSToRight(1));
		assertEquals("JJ", commas[0].getPOSToRight(2));
		assertEquals("", commas[0].getPOSToRight(1000));
	}
	
	@Test
	public void testGetChunk(){
		assertEquals("NP", commas[0].getChunkToLeftOfComma(1).getLabel());
		assertEquals("NP", commas[0].getChunkToRightOfComma(1).getLabel());
		assertEquals("VP", commas[0].getChunkToRightOfComma(2).getLabel());
		assertEquals(null, commas[0].getChunkToRightOfComma(3));
		assertEquals(null, commas[0].getChunkToRightOfComma(0));
		assertEquals(null, commas[0].getChunkToLeftOfComma(-1));
		assertEquals(null, commas[0].getChunkToLeftOfComma(2));
	}
	
	@Test
	public void testGetSiblingPhrase(){
		assertEquals("NP", commas[0].getPhraseToLeftOfComma(1).getLabel());
		assertEquals("NP", commas[0].getPhraseToRightOfComma(1).getLabel());
		assertEquals(",", commas[0].getPhraseToRightOfComma(2).getLabel());
		assertEquals(null, commas[0].getPhraseToRightOfComma(3));
		assertEquals(null, commas[0].getPhraseToLeftOfComma(2));
	}
	
	@Test
	public void testGetParentSiblingPhrase(){
		assertEquals("NP", commas[0].getPhraseToLeftOfParent(0).getLabel());
		assertEquals("VP", commas[0].getPhraseToRightOfParent(1).getLabel());
		assertEquals(".", commas[0].getPhraseToRightOfParent(2).getLabel());
		assertEquals(null, commas[0].getPhraseToRightOfParent(3));
		assertEquals(null, commas[0].getPhraseToLeftOfParent(1));
	}
	
	//TODO testGetLeftTORightDependencies
	//TODO testGetRightToLeftDependencies
	//TODO testGetSRLFeatures
}
