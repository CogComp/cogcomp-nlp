/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TreeParser<S> {
    private INodeReader<S> nodeParser;
    private List<String> tokens;
    private int currentTokenId;

    public TreeParser(INodeReader<S> nodeParser) {
        this.nodeParser = nodeParser;
    }

    public Tree<S> parse(String treeString) {
        tokens = tokenize(treeString);

        currentTokenId = 0;

        Tree<S> tree = parseExpr(new Tree<S>());

        Tree<S> t1 = tree.getChild(0);
        t1.makeParentNull();

        return t1;
    }

    private Tree<S> parseExpr(Tree<S> tree) {

        String currentToken = tokens.get(currentTokenId);

        if (currentToken.equals("(")) {
            currentTokenId++;
            currentToken = tokens.get(currentTokenId);

            Tree<S> newTree = new Tree<>(nodeParser.parseNode(currentToken));

            currentTokenId++;

            newTree = parseSequence(newTree);

            currentToken = tokens.get(currentTokenId);
            if (currentToken.equals(")")) {
                currentTokenId++;
                tree.addSubtree(newTree);

                return tree;
            } else {
                throw new IllegalArgumentException("Missing close parenthesis near " + currentToken);
            }

        } else {
            tree.addLeaf(nodeParser.parseNode(currentToken));

            currentTokenId++;
            return tree;
        }
    }

    private Tree<S> parseSequence(Tree<S> currentTree) {

        // at this point, i have already seen at least an open parenthesis AND
        // an atom. If the next character is a character is a close parenthesis,
        // then I just return the input. NOTE: DO NOT MOVE THE TOKEN POINTER.

        String currentToken = tokens.get(currentTokenId);
        if (currentToken.equals(")")) {
            return currentTree;
        } else {
            // now we have something here. that should be an expression followed
            // by a sequence. Find the expression, attach it to the current
            // tree. Then give that combined tree to the sequence parser and
            // return whatever it gives.

            parseExpr(currentTree);
            return parseSequence(currentTree);
        }

    }

    // See
    // http://stackoverflow.com/questions/2206378/how-to-split-a-string-but-also-keep-the-delimiters
    private final static String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private final static Pattern regex = Pattern.compile(String.format(WITH_DELIMITER,
            "[\\s\\(\\)]"));

    private List<String> tokenize(String treeString) {

        String[] tok1 = regex.split(treeString);

        List<String> toks = new ArrayList<>();

        for (String s : tok1) {
            if (!(s.length() == 0 || s.matches("\\s+")))
                toks.add(s);
        }

        return toks;
    }

    public static Tree<String> parseStringTree(String treeString) {
        return new TreeParser<>(new INodeReader<String>() {

            public String parseNode(String string) {
                return string;
            }
        }).parse(treeString);
    }

}
