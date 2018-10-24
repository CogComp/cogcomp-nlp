/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.algorithms.Mappers;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains utility functions for manipulating parse trees.
 *
 * @author Vivek Srikumar
 */
public class ParseUtils {

    /**
     * Convert brackets from the Penn treebank format (which uses strings like -LRB-, -RRB-, etc to
     * denote '(', ')', etc.) to readable tokens.
     *
     * @param sentence A sentence which is to be converted
     * @return A new sentence.
     */
    public static String convertBracketsFromPTBFormat(String sentence) {

        sentence = sentence.replaceAll("-LRB-", "(");
        sentence = sentence.replaceAll("-RRB-", ")");
        sentence = sentence.replaceAll("-LSB-", "[");
        sentence = sentence.replaceAll("-RSB-", "]");
        sentence = sentence.replaceAll("-LCB-", "{");
        sentence = sentence.replaceAll("-RCB-", "}");

        return sentence;
    }

    /**
     * Convert brackets from readable forms to the Penn treebank format (which uses strings like
     * -LRB-, -RRB-, etc to denote '(', ')', etc.)
     *
     * @param sentence A sentence which is to be converted
     * @return A new sentence.
     */
    public static String convertBracketsToPTBFormat(String sentence) {

        sentence = sentence.replaceAll("\\(", "-LRB-");
        sentence = sentence.replaceAll("\\)", "-RRB-");
        sentence = sentence.replaceAll("\\[", "-LSB-");
        sentence = sentence.replaceAll("\\]", "-RSB-");
        sentence = sentence.replaceAll("\\{", "-LCB-");
        sentence = sentence.replaceAll("\\}", "-RCB-");

        return sentence;

    }

    /**
     * Transforms a parse tree into a new tree where each node is labeled by the span it covers in
     * addition to the label of that node from the original parse tree.
     * <p>
     * For example, consider the following input tree:
     * <p>
     * <p>
     * 
     * <pre>
     *
     * (S1 (S (NP (DT The)
     *            (NN bird))
     *        (VP (VBD flew)))
     *        (. .))
     * </pre>
     * <p>
     * This is transformed as follows:
     * <p>
     * <p>
     * 
     * <pre>
     * ([S1,[0,4]] ([S,[0,4]] ([NP,[0,2]] ([DT,[0,1]] [The,&lt;0,1]])
     *                                    ([NN,[1,2]] [bird,[1,2]]))
     *                        ([VP,[2,3]] ([VBD,[2,3]] [flew,[2,3]]))
     *                        ([.,[3,4]] [.,[3,4]])))
     * </pre>
     * <p>
     * Here, the notation [.,.] is used to denote a {@link Pair} object. That is, the node labeled
     * [NP,[0,2]] indicates that the corresponding node in the parse tree is labeled NP and that NP
     * spans the tokens ranging from 0 to 2 (exclusive.)
     *
     * @param parseTree The parse tree to be annotated with the spans
     * @return A new tree where each node is the {@link Pair} of the original node's label and the
     *         span that the node covers.
     */
    public static Tree<Pair<String, IntPair>> getSpanLabeledTree(Tree<String> parseTree) {
        return getSpanLabeledTree(parseTree, 0).getFirst();
    }

    private static Pair<Tree<Pair<String, IntPair>>, Integer> getSpanLabeledTree(
            Tree<String> parseTree, int currentLeafId) {

        // This code is ugly and could use some re-factoring. Also, it seems
        // that this code might be slow. So a faster version is also in order.

        if (parseTree.isLeaf()) {
            IntPair span;

            if (ParseTreeProperties.isNullLabel(parseTree.getParent().getLabel())) {
                span = new IntPair(currentLeafId, currentLeafId);
            } else {
                span = new IntPair(currentLeafId, currentLeafId + 1);
                currentLeafId++;
            }

            Pair<String, IntPair> label = new Pair<>(parseTree.getLabel(), span);
            Tree<Pair<String, IntPair>> tree = new Tree<>(label);

            return new Pair<>(tree, currentLeafId);
        }

        List<Tree<Pair<String, IntPair>>> children = new ArrayList<>();

        int start = currentLeafId;
        for (Tree<String> child : parseTree.getChildren()) {
            Pair<Tree<Pair<String, IntPair>>, Integer> tmp =
                    getSpanLabeledTree(child, currentLeafId);
            currentLeafId = tmp.getSecond();
            children.add(tmp.getFirst());
        }

        int end = currentLeafId;

        IntPair span = new IntPair(start, end);
        Pair<String, IntPair> label = new Pair<>(parseTree.getLabel(), span);
        Tree<Pair<String, IntPair>> output = new Tree<>(label);

        for (Tree<Pair<String, IntPair>> child : children) {
            output.addSubtree(child);
        }

        return new Pair<>(output, currentLeafId);
    }

    private static boolean createSnippedTree(Tree<String> original, Tree<String> newTree) {
        assert original.getLabel().equals(newTree.getLabel());

        if (original.isLeaf())
            return true;

        boolean addedChild = false;

        for (Tree<String> child : original.getChildren()) {
            String label = child.getLabel();
            if (ParseTreeProperties.isNullLabel(label))
                continue;

            Tree<String> newChild = new Tree<>(label);

            if (!createSnippedTree(child, newChild))
                continue;

            newTree.addSubtree(newChild);
            addedChild = true;
        }

        return addedChild;
    }

    /**
     * Removes subtrees labeled with the null label (-NONE-) and returns a new tree
     *
     * @param tree A parse tree, possibly containing null labels
     * @return A new parse tree where subtrees with the null label are removed
     */
    public static Tree<String> snipNullNodes(Tree<String> tree) {
        Tree<String> newTree = new Tree<>(tree.getLabel());

        createSnippedTree(tree, newTree);

        return newTree;
    }

    /**
     * Strips function tags from a given node label. This function assumes that everything that
     * comes before the first hyphen is useful and returns that.
     *
     * @param label A node label
     * @return The node label without function tags
     */
    public static String stripFunctionTags(String label) {
        if (label.indexOf("-") > 0)
            return label.substring(0, label.indexOf("-"));
        else
            return label;

    }

    /**
     * Strips function tags from a tree and returns a new tree.
     *
     * @param tree A parse tree
     * @return A parse tree without any function tags
     */
    @SuppressWarnings("serial")
    public static Tree<String> stripFunctionTags(Tree<String> tree) {
        return Mappers.mapTree(tree, new ITransformer<Tree<String>, String>() {
            @Override
            public String transform(Tree<String> input) {
                if (input.isLeaf())
                    return input.getLabel();
                else
                    return stripFunctionTags(input.getLabel());
            }
        });
    }

    /**
     * Removes index information in the parse tree to other nodes. That is, for nodes of the form
     * "X=1", it converts it to just "X".
     *
     * @param tree A parse tree
     * @return A new tree where the index information is removed.
     */
    @SuppressWarnings("serial")
    public static Tree<String> stripIndexReferences(Tree<String> tree) {
        return Mappers.mapTree(tree, new ITransformer<Tree<String>, String>() {

            @Override
            public String transform(Tree<String> input) {
                if (input.isLeaf())
                    return input.getLabel();

                String label = input.getLabel();
                return stripIndexReferences(label);

            }
        });
    }

    /**
     * Gets the terminal string from the parse tree. The tokens in the output are separated by
     * spaces.
     *
     * @param tree The parse tree, where the leaf nodes are the terminals we care about
     * @return The terminal string
     */
    public static String getSentenceFromTree(Tree<String> tree) {
        StringBuilder sb = new StringBuilder();

        for (Tree<String> t : tree.getYield()) {
            String token = t.getLabel();
            sb.append(SentenceUtils.makeSentencePresentable(token).trim()).append(" ");
        }

        return sb.toString().trim();
    }

    public static String stripIndexReferences(String label) {
        if (label.indexOf("=") <= 0)
            return label;
        else
            return label.substring(0, label.indexOf("="));
    }

    /**
     * This string indicates a path going up a parse tree in the path string.
     */
    public final static String PATH_UP_STRING = " *^* ";

    /**
     * This string indicates a path going down a parse tree in the path string.
     */
    public final static String PATH_DOWN_STRING = " *v* ";

    /**
     * Get the labels of all the siblings of the parse tree node that covers the input constituent.
     * Note that the input constituent could be from any view. The output includes the phrase label
     * of this constituent too.
     *
     * @param parseViewName The name of the parse view. This might typically be one of
     *        ViewNames.PARSE_CHARNIAK or ViewNames.PARSE_STANFORD
     * @param constituent The constituent whose sibling phrases are required.
     * @return An array of strings. If the input constituent is the entire sentence, then an array
     *         of size zero is returned to indicate that the corresponding parse tree node does not
     *         have any siblings.
     */
    public static String[] getAllPhraseSiblingLabels(String parseViewName, Constituent constituent) {
        Tree<String> node = getParseTreeCovering(parseViewName, constituent);
        return getAllSiblingLabels(node);
    }

    /**
     * Get the labels of all siblings of a given tree node.
     *
     * @param node The node whose siblings are required.
     * @return The labels of the siblings of the input node, as an array of strings. If the input is
     *         the root of the tree, then a string array of size zero is returned to indicate that
     *         the corresponding parse tree node does not have any siblings.
     */
    public static String[] getAllSiblingLabels(Tree<String> node) {
        List<String> siblings = new ArrayList<>();

        if (node.isRoot())
            return new String[0];

        Tree<String> parent = node.getParent();
        for (Tree<String> child : parent.childrenIterator()) {
            siblings.add(child.getLabel());
        }

        return siblings.toArray(new String[siblings.size()]);
    }

    /**
     * Finds a common ancestor of two trees which are both contained in a larger tree. This searches
     * the path from the head of the trees to the root of the trees till it finds a common node. If
     * none is found, then the function returns null.
     *
     * @param t1 The first tree
     * @param t2 The second tree
     * @param tree The tree that contains {@code t1} and {@code t2}.
     * @return The tree that is the common ancestor of {@code t1} and {@code t2} . If none is found,
     *         then the function returns null.
     */
    @Deprecated
    public static <T> Tree<T> getCommonAncestor(Tree<T> t1, Tree<T> t2, Tree<T> tree)
            throws Exception {
        List<Tree<T>> p1 = getPathTreesToRoot(tree, t1, 400);
        List<Tree<T>> p2 = getPathTreesToRoot(tree, t2, 400);

        Collections.reverse(p1);
        Collections.reverse(p2);

        for (Tree<T> aP1 : p1) {
            if (p2.contains(aP1))
                return aP1;
        }

        return null;
    }

    /**
     * Get the parse tree of a sentence. This code assumes that the view called
     * {@code parseViewName} exists in the text annotation.
     *
     * @param parseViewName The name of the parse view
     * @param s The sentence
     * @return The parse tree of the sentence
     */
    public static Tree<String> getParseTree(String parseViewName, Sentence s) {
        return getParseTree(parseViewName, s.getSentenceConstituent().getTextAnnotation(),
                s.getSentenceId());
    }

    /**
     * Get the parse tree of the <code>sentenceId</code>th sentence from the text annotation. This
     * code assumes that the view called {@code parseViewName} exists in the text annotation.
     *
     * @param parseViewName The name of the parse view
     * @param ta The text annotation object
     * @param sentenceId The sentence whose parse tree is required
     * @return The parse tree of the {@code sentenceId}th sentence
     */
    public static Tree<String> getParseTree(String parseViewName, TextAnnotation ta, int sentenceId) {
        return ((TreeView) (ta.getView(parseViewName))).getTree(sentenceId);
    }

    /**
     * Get the Charniak parse tree of the <code>sentenceId</code>th sentence from the text
     * annotation. This code assumes that the view called ViewNames.PARSE_CHARNIAK exists in the
     * text annotation.
     *
     * @param ta The text annotation object
     * @param sentenceId The sentence whose parse tree is required
     * @return The parse tree of the {@code sentenceId}th sentence
     */
    @Deprecated
    public static Tree<String> getParseTree(TextAnnotation ta, int sentenceId) {
        return ((TreeView) (ta.getView(ViewNames.PARSE_CHARNIAK))).getTree(sentenceId);

    }

    /**
     * Get a parse tree from a text annotation that covers the specified constituent.
     *
     * @param parseViewName The name of the parse view
     * @param c The constituent that we care about
     * @return The portion of the parse tree of the {@link TextAnnotation} to which the constituent
     *         belongs which covers the constituent.
     */
    public static Tree<String> getParseTreeCovering(String parseViewName, Constituent c) {

        TextAnnotation ta = c.getTextAnnotation();
        int sentenceId = ta.getSentenceId(c);
        Tree<String> tree = getParseTree(parseViewName, ta, sentenceId);

        int sentenceStartSpan = ta.getSentence(sentenceId).getStartSpan();

        int start = c.getStartSpan() - sentenceStartSpan;
        int end = c.getEndSpan() - sentenceStartSpan;

        return getTreeCovering(tree, start, end);
    }

    /**
     * Returns a pair of paths. The first element of the pair is the path up from the start node to
     * the common ancestor of start and end. The second element is the path down from the common
     * ancestor to the end node.
     */
    @Deprecated
    public static <T> Pair<List<Tree<T>>, List<Tree<T>>> getPath(Tree<T> start, Tree<T> end,
            Tree<T> tree, int maxDepth) throws Exception {
        List<Tree<T>> p1 = getPathTreesToRoot(tree, start, maxDepth);
        List<Tree<T>> p2 = getPathTreesToRoot(tree, end, maxDepth);

        Collections.reverse(p1);
        Collections.reverse(p2);

        boolean foundAncestor = false;
        List<Tree<T>> pathUp = new ArrayList<>();

        for (Tree<T> aP1 : p1) {
            if (!foundAncestor) {
                pathUp.add(aP1);
            }
            if (p2.contains(aP1)) {
                foundAncestor = true;
                break;
            }
        }
        if (!foundAncestor)
            throw new Exception("Common ancestor not found in path down.");

        List<Tree<T>> pathDown = new ArrayList<>();
        foundAncestor = false;

        for (Tree<T> aP2 : p2) {
            if (!foundAncestor) {
                pathDown.add(aP2);
            }
            if (p1.contains(aP2)) {
                foundAncestor = true;
                break;
            }
        }

        if (!foundAncestor)
            throw new Exception("Common ancestor not found in path up.");

        Collections.reverse(pathDown);

        return new Pair<>(pathUp, pathDown);

    }

    /**
     * Get a string representing the path between trees {@code start} and {@code end} that belong to
     * the tree {@code tree}
     */
    @Deprecated
    public static <T> String getPathString(Tree<T> start, Tree<T> end, Tree<T> tree, int maxDepth)
            throws Exception {

        Pair<List<Tree<T>>, List<Tree<T>>> paths = getPath(start, end, tree, maxDepth);

        StringBuilder buffer = new StringBuilder();
        for (Tree<T> up : paths.getFirst()) {
            buffer.append(up.getLabel()).append(PATH_UP_STRING);
        }

        for (Tree<T> up : paths.getSecond()) {
            buffer.append(up.getLabel()).append(PATH_DOWN_STRING);
        }

        return buffer.toString();

    }

    @Deprecated
    public static <T> String getPathStringIgnoreLexicalItems(Tree<T> start, Tree<T> end,
            Tree<T> tree, int maxDepth) throws Exception {

        Pair<List<Tree<T>>, List<Tree<T>>> paths = getPath(start, end, tree, maxDepth);

        StringBuilder buffer = new StringBuilder();

        for (int i = 1; i < paths.getFirst().size(); i++) {
            buffer.append(paths.getFirst().get(i).getLabel()).append(PATH_UP_STRING);
        }

        for (int i = 0; i < paths.getSecond().size() - 1; i++)

        {
            buffer.append(paths.getSecond().get(i).getLabel()).append(PATH_DOWN_STRING);
        }

        return buffer.toString();

    }

    @Deprecated
    public static <T> String getPathStringToCommonAncestor(Tree<T> start, Tree<T> end,
            Tree<T> tree, int maxDepth) throws Exception {
        Pair<List<Tree<T>>, List<Tree<T>>> paths = getPath(start, end, tree, maxDepth);

        StringBuilder buffer = new StringBuilder();

        for (int i = 1; i < paths.getFirst().size(); i++) {
            buffer.append(paths.getFirst().get(i).getLabel()).append(PATH_UP_STRING);
        }

        return buffer.toString();
    }

    @Deprecated
    public static <T> List<T> getPathToRoot(Tree<T> tree, Tree<T> leaf, int maxDepth)
            throws Exception {
        List<T> path = new ArrayList<>();

        int depth = 0;
        Tree<T> t = leaf;
        while (!t.equals(tree)) {
            path.add(t.getLabel());
            t = t.getParent();
            depth++;
            if (depth > maxDepth) {
                break;
            }
        }

        path.add(tree.getLabel());

        Collections.reverse(path);

        return path;
    }

    /**
     * This function returns a list of trees from a node in the tree to the root. If the node is
     * deeper than maxdepth, then the bottom maxDepth nodes are returned.
     */
    @Deprecated
    public static <T> List<Tree<T>> getPathTreesToRoot(Tree<T> tree, Tree<T> node, int maxDepth)
            throws Exception {
        List<Tree<T>> path = new ArrayList<>();

        int depth = 0;
        Tree<T> t = node;
        while (!t.equals(tree)) {
            path.add(t);
            t = t.getParent();

            if (t == null)
                throw new Exception(node + " is not an element of " + tree);
            depth++;
            if (depth > maxDepth) {
                break;
            }
        }

        path.add(tree);

        Collections.reverse(path);

        return path;
    }

    /**
     * Assuming that the tree comes with lexical items and POS tags, the subcat frame for the verb
     * can be found by going to the parent of the POS tag (which is probably a VP) and listing its
     * children.
     */
    public static String getSubcatFrame(Tree<String> yieldNode) {
        StringBuilder sb = new StringBuilder();
        Tree<String> node = yieldNode.getParent().getParent();

        for (Tree<String> t : node.childrenIterator()) {
            sb.append(t.getLabel()).append(" ");
        }

        return sb.toString();
    }

    public static String getTerminalString(Tree<String> tree) {
        StringBuilder sb = new StringBuilder();
        for (Tree<String> y : tree.getYield()) {
            sb.append(y.getLabel()).append(" ");
        }

        return sb.toString();
    }

    public static String[] getTerminalStringSentence(Tree<String> tree) {
        List<String> words = new ArrayList<>();
        for (Tree<String> y : tree.getYield()) {
            if (!ParseTreeProperties.isNullLabel(y.getParent().getLabel()))
                words.add(ParseUtils.convertBracketsFromPTBFormat(y.getLabel()));
        }

        return words.toArray(new String[words.size()]);
    }

    public static List<String> getTerminalTokens(Tree<String> tree) {
        List<String> output = new ArrayList<>();
        for (Tree<String> t : tree.getYield()) {
            output.add(t.getLabel());
        }

        return output;
    }

    public static Tree<Pair<String, IntPair>> getTokenIndexedCleanedParseTreeNodeCovering(
            Constituent c, String parseViewName) {
        TextAnnotation ta = c.getTextAnnotation();

        Tree<String> tree = getParseTree(parseViewName, ta, ta.getSentenceId(c));

        int start = c.getStartSpan();
        int end = c.getEndSpan();

        tree = ParseUtils.snipNullNodes(tree);
        tree = ParseUtils.stripFunctionTags(tree);
        tree = ParseUtils.stripIndexReferences(tree);

        return getTokenIndexedTreeCovering(tree, start, end);
    }

    public static Tree<Pair<String, IntPair>> getTokenIndexedParseTreeNodeCovering(
            String parseViewName, Constituent c) {

        // / UGLY CODE ALERT!!!

        TextAnnotation ta = c.getTextAnnotation();
        int sentenceId = ta.getSentenceId(c);

        Tree<String> tree = getParseTree(parseViewName, ta, sentenceId);

        final int sentenceStartSpan = ta.getSentence(sentenceId).getStartSpan();
        int start = c.getStartSpan() - sentenceStartSpan;
        int end = c.getEndSpan() - sentenceStartSpan;

        // Find the tree that covers the start and end tokens. However, start
        // and end have been shifted relative to the start of the sentence. So
        // we need to shift it back, which is why we have that UGLY as sin
        // mapper at the end.

        Tree<Pair<String, IntPair>> toknTree = getTokenIndexedTreeCovering(tree, start, end);

        ITransformer<Tree<Pair<String, IntPair>>, Pair<String, IntPair>> transformer =
                new ITransformer<Tree<Pair<String, IntPair>>, Pair<String, IntPair>>() {

                    @Override
                    public Pair<String, IntPair> transform(Tree<Pair<String, IntPair>> input) {

                        Pair<String, IntPair> label = input.getLabel();

                        IntPair newSpan =
                                new IntPair(label.getSecond().getFirst() + sentenceStartSpan, label
                                        .getSecond().getSecond() + sentenceStartSpan);
                        return new Pair<>(label.getFirst(), newSpan);
                    }
                };
        return Mappers.mapTree(toknTree, transformer);
    }

    /**
     * From a parse tree and a span that is specified with the start and end (exclusive), this
     * function returns a tree that corresponds to the subtree that covers the span. Each node in
     * the new tree corresponds to a node in the input tree and is labeled with the label of the
     * original node along with the span that this node covered in the original tree.
     *
     * @return A new tree that covers the specified span and each node specifies the label and the
     *         span of the original tree that it covers.
     */
    public static Tree<Pair<String, IntPair>> getTokenIndexedTreeCovering(Tree<String> parse,
            int start, int end) {

        Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(parse);

        Tree<Pair<String, IntPair>> current = spanLabeledTree;
        while (current != null) {
            IntPair span = current.getLabel().getSecond();
            if (span.getFirst() == start && span.getSecond() == end) {
                return current;
            } else {
                boolean found = false;
                for (Tree<Pair<String, IntPair>> child : current.getChildren()) {
                    if (child.getLabel().getSecond().getFirst() <= start
                            && child.getLabel().getSecond().getSecond() >= end) {
                        current = child;
                        found = true;
                        break;
                    }
                }
                if (!found)
                    break;
            }

        }

        return current;
    }

    public static Tree<String> getTreeCovering(Tree<String> parse, int start, int end) {
        Tree<String> tree = parse.getYield().get(start);

        while (!tree.isRoot()) {
            if (tree.getYield().size() >= (end - start)) {
                break;
            }
            tree = tree.getParent();
        }

        return tree;
    }

    /**
     * Get the head word of a constituent using the {@link HeadFinderBase} that is passed as an
     * argument. To use this function, first, a head finder should be created. For example:
     * <p>
     * 
     * <pre>
     *   TextAnnotation ta = ... // some text annotation
     *   Constituent c = ... // some constituent
     * 
     *   CollinsHeadFinder headFinder = new CollinsHeadFinder();
     * 
     *   int headId = ParseHelper.getHeadWordPosition(c, headFinder);
     * 
     *   // now we can do other things with the headId.
     *   String headWord = WordHelpers.getWord(ta, headId);
     * </pre>
     *
     * @param parseViewName The name of the view which contains the parse trees
     * @param c The constituent whose head we wish to find
     * @param headFinder The head finder
     * @return The index of the head word..
     */
    public static int getHeadWordPosition(Constituent c, HeadFinderBase headFinder,
            String parseViewName) throws Exception {
        TextAnnotation ta = c.getTextAnnotation();

        TreeView parse = (TreeView) ta.getView(parseViewName);

        Constituent parsePhrase = parse.getParsePhrase(c);

        return headFinder.getHeadWordPosition(parsePhrase);
    }

    /**
     * Primarily a fix for prepSRL objects; converts them from single head words to constituents.
     * E.g. for the sentence "the man with the telescope", the object of the preposition will be
     * "the telescope" instead of just "telescope".
     *
     * @param predicate The predicate of the construction (e.g. "with")
     * @param argHead The head-word of the argument of the construction (e.g. "telescope")
     * @param parseViewName The name of the parse view used to extract the phrase-structure tree
     * @return The full constituent phrase containing the argument head
     */
    public static Constituent getPhraseFromHead(Constituent predicate, Constituent argHead,
            String parseViewName) {
        // Get the path from the argument to the preposition
        // but only if the predicate node "m-commands" the arg
        TextAnnotation ta = argHead.getTextAnnotation();
        int sentenceOffset = ta.getSentence(ta.getSentenceId(argHead)).getStartSpan();
        int argStart = argHead.getStartSpan() - sentenceOffset;

        Tree<Pair<String, IntPair>> predParentTree =
                getTokenIndexedTreeCovering(predicate, parseViewName).getParent();
        boolean found = false;
        for (Tree<Pair<String, IntPair>> s : predParentTree.getYield()) {
            if (s.getLabel().getSecond().getFirst() == argStart)
                found = true;
        }
        if (!found)
            return null;

        // Now follow the path from the argument node to get to the preposition
        Tree<Pair<String, IntPair>> argPhrase = getTokenIndexedTreeCovering(argHead, parseViewName);
        while (!checkForPredicate(argPhrase.getParent(), predicate.getStartSpan() - sentenceOffset)) {
            if (argPhrase.getParent() == null)
                break;
            argPhrase = argPhrase.getParent();
        }
        // If the phrase covering the constituent is the whole sentence then the annotation is wrong
        if (argPhrase.getParent() == null)
            return null;
        int start = predicate.getStartSpan() + 1;
        int end = start + argPhrase.getYield().size();

        return new Constituent(argHead.getLabel(), argHead.getViewName(),
                argHead.getTextAnnotation(), start, end);
    }

    private static Tree<Pair<String, IntPair>> getTokenIndexedTreeCovering(Constituent predicate,
            String parseViewName) {
        TextAnnotation ta = predicate.getTextAnnotation();
        int sentenceId = ta.getSentenceId(predicate);

        Tree<String> tree = getParseTree(parseViewName, ta, sentenceId);

        int sentenceStartSpan = ta.getSentence(sentenceId).getStartSpan();
        int start = predicate.getStartSpan() - sentenceStartSpan;
        int end = predicate.getEndSpan() - sentenceStartSpan;

        return getTokenIndexedTreeCovering(tree, start, end);
    }

    private static boolean checkForPredicate(Tree<Pair<String, IntPair>> tree, int predicateIndex) {
        if (tree == null)
            return false;
        // Does the left-most leaf of the tree match the predicate?
        return tree.getYield().get(0).getLabel().getSecond().getFirst() == predicateIndex;
    }
}
