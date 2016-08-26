package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AffixesTest {
    private Word testWord;
    private Affixes affixesClassifier;

    @Before
    public void setup() {
        testWord = new Word("initialization");
        affixesClassifier = new Affixes();
    }

    @Test
    public void testClassify() {
        FeatureVector result = affixesClassifier.classify(testWord);
        assertTrue(result != null);
        String[] resultArray = result.discreteValueArray();
        assertEquals("tion", resultArray[5]);
    }
}
