package edu.illinois.cs.cogcomp.lbj.pos.tests;

import edu.illinois.cs.cogcomp.lbj.pos.TrainedPOSTagger;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.junit.Test;

/**
 * Created by mssammon on 3/20/15.
 */
public class TestPos
{
    private TrainedPOSTagger tagger;


    @Test
    public void testTagger()
    {
        tagger = new TrainedPOSTagger();
	
        String str = "My mother always told me I should never eat her father's boiled eggs, and although I tried hard "
        + "to remember I often failed to heed her \"sound\" advice.";
        String[] input = new String[ 1 ];
        input[ 0 ] = str;
        Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter( input )));
        String sentence = "";

        for (Token word = (Token) parser.next(); word != null; word = (Token) parser.next()) {
            String tag = tagger.discreteValue(word);
		System.out.println(tag);
            sentence += " (" + tag + " " + word.form + ")";

            if (word.next == null) {
                System.out.println(sentence.substring(1));
                sentence = "";
            }
        }
	
    }
}
