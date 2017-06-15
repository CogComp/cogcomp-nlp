package edu.illinois.cs.cogcomp.mrcs;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import edu.illinois.cs.cogcomp.llm.common.PhraseList;
import edu.illinois.cs.cogcomp.llm.common.Preprocess;

public class AnnotationTest {
	
	@Test
	public void contPhraseTest(){
		PhraseList pl=new PhraseList("src/main/resources/phrases.txt");
		String s="please turn the television monitor on yo";
		String ret=new Preprocess().getPhrase(s, pl);
		System.out.println(ret);
		assertTrue(ret.equals("please turn_on the television_monitor yo"));
	}
	
	
	@Test
	public void discontPhraseTest(){
		PhraseList pl=new PhraseList("src/main/resources/phrases.txt");
		String s="please turn the light on";
		String ret=new Preprocess().getDiscontPhrase(s, pl);
		System.out.println(ret);
		assertTrue(ret.equals("please turn_on the light"));
	}
	
	
	@Ignore
	@Test
	public void nerTest(){
		
	}
}
