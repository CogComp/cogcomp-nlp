/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An instantiation of {@link edu.illinois.cs.cogcomp.nlp.utilities.HeadFinderBase} with Collins's
 * rules (Collins, 1999)
 *
 * @author Vivek Srikumar
 */
public class CollinsHeadFinder extends HeadFinderBase {

    public static CollinsHeadFinder instance;

    protected HashMap<String, List<Pair<HeadSearchDirection, String[]>>> headFinderInformation;

    public static CollinsHeadFinder getInstance() {
        if (instance == null)
            instance = new CollinsHeadFinder();
        return instance;
    }

    public CollinsHeadFinder() {
        headFinderInformation = new HashMap<>();

        HeadSearchDirection left = HeadSearchDirection.Left;
        HeadSearchDirection right = HeadSearchDirection.Right;
        HeadSearchDirection rightDis = HeadSearchDirection.RightDis;

        addHeadInformation("ADJP", left, new String[] {"NNS", "QP", "NN", "$", "ADVP", "JJ", "VBN",
                "VBG", "ADJP", "JJR", "NP", "JJS", "DT", "FW", "RBR", "RBS", "SBAR", "RB"});

        addHeadInformation("ADVP", right, new String[] {"RB", "RBR", "RBS", "FW", "ADVP", "TO",
                "CD", "JJR", "JJ", "IN", "NP", "JJS", "NN"});

        addHeadInformation("CONJP", right, new String[] {"CC", "RB", "IN"});

        addHeadInformation("FRAG", right, new String[] {});

        addHeadInformation("INTJ", left, new String[] {});

        addHeadInformation("LST", right, new String[] {"LS", ":"});

        addHeadInformation("NAC", left, new String[] {"NN", "NNS", "NNP", "NNPS", "NP", "NAC",
                "EX", "$", "CD", "QP", "PRP", "VBG", "JJ", "JJS", "JJR", "ADJP", "FW"});

        addHeadInformation("NX", left, new String[] {});

        addHeadInformation("PP", right, new String[] {"IN", "TO", "VBG", "VBN", "RP", "FW"});

        addHeadInformation("PRN", left, new String[] {});

        addHeadInformation("PRT", right, new String[] {"RP"});

        addHeadInformation("QP", left, new String[] {"IN", "NNS", "NN", "JJ", "RB", "DT", "CD",
                "NCD", "QP", "JJR", "JJS"});

        addHeadInformation("RRC", right, new String[] {"VP", "NP", "ADVP", "ADJP", "PP"});

        addHeadInformation("S", left, new String[] {"TO", "IN", "VP", "S", "SBAR", "ADJP", "UCP",
                "NP"});

        addHeadInformation("SBAR", left, new String[] {"WHNP", "WHPP", "WHADVP", "WHADJP", "IN",
                "DT", "S", "SQ", "SINV", "SBAR", "FRAG"});

        addHeadInformation("SBARQ", left, new String[] {"left", "SQ", "S", "SINV", "SBARQ", "FRAG"});

        addHeadInformation("SINV", left, new String[] {"VBZ", "VBD", "VBP", "VB", "MD", "VP", "S",
                "SINV", "ADJP", "NP"});

        addHeadInformation("SQ", left, new String[] {"VBZ", "VBD", "VBP", "VB", "MD", "VP", "SQ"});

        addHeadInformation("UCP", right, new String[] {});

        addHeadInformation("VP", left, new String[] {"TO", "VBD", "VBN", "MD", "VBZ", "VB", "VBG",
                "VBP", "AUX", "AUXG", "VP", "ADJP", "NN", "NNS", "NP"});

        addHeadInformation("WHADJP", left, new String[] {"CC", "WRB", "JJ", "ADJP"});

        addHeadInformation("WHADVP", right, new String[] {"CC", "WRB"});

        addHeadInformation("WHNP", left,
                new String[] {"WDT", "WP", "WP$", "WHADJP", "WHPP", "WHNP"});

        addHeadInformation("WHPP", right, new String[] {"IN", "TO", "FW"});

        addHeadInformation("X", right, new String[] {});

        addHeadInformation("NP", rightDis, new String[] {"NN", "NNP", "NNPS", "NNS", "NX", "POS",
                "JJR"});
        addHeadInformation("NP", left, new String[] {"NP"});
        addHeadInformation("NP", rightDis, new String[] {"$", "ADJP", "PRN"});
        addHeadInformation("NP", right, new String[] {"CD"});
        addHeadInformation("NP", rightDis, new String[] {"JJ", "JJS", "RB", "QP"});

        addHeadInformation("TYPO", left, new String[] {});
    }

    private void addHeadInformation(String nonTerminal, HeadSearchDirection direction, String[] tags) {
        if (!headFinderInformation.containsKey(nonTerminal)) {
            List<Pair<HeadSearchDirection, String[]>> values;
            values = new ArrayList<>();
            headFinderInformation.put(nonTerminal, values);
        }
        headFinderInformation.get(nonTerminal).add(new Pair<>(direction, tags));
    }

    @Override
    public List<Pair<HeadSearchDirection, String[]>> getNonterminalHeadInformation(
            String nonTerminal) {
        if (headFinderInformation.containsKey(nonTerminal))
            return headFinderInformation.get(nonTerminal);
        else
            return null;
    }

    @Override
    protected int fixHeadChildHacks(int headChildId, Constituent tree) {
        if (headChildId >= 2) {

            String prev = getChildLabel(tree, headChildId - 1);

            if (prev.equals("CC")) {
                int newHeadIndex = headChildId - 2;
                Constituent child = tree.getOutgoingRelations().get(newHeadIndex).getTarget();

                while (newHeadIndex >= 0 && ParseTreeProperties.isPreTerminal(child)
                        && POSUtils.isPOSPunctuation(child.getLabel()))
                    newHeadIndex--;
                if (newHeadIndex >= 0)
                    headChildId = newHeadIndex;
            }
        }
        return headChildId;
    }
}
