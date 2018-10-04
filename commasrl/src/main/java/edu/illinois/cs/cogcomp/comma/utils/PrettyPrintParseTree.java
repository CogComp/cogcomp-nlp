/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;

public class PrettyPrintParseTree {
    public static String pennString(Tree<String> tree) {
        StringWriter sw = new StringWriter();
        display(tree, 0, false, false, false, true, true, new PrintWriter(sw));
        return sw.toString();
    }

    private static void display(Tree<String> tree, int indent, boolean parentLabelNull,
            boolean firstSibling, boolean leftSiblingPreTerminal, boolean topLevel,
            boolean onlyLabelValue, PrintWriter pw) {
        // the condition for staying on the same line in Penn Treebank
        boolean suppressIndent =
                (parentLabelNull || (leftSiblingPreTerminal && isPreTerminal(tree) && (tree
                        .getLabel() == null || !tree.getLabel().startsWith("CC"))));
        if (suppressIndent) {
            pw.print(" ");
            // pw.flush();
        } else {
            if (!topLevel) {
                pw.println();
            }
            for (int i = 0; i < indent; i++) {
                pw.print(" ");
                // pw.flush();
            }
        }
        if (tree.isLeaf() || isPreTerminal(tree)) {
            String terminalString =
                    toStringBuilder(tree, new StringBuilder(), onlyLabelValue).toString();
            pw.print(terminalString);
            pw.flush();
            return;
        }
        pw.print("(");
        String nodeString;
        if (onlyLabelValue) {
            String value = tree.getLabel();
            nodeString = (value == null) ? "" : value;
        } else {
            nodeString = "FML";
        }
        pw.print(nodeString);
        // pw.flush();
        boolean parentIsNull = tree.getLabel() == null || tree.getLabel() == null;
        displayChildren(tree.getChildren(), indent + nodeString.length() + 2, parentIsNull, true,
                pw);
        pw.print(")");
        pw.flush();
    }

    private static void displayChildren(List<Tree<String>> trChildren, int indent,
            boolean parentLabelNull, boolean onlyLabelValue, PrintWriter pw) {
        boolean firstSibling = true;
        boolean leftSibIsPreTerm = true; // counts as true at beginning
        for (Tree<String> currentTree : trChildren) {
            display(currentTree, indent, parentLabelNull, firstSibling, leftSibIsPreTerm, false,
                    onlyLabelValue, pw);
            leftSibIsPreTerm = isPreTerminal(currentTree);
            // CC is a special case for English, but leave it in so we can
            // exactly match PTB3 tree formatting
            if (currentTree.getLabel() != null && currentTree.getLabel().startsWith("CC")) {
                leftSibIsPreTerm = false;
            }
            firstSibling = false;
        }
    }

    public static StringBuilder toStringBuilder(Tree<String> tree, StringBuilder sb,
            boolean printOnlyLabelValue) {
        if (tree.isLeaf()) {
            if (tree.getLabel() != null) {
                if (printOnlyLabelValue) {
                    sb.append(tree.getLabel());
                } else {
                    sb.append(tree.getLabel());
                }
            }
            return sb;
        } else {
            sb.append('(');
            if (tree.getLabel() != null) {
                if (printOnlyLabelValue) {
                    if (tree.getLabel() != null) {
                        sb.append(tree.getLabel());
                    }
                    // don't print a null, just nothing!
                } else {
                    sb.append(tree.getLabel());
                }
            }
            List<Tree<String>> kids = tree.getChildren();
            if (kids != null) {
                for (Tree<String> kid : kids) {
                    sb.append(' ');
                    toStringBuilder(kid, sb, printOnlyLabelValue);
                }
            }
            return sb.append(')');
        }
    }

    public static boolean isPreTerminal(Tree t) {
        List<Tree> kids = t.getChildren();
        return (kids.size() == 1) && (kids.get(0).isLeaf());
    }

}
