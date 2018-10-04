/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers.clausie;

import java.util.List;

public class ClausieSplitter {

    public static String[] split(String sentence) {
        List<ClausieWrapper.SVO> splitted = ClausieWrapper.extract(sentence);

        // Clausie can make tons of mistakes. Do not split anything then.
        if (splitted == null)
            return new String[] {sentence};

        String[] toReturn = new String[splitted.size()];
        int i = 0;

        // S: He, V: went, O: to the temple in a car.
        for (ClausieWrapper.SVO s : splitted) {
            StringBuilder sStr =
                    new StringBuilder(dropAtRateOf(s.s.word)).append(" ").append(
                            dropAtRateOf(s.v.word)).append(" ").append(
                            dropAtRateOf(s.o.word));
            toReturn[i++] = sStr.toString().trim();
        }

        return toReturn;
    }

    private static String dropAtRateOf(String w) {
        // niket@1 goes@3 to "niket goes"
        char sep = '@';
        if (w == null || w.isEmpty() || w.indexOf(sep) < 0)
            return w;

        StringBuilder sb = new StringBuilder();

        for (String token : w.split(" ")) {
            // niket@1 be (some words can contain no @)
            int AtRateOfPos = token.indexOf(sep);
            sb.append(sb.length() > 0 ? " " : "").append(
                    AtRateOfPos >= 0 ? token.substring(0, token.indexOf(sep))
                            : token);
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(ClausieUtil.arrayToString(ClausieSplitter.split(
                "Daniel works with Prof. Roth and Dr. Tushar."), "\n"));
    }
}

