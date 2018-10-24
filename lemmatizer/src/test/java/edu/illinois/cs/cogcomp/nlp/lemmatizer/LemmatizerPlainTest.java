/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LemmatizerPlainTest {

    private ArrayList<String> gold, input;

    @Before
    public void setUp() throws Exception {
        if (!new File("src/test/resources/input.txt").exists())
            fail("Input file missing.");
        if (!new File("src/test/resources/output.txt").exists())
            fail("Output file missing.");
        input = LineIO.read("src/test/resources/input.txt");
        gold = LineIO.read("src/test/resources/output.txt");
    }

    @Test
    public void test() {
        IllinoisLemmatizer lem = new IllinoisLemmatizer();

        ArrayList<String> lemmas = new ArrayList<>();

        for (String token : input) {
            String[] arr = token.split("\\s+");
            String word = arr[0];
            String pos = arr[1];
            lemmas.add(lem.getLemma(word, pos));
        }
        assertTrue(lemmas.equals(gold));
    }
}
