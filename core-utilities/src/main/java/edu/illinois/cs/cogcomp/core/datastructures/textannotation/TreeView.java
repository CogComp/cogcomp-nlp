/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This view represents a tree structure. Use this for parse trees and dependency trees. It extends
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View} by providing functions to
 * get and set {@link edu.illinois.cs.cogcomp.core.datastructures.trees.Tree} objects.
 *
 * @author Vivek Srikumar
 */
public class TreeView extends View {
    private static final String PARENT_OF_STRING = "ParentOf";
    private static final long serialVersionUID = 7902172271434061397L;
    private static Logger logger = LoggerFactory.getLogger(TreeView.class);
    protected List<Tree<String>> trees;

    protected boolean isDependencyTree;

    protected boolean firstTree;

    private TIntObjectHashMap<Constituent> roots;

    /**
     * Create a new TreeView with default {@link #viewGenerator} and {@link #score}.
     */
    public TreeView(String viewName, TextAnnotation text) {
        this(viewName, viewName + "-annotator", text, 1.0);
    }

    public TreeView(String viewName, String viewGenerator, TextAnnotation text, double score) {
        super(viewName, viewGenerator, text, score);
        firstTree = true;
    }

    /**
     * Checks if a constituent is a root node of a tree. It is assumed that the input constituent is
     * a member of a TreeView.
     */
    public static boolean isRoot(Constituent c) {
        return c.getIncomingRelations().size() == 0;
    }

    /**
     * Checks if a constituent is a leaf of a tree. It is assumed that the input constituent is a
     * member of a TreeView.
     */
    public static boolean isLeaf(Constituent c) {
        return c.getOutgoingRelations().size() == 0;
    }

    /**
     * Gets the parent of a constituent. It is assumed that the input constiutent is a member of a
     * TreeView.
     */
    public static Constituent getParent(Constituent constituent) {
        return constituent.getIncomingRelations().get(0).getSource();
    }

    protected Tree<String> buildTree(Constituent root) {
        Tree<String> t;

        if (isDependencyTree)
            t = new Tree<>(root.toString());
        else
            t = new Tree<>(root.getLabel());
        for (Relation r : root.getOutgoingRelations()) {
            if (!isDependencyTree)
                t.addSubtree(buildTree(r.getTarget()));
            else
                t.addSubtree(buildTree(r.getTarget()), r.getRelationName());
        }

        return t;
    }

    /**
     * Gets the root constituent of the tree for the given sentence
     */
    public Constituent getRootConstituent(int sentenceId) {
        findRoots();

        return this.roots.get(sentenceId);
    }

    /**
     * Gets the root constituent of the tree for the given sentence
     */
    public Constituent getRootConstituent(Sentence sentence) {
        if (this.roots == null) {
            findRoots();
        }

        return this.roots.get(this.getTextAnnotation().getSentenceId(
                sentence.getSentenceConstituent()));
    }

    /**
     * Get the {@link edu.illinois.cs.cogcomp.core.datastructures.trees.Tree} representation of the
     * tree for the given sentence.
     */
    public Tree<String> getTree(int sentenceId) {
        if (this.trees == null) {
            makeTrees();
        }
        return this.trees.get(sentenceId);
    }

    protected Tree<String> makeTree(Constituent root) {

        setDependencyTreeSwitch(root);

        return buildTree(root);

    }

    protected void setDependencyTreeSwitch(Constituent root) {
        List<Relation> rootRelations = root.getOutgoingRelations();

        isDependencyTree =
                rootRelations.size() != 0
                        && !rootRelations.get(0).getRelationName().equals(PARENT_OF_STRING);
    }

    private void findRoots() {
        if (roots == null)
            roots = new TIntObjectHashMap<>();
        for (int i = 0; i < this.textAnnotation.sentences().size(); ++i)
            if(null == roots.get(i))
                roots.put(i, getTreeRoot(this.textAnnotation.getSentence(i)));
    }

    /**
     * Makes the tree objects. This creates one tree per sentence.
     */
    protected void makeTrees() {
        safeInitializeTrees();

        for (int sentenceId = 0; sentenceId < this.getTextAnnotation().getNumberOfSentences(); sentenceId++) {
            Constituent root = this.getRootConstituent(sentenceId);

            if (root == null) {
                // throw new IllegalStateException(
                // "Unable to find the root constituent. "
                // + "Maybe the view is not restricted to a single sentence.");
                trees.set(sentenceId, null);
            } else
                trees.set(sentenceId, makeTree(root));

        }
        firstTree = false;
    }

    /**
     * Get the root constituent of the tree that covers a sentence
     */
    public Constituent getTreeRoot(int sentenceId) {
        return this.getTreeRoot(this.getTextAnnotation().getSentence(sentenceId));
    }

    /**
     * Get the root constituent of the tree that covers a sentence
     */
    public Constituent getTreeRoot(Sentence s) {
        Constituent root = null;
        Constituent sentenceConstituent = s.getSentenceConstituent();
        IQueryable<Constituent> queryable =
                this.where(Queries.containedInConstituent(sentenceConstituent));
        for (Constituent c : queryable) {
            if (c.getIncomingRelations().size() == 0) {
                root = c;
                break;
            }
        }
        return root;
    }

    /**
     * Ugly hack to initialize the trees arraylist with the correct number elements. There should be
     * as many trees as there are sentences. Later on, we can say trees.set(sentenceId, tree);
     */
    private void safeInitializeTrees() {
        if (this.trees == null) {
            trees = new ArrayList<>();
            for (int i = 0; i < this.getTextAnnotation().getNumberOfSentences(); i++) {
                trees.add(null);
            }
        }
    }

    @Override
    public String toString() {
        if (trees == null)
            makeTrees();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getTextAnnotation().getNumberOfSentences(); i++) {

            if (this.trees.get(i) != null)
                sb.append(this.getTree(i).toString()).append("\n");
        }
        return sb.toString();
    }

    private int getSentenceStart(int sentenceId) {
        Sentence sentence = this.getTextAnnotation().getSentence(sentenceId);
        return sentence.getStartSpan();
    }

    /**
     * Set the parse tree of the {@code sentenceId}<sup>th</sup> sentence.
     * <p>
     * <b>Note</b>: The same TreeView cannot contain both the parse tree and the dependency tree and
     * will throw an exception if an attempt is made to set the parse tree in a view that has a
     * dependency tree.
     */
    public void setParseTree(int sentenceId, Tree<String> tree) {
        safeInitializeTrees();

        if (firstTree) {
            firstTree = false;
            this.isDependencyTree = false;
        }

        if (this.isDependencyTree) {
            throw new IllegalArgumentException("Not expecting a dependency tree, but found " + tree);
        }

        this.trees.set(sentenceId, tree);

        int sentenceStart = getSentenceStart(sentenceId);

        Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);

        Pair<String, IntPair> rootLabel = spanLabeledTree.getLabel();
        IntPair rootSpan = rootLabel.getSecond();

        int rootStart = rootSpan.getFirst() + sentenceStart;
        int rootEnd = rootSpan.getSecond() + sentenceStart;

        Constituent root = createNewConstituent(rootStart, rootEnd, rootLabel.getFirst(), 1.0);

        this.addConstituent(root);

        addParseTree(spanLabeledTree, root, sentenceStart);
    }

    /**
     * Set the dependency tree of hte {@code sentenceId}<sup>th</sup> sentence. Each node in the
     * dependency tree specifies both the token and its position <b>with respect to the start of the
     * sentence</b>, not the entire TextAnnotation.
     * <p>
     * <b>Note</b>: The same TreeView cannot contain both the parse tree and the dependency tree and
     * will throw an exception if an attempt is made to set the dependency tree of a view that has a
     * phrase-structure tree.
     */
    public void setDependencyTree(int sentenceId, Tree<Pair<String, Integer>> depTree) {
        this.setDependencyTree(sentenceId, depTree, 0.0d);
    }

    /**
     * Set the dependency tree for a specified sentence. Each node in the dependency tree specifies
     * both the token and its position <b>with respect to the start of the sentence</b>, not the
     * entire TextAnnotation.
     * <p>
     * <b>Note</b>: The same TreeView cannot contain both the parse tree and the dependency tree and
     * will throw an exception if an attempt is made to set the dependency tree of a view that has a
     * phrase-structure tree.
     */
    public void setDependencyTree(int sentenceId, Tree<Pair<String, Integer>> depTree,
            double treeScore) {
        // safeInitializeTrees();

        if (firstTree) {
            firstTree = false;
            this.isDependencyTree = true;
        }

        if (!this.isDependencyTree) {
            throw new IllegalArgumentException("Not expecting a dependency tree, but found "
                    + depTree);
        }

        int sentenceStart = getSentenceStart(sentenceId);

        Constituent root =
                getConstituentRelativeToSentenceStart(depTree, treeScore, sentenceStart, "ROOT");

        this.addConstituent(root);

        try {
            addDependencyTree(depTree, sentenceStart, root);
        } catch (IllegalStateException e) {
            System.err.println(depTree);
            throw e;
        }
    }

    /**
     * assumes that the integer in the dependency tree label is ALWAYS token offset in the TEXT,
     * i.e. is NOT sentence-relative.
     */
    private Constituent createTreeConstituent(Tree<Pair<String, Integer>> depTree,
            double treeScore, String constituentLabel) {
        Pair<String, Integer> label = depTree.getLabel();
        int start = label.getSecond();
        int end = label.getSecond() + 1; // TODO: verify stanford at-the-end indexing!

        return createNewConstituent(start, end, constituentLabel, treeScore);
    }

    private Constituent getConstituentRelativeToSentenceStart(Tree<Pair<String, Integer>> depTree,
            double treeScore, int sentenceStart, String constituentLabel) {
        // HACK AHEAD: Sometimes, the integer in the dependency tree label is
        // the token offset in the text. Sometimes, it is the token offset in
        // the sentence. A cheap way to test for this is to verify that the
        // first part of the label is the token. If so, it is a token offset in
        // the text. Otherwise, it is with respect to the start of the sentence.
        // Shift it by the sentence start in the latter case.

        Pair<String, Integer> label = depTree.getLabel();
        int start = label.getSecond();
        int end = label.getSecond() + 1;

        String tokenInTree = label.getFirst();
        // HACKING THE HACK: If the tokenInTree is a -LRB- or a -RRB- replace it
        // with the standard '(' and ')' to match the sentence tokens
        if (tokenInTree.equals("-LRB-"))
            tokenInTree = "(";
        else if (tokenInTree.equals("-RRB-"))
            tokenInTree = ")";

        TextAnnotation ta = this.getTextAnnotation();
        if (start < ta.size()) {
            // Add a check in case the token is at the same position in multiple sentences
            // (see unit test for such a case)
            if (start < sentenceStart || !ta.getToken(start).equals(tokenInTree)) {
                start += sentenceStart;
                end += sentenceStart;
            }
        }
        return createNewConstituent(start, end, constituentLabel, treeScore);
    }

    protected void addDependencyTree(Tree<Pair<String, Integer>> depTree, int sentStart,
            Constituent parent) {
        String word = depTree.getLabel().getFirst();

        String token = this.getTextAnnotation().getToken(parent.getStartSpan());

        word = treebankTokenHacks(word);
        token = treebankTokenHacks(token);

        assert word.trim().length() > 0;
        assert token.trim().length() > 0;

        if (!word.equals(token)) {
            logger.error(parent.getTextAnnotation().toString());
            logger.error(depTree.toString());

            throw new IllegalStateException("Expecting " + token + ", found " + word
                    + " instead while constructing the dependency tree");
        }

        for (int i = 0; i < depTree.getNumberOfChildren(); i++) {
            String relationLabel = depTree.getEdgeLabel(i).getFirst();

            Tree<Pair<String, Integer>> child = depTree.getChild(i);

            Constituent childConstituent =
                    getConstituentRelativeToSentenceStart(child, 1.0, sentStart, relationLabel);

            this.addConstituent(childConstituent, true);
            this.addRelation(new Relation(relationLabel, parent, childConstituent, 1.0));

            addDependencyTree(child, sentStart, childConstituent);
        }
    }

    protected void addDependencyTreeWithHack(Tree<Pair<String, Integer>> depTree,
            Constituent parent, int sentenceStart) {
        String word = depTree.getLabel().getFirst();

        String token = this.getTextAnnotation().getToken(parent.getStartSpan());

        word = treebankTokenHacks(word);
        token = treebankTokenHacks(token);

        assert word.trim().length() > 0;
        assert token.trim().length() > 0;

        if (!word.equals(token)) {
            logger.info(parent.getTextAnnotation().toString());
            logger.info(depTree.toString());

            throw new IllegalStateException("Expecting " + token + ", found " + word
                    + " instead while constructing the dependency tree");
        }

        for (int i = 0; i < depTree.getNumberOfChildren(); i++) {
            String relationLabel = depTree.getEdgeLabel(i).getFirst();

            Tree<Pair<String, Integer>> child = depTree.getChild(i);

            Constituent childConstituent =
                    getConstituentRelativeToSentenceStart(child, 1.0, sentenceStart, relationLabel);

            this.addConstituent(childConstituent, true);
            this.addRelation(new Relation(relationLabel, parent, childConstituent, 1.0));

            addDependencyTreeWithHack(child, childConstituent, sentenceStart);
        }
    }

    /**
     * Set the parse tree of the {@code sentenceId}th sentence.
     */
    public void setScoredParseTree(int sentenceId, Tree<String> tree, Tree<Double> scores) {
        safeInitializeTrees();

        if (!this.getViewName().startsWith("PARSE")) {
            throw new IllegalStateException("Cannot set a Tree<String> object "
                    + "as the dependency tree." + " Need a Tree<String, Integer> "
                    + "to recover dependency token information. ");
        }

        this.trees.set(sentenceId, tree);

        Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);

        int sentenceStart = getSentenceStart(sentenceId);

        Pair<String, IntPair> rootLabel = spanLabeledTree.getLabel();

        IntPair rootSpan = rootLabel.getSecond();

        int rootStart = rootSpan.getFirst() + sentenceStart;
        int rootEnd = rootSpan.getSecond() + sentenceStart;

        Constituent root =
                createNewConstituent(rootStart, rootEnd, rootLabel.getFirst(), scores.getLabel());

        this.addConstituent(root);

        addScoredParseTree(spanLabeledTree, scores, root,
                this.getTextAnnotation().getSentence(sentenceId).getStartSpan());
    }

    /**
     * Transforms a scored input tree into the constituent-relation graph
     */
    protected void addScoredParseTree(Tree<Pair<String, IntPair>> spanLabeledTree,
            Tree<Double> scores, Constituent root, int sentenceStartPosition) {

        for (int childId = 0; childId < spanLabeledTree.getNumberOfChildren(); childId++) {
            Tree<Pair<String, IntPair>> child = spanLabeledTree.getChild(childId);

            String edgeLabel;
            double edgeScore = 0;

            edgeLabel = PARENT_OF_STRING;

            double constituentScore = scores.getChild(childId).getLabel();

            int start = child.getLabel().getSecond().getFirst() + sentenceStartPosition;
            int end = child.getLabel().getSecond().getSecond() + sentenceStartPosition;

            String constituentLabel = child.getLabel().getFirst();

            Constituent childConstituent;
            if (start >= end) {
                // Ignore constituents with incorrect span bounds
                logger.debug("Constituent with incorrect span found in " + root.getViewName());
            } else {
                childConstituent =
                        createNewConstituent(start, end, constituentLabel, constituentScore);

                this.addConstituent(childConstituent, true);

                this.addRelation(new Relation(edgeLabel, root, childConstituent, edgeScore));

                Tree<Double> scoresChild = scores.getChild(childId);
                this.addScoredParseTree(child, scoresChild, childConstituent, sentenceStartPosition);
            }
        }
    }

    /**
     * Transforms an unscored input tree into the constituent-relation graph
     */
    protected void addParseTree(Tree<Pair<String, IntPair>> spanLabeledTree, Constituent root,
            int sentenceStartPosition) {

        for (int childId = 0; childId < spanLabeledTree.getNumberOfChildren(); childId++) {
            Tree<Pair<String, IntPair>> child = spanLabeledTree.getChild(childId);

            String edgeLabel;
            edgeLabel = PARENT_OF_STRING;

            Pair<String, IntPair> childLabel = child.getLabel();

            IntPair childSpan = childLabel.getSecond();
            int start = childSpan.getFirst() + sentenceStartPosition;
            int end = childSpan.getSecond() + sentenceStartPosition;

            String constituentLabel = childLabel.getFirst();
            Constituent childConstituent;
            if (start >= end) {
                // Ignore constituents with incorrect span bounds
                logger.debug("Constituent with incorrect span found in " + root.getViewName());
            } else {
                childConstituent = createNewConstituent(start, end, constituentLabel, 1.0);

                if (end == start + 1 && child.getNumberOfChildren() == 0) {
                    // this is a leaf. The leaf must be a token in the sentence
                    String token = this.getTextAnnotation().getToken(start);

                    String s = constituentLabel;
                    token = treebankTokenHacks(token);
                    s = treebankTokenHacks(s);

                    if (!token.equals(s)) {
                        assert false : "Expecting token: " + token + ", found " + s + " instead.";
                    }
                }

                // because of the way that punctuation is labeled, it is possible to have duplicate label/leaf constituents.
                this.addConstituent(childConstituent, true);
                this.addRelation(new Relation(edgeLabel, root, childConstituent, 1.0));
                this.addParseTree(child, childConstituent, sentenceStartPosition);
            }
        }
    }

    /**
     * This function exists to deal with annoying data conventions in the treebank column format
     * data.
     */
    private String treebankTokenHacks(String s) {
        String token = s.replaceAll("\\\\/", "/").replaceAll("\\\\\\*", "*");
        token = ParseUtils.convertBracketsFromPTBFormat(token);
        return token;
    }

    /**
     * Makes a new constituent spanning {@code start} to {@code end} with the label
     * {@code constituentLabel} and score {@code constituentScore}.
     */
    private Constituent createNewConstituent(int start, int end, String constituentLabel,
            double constituentScore) {
        return new Constituent(constituentLabel, constituentScore, this.getViewName(),
                this.getTextAnnotation(), start, end);
    }

    /**
     * We need to adjust the tree span offsets, they are relative to the start of the sentence,
     * we will make them relative to the start of the text.
     * @param tree the label, span intpair pairs of pairs of pairs.
     * @param offset the amount to advance the spans.
     */
    private void adjustTree(Tree<Pair<String, IntPair>> tree, int offset) {
        IntPair span = tree.getLabel().getSecond();
        span.setFirst(span.getFirst()+offset);
        span.setSecond(span.getSecond()+offset);
        for (Tree<Pair<String, IntPair>> child : tree.getChildren()) {
            adjustTree(child, offset);
        }
    }
    
    /**
     * Get the constituents comprising the sentence at the provided index organized into a 
     * tree.
     * @param sentenceId the id of the sentence.
     * @return a tree comprised of constituents.
     */
    public Tree<Constituent> getConstituentTree(int sentenceId) {
        if (sentenceId >= this.trees.size())
            return null;
        // first get the parse tree containing for each label the token offsets as a pair.
        Tree<String> sentence = getTree(sentenceId);
        
        // now get the span labeled tree
        Tree<Pair<String, IntPair>> labeledSpans = ParseUtils.getSpanLabeledTree(sentence);
        
        // the offset are relative to the beginning of the tree adjust to the start of text.
        // the root constituent will contain the starting location.
        Constituent c = this.getRootConstituent(sentenceId);
        int offset = c.getStartSpan();
        
        // now adjust each of the tree pairs to the constituents offset.
        adjustTree(labeledSpans, offset);
        return getConstituentSubTree(labeledSpans);
    }

    private Tree<Constituent> getConstituentSubTree(Tree<Pair<String, IntPair>> parseTree) {
        Pair<String, IntPair> rootLabel = parseTree.getLabel();
        Constituent root = createConstituent(rootLabel);
        Tree<Constituent> constituentTree = new Tree<>(root);
        for (Tree<Pair<String, IntPair>> child : parseTree.getChildren()) {
            if (child.isLeaf())
                continue;
            constituentTree.addSubtree(getConstituentSubTree(child));
        }
        return constituentTree;
    }

    private Constituent createConstituent(Pair<String, IntPair> rootLabel) {
        IntPair span = rootLabel.getSecond();
        return createNewConstituent(span.getFirst(), span.getSecond(), rootLabel.getFirst(), 1.0);
    }

    /**
     * Finds the highest node in the parse tree that contains the input constituent. This function
     * is a useful mechanism to get a node from the parse tree. Note that several parse helper
     * functions (such as path helpers) require nodes from the parse view and this function could be
     * used to get them.
     *
     * @param c The input constituent. This could be from any view.
     */
    @SuppressWarnings({"unchecked", "serial"})
    public Constituent getParsePhrase(Constituent c) throws Exception {
        Predicate<Constituent> predicate =
                Queries.containsConstituent(c).and(new Predicate<Constituent>() {

                    @Override
                    public Boolean transform(Constituent input) {
                        return input.getOutgoingRelations().size() > 0
                                && !ParseTreeProperties.isPreTerminal(input);
                    }
                });

        List<Constituent> candidates =
                (List<Constituent>) this.where(predicate).orderBy(
                        TextAnnotationUtilities.constituentLengthComparator);

        // Find the set of all spans that have the same size as the smallest
        // one.
        List<Constituent> spans = findSmallestSpan(candidates);

        // There *must* be such span
        if (spans.size() == 0)
            throw new Exception("Cannot find any span in parse tree for `" + c + "`!\n" + this
                    + "\n");

        Constituent phrase = null;
        if (spans.size() == 1) {
            phrase = spans.iterator().next();
        } else {

            /*
             * Now, try to find the constituent among the spans that is the highest. What is the
             * highest span? If it has a child that is contained in spans, then it can't be the
             * highest. Otherwise, it must be the highest one. This is a pain and somewhat slow. On
             * the bright side, this is not frequent.
             */
            for (Constituent cc : spans) {
                boolean found = false;
                for (Relation r : cc.getOutgoingRelations()) {
                    if (!spans.contains(r.getTarget())) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    phrase = cc;
                    break;
                }
            }
        }

        // see function for details
        phrase = getParsePhraseSingeWordHack(c, phrase);

        // finally, if the label of the parse phrase is a POS tag and there is a
        // unit production into it, then go up one level.

        if (ParseTreeProperties.isPreTerminal(phrase)) {
            Constituent parent = phrase.getIncomingRelations().get(0).getSource();

            if (parent.getSpan().equals(phrase.getSpan()))
                phrase = parent;
        }

        return phrase;
    }

    @SuppressWarnings({"unchecked", "serial"})
    private Constituent getParsePhraseSingeWordHack(Constituent c, Constituent phrase) {
        List<Constituent> candidates;
        if (phrase == null || !Queries.containsConstituent(phrase).transform(c)) {
            // a hack that deals with cases when a single word constituent is
            // not found in the parse tree because it is not a phrase.
            if (c.getStartSpan() == c.getEndSpan() - 1) {
                candidates =
                        (List<Constituent>) this.where(Queries.sameSpanAsConstituent(c).and(
                                new Predicate<Constituent>() {

                                    @Override
                                    public Boolean transform(Constituent input) {
                                        return input.getOutgoingRelations().size() > 0;

                                    }
                                }));
                phrase = candidates.get(0);
            }
        }
        return phrase;
    }

    private List<Constituent> findSmallestSpan(List<Constituent> candidates) {
        List<Constituent> spans = new ArrayList<>();

        IntPair span = null;
        for (Constituent candidate : candidates) {
            boolean add = false;
            if (span == null) {
                span = candidate.getSpan();
                add = true;
            } else if (span.equals(candidate.getSpan())) {
                add = true;
            }

            if (add) {
                // Don't add POS tags and words
                if (candidate.getOutgoingRelations().size() > 0
                        && !ParseTreeProperties.isPreTerminal(candidate))
                    spans.add(candidate);

            }
        }
        return spans;
    }

}
