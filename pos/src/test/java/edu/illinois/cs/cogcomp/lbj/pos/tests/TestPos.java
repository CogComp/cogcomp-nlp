package edu.illinois.cs.cogcomp.lbj.pos.tests;

import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
import edu.illinois.cs.cogcomp.lbj.pos.POSTaggerKnown;
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
    private POSTagger tagger;


    @Test
    public void testTagger()
    {
	POSTaggerKnown.read("models/edu/illinois/cs/cogcomp/lbj/pos/POSTaggerKnown.lc", "models/edu/illinois/cs/cogcomp/lbj/pos/POSTaggerKnown.lex");
        tagger = new POSTagger();
	/*
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
	*/
    }
}
