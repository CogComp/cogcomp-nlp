package edu.illinois.cs.cogcomp.comma.readers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Parser for use with lbjava
 * Provides a few different options for ordering the commas being returned
 * @author navari
 *
 */
public class CommaParser implements Parser {
    private List<Comma> commas;
    private List<Sentence> sentences;
    Iterator<Comma> commaIt;

    public static enum Ordering{
    	RANDOM,//random ordering
    	ORDERED,//seeded random. useful if you want the same order over multiple runs
    	ORIGINAL,//original order
    }
  	private static final long seed = 123551225125L;

  	/**
  	 * 
  	 * @param sentences the sentences whose commas are to be presented
  	 * @param ordering the ordering of the commas: original, ordered, or random
  	 * @param orderCommasBySentence set to true if the all the commas of a sentence should be present before a comma from another sentence can be presented
  	 */
  	public CommaParser(List<Sentence> sentences, Ordering ordering, boolean orderCommasBySentence){
    	this.sentences = sentences;
    	commas = new ArrayList<>();
    	if (orderCommasBySentence) {
			switch (ordering) {
			case RANDOM:
				Collections.shuffle(sentences);
				break;
			case ORDERED:
				Collections.shuffle(sentences, new Random(seed));
				break;
			case ORIGINAL:
				break;
			}
		}
		for(Sentence s : sentences)
			commas.addAll(s.getCommas());

		if(!orderCommasBySentence){
			switch (ordering) {
			case RANDOM:
				Collections.shuffle(commas);
				break;
			case ORDERED:
				Collections.shuffle(commas, new Random(seed));
				break;
			case ORIGINAL:
				break;
			}
		}
		reset();
    }
    
    @Override
    public Object next() {
        if(commaIt.hasNext())
        	return commaIt.next();
        return null;
    }
    
    @Override
    public void reset() {
		commaIt = commas.iterator();
    }

    @Override
    public void close() {}
    
    public List<Sentence> getSentences(){
    	return sentences;
    }
}
