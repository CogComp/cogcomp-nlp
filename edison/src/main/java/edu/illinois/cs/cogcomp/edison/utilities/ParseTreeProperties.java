package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

import java.util.Arrays;
import java.util.List;

/**
 * This class consists of a set of functions of the form "is a label X?", where the label is a node
 * in the parse tree and X could be different linguistic classes, parts of speech, etc. The
 * functions assume that the parse trees are annotated in the Penn Treebank style.
 * <p/>
 * Most of the information is from the list <a
 * href="http://bulba.sdsu.edu/jeanette/thesis/PennTags.html">here</a>.
 *
 * @author Vivek Srikumar
 *         <p/>
 *         Jan 29, 2009
 */
public class ParseTreeProperties {

    private static List<String> punctuations = Arrays.asList("''", "``", "\"", "'", ",", ":", ".",
            "(", ")", "[", "]", "{", "}");

    /**
     * Is a given tree a pre-terminal. This could be used to check for POS tag too, if the
     * convention is that pre-terminals are POS tags.
     */
    public static boolean isPreTerminal(Tree<String> tree) {
        if (tree.isLeaf())
            return false;
        return tree.getChild(0).isLeaf();
    }

    public static boolean isPreTerminal(Constituent treeNode) {
        List<Relation> out = treeNode.getOutgoingRelations();
        if (out.size() == 0)
            return false;

        out = out.get(0).getTarget().getOutgoingRelations();
        return out.size() == 0;
    }

    public static boolean isNonTerminalNoun(String nonTerminalToken) {
        nonTerminalToken = ParseUtils.stripFunctionTags(nonTerminalToken);

        return nonTerminalToken.startsWith("NP") || nonTerminalToken.startsWith("NX");
    }

    public static boolean isNonTerminalVerb(String nonTerminalToken) {
        nonTerminalToken = ParseUtils.stripFunctionTags(nonTerminalToken);

        return nonTerminalToken.equals("VP");

    }

    public static boolean isNonTerminalAdjective(String nonTerminalToken) {
        nonTerminalToken = ParseUtils.stripFunctionTags(nonTerminalToken);

        return nonTerminalToken.equals("ADJP");

    }

    public static boolean isNonTerminalAdverb(String nonTerminalToken) {
        nonTerminalToken = ParseUtils.stripFunctionTags(nonTerminalToken);

        return nonTerminalToken.equals("ADVP");

    }

    public static boolean isNonTerminalPP(String nonTerminalToken) {
        nonTerminalToken = ParseUtils.stripFunctionTags(nonTerminalToken);

        return nonTerminalToken.equals("PP");

    }

    public static boolean isNominal(String token) {
        return isNonTerminalNoun(token) || POSUtils.isPOSNoun(token);
    }

    public static boolean isVerb(String token) {
        return isNonTerminalVerb(token) || POSUtils.isPOSVerb(token);
    }

    public static boolean isPunctuationToken(String label) {
        return punctuations.contains(label);
    }

    public static boolean isNullLabel(String label) {
        return label.equals("-NONE-");
    }

}
