/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 *
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Vivek Srikumar Jul 7, 2009
 */
public class TestLongestCommonSubsequence {

//    protected void setUp() throws Exception {
//
//    }

    @Test
    public void testGetLCSMatchSentence() {

        String s1 = "Dan bought two books.";
        String s2 = "Dan bought two books .";

        List<IntPair> match = LongestCommonSubsequence.getCharacterLCS(s1, s2);

        for (IntPair obj : match) {

            assertEquals(s1.charAt(obj.getFirst() - 1), s2.charAt(obj.getSecond() - 1));
        }
    }

    @Test
    public void testGetLCSMatchMap() {
        String s1 = "Mr. John Smith-Murray died at Denver, CO.";
        String s2 = "Mr. John Smith - Murray died at Denver , CO .";

        Map<Integer, Integer> match = LongestCommonSubsequence.getCharacterLCSMap(s1, s2);

        for (int i = 0; i < s1.length(); i++) {
            assertEquals(s1.charAt(i), s2.charAt(match.get(i + 1) - 1));
        }

    }

    @Test
    public void testGetLCSMatch() {
        String strA = "ABABAB";
        String strB = "BABA";

        List<String> sequence1 = splitStringToChars(strA);
        List<String> sequence2 = splitStringToChars(strB);

        String[] results;



        results = new String[] {"B", "A", "B", "A"};

        LongestCommonSubsequence<String> lcsMatcher = new LongestCommonSubsequence<>();

        List<IntPair> lcsMatch = lcsMatcher.getLCSMatch(sequence1, sequence2);

        assertEquals(results.length, lcsMatch.size());
    }

    private static List<String> splitStringToChars(String str) {
        List<String> split = new ArrayList<>(str.length());

        for (int i = 0; i < str.length(); ++i)
            split.add(str.substring(i, i + 1));
        return split;
    }

}
