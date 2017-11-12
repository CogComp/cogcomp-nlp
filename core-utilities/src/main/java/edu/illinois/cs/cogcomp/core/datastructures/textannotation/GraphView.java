/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 * <p>
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This view represents a graph structure. It extends {@link View} by providing functions related to general graphs.
 *
 * @author Daniel Khashabi
 */
public class GraphView extends View {
    private static final String PARENT_OF_STRING = "ParentOf";
    private static final long serialVersionUID = 7902172271434061397L;
    private static Logger logger = LoggerFactory.getLogger(GraphView.class);

    private TIntObjectHashMap<Constituent> roots;

    Map<Pair<Integer, Integer>, Integer> nodesDistances = new HashMap();

    Map<Constituent, Integer> nodesIdToConstituents = new HashMap<>();

    /**
     * Create a new GraphView with default {@link #viewGenerator} and {@link #score}.
     */
    public GraphView(String viewName, TextAnnotation text) {
        this(viewName, viewName + "-annotator", text, 1.0);
    }

    public GraphView(String viewName, String viewGenerator, TextAnnotation text, double score) {
        super(viewName, viewGenerator, text, score);
    }

    /**
     * Checks if a constituent is a root node of graph. It is assumed that the input constituent is
     * a member of a GraphView.
     */
    public static boolean isRoot(Constituent c) {
        return c.getIncomingRelations().size() == 0;
    }

    /**
     * Checks if a constituent is a leaf of the graph. It is assumed that the input constituent is a
     * member of a GraphView.
     */
    public static boolean isLeaf(Constituent c) {
        return c.getOutgoingRelations().size() == 0;
    }

    /**
     * Gets the parent of a constituent. It is assumed that the input constiutent is a member of a
     * GraphView.
     */
    public static Constituent getParent(Constituent constituent) {
        return constituent.getIncomingRelations().get(0).getSource();
    }

    /**
     * Gets the root constituent of the graph for the given sentence
     */
    public Constituent getRootConstituent(int sentenceId) {
        findRoots();
        return this.roots.get(sentenceId);
    }

    /**
     * Gets the root constituent of the graph for the given sentence
     */
    public Constituent getRootConstituent(Sentence sentence) {
        if (this.roots == null) {
            findRoots();
        }

        return this.roots.get(this.getTextAnnotation().getSentenceId(
                sentence.getSentenceConstituent()));
    }

    private void findRoots() {
        if (roots == null)
            roots = new TIntObjectHashMap<>();
        for (int i = 0; i < this.textAnnotation.sentences().size(); ++i)
            if (null == roots.get(i))
                roots.put(i, getGraphRoot(this.textAnnotation.getSentence(i)));
    }


    /**
     * Get the root constituent of the tree that covers a sentence
     */
    public Constituent getGraphRoot(int sentenceId) {
        return this.getGraphRoot(this.getTextAnnotation().getSentence(sentenceId));
    }

    /**
     * Get the root constituent of the tree that covers a sentence
     */
    public Constituent getGraphRoot(Sentence s) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getTextAnnotation().getNumberOfSentences(); i++) {
            //if (this.trees.get(i) != null)
            //    sb.append(this.getTree(i).toString()).append("\n");
        }
        return sb.toString();
    }

    private int getSentenceStart(int sentenceId) {
        Sentence sentence = this.getTextAnnotation().getSentence(sentenceId);
        return sentence.getStartSpan();
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

    // reverse index mapping constituents to their index
    public void setNodeReverseIndices() {
        for(int i = 0; i < this.getConstituents().size(); i++) {
            nodesIdToConstituents.put(this.getConstituents().get(i), i);
        }
    }

    /**
     * Compute the distances between all pairs of nodes in the graph.
     * The results are stored into the field nodesDistancesMap (an hash map).
     */
    public void computeNodeDistances() {
        setNodeReverseIndices();
        int nnodes = this.getNumberOfConstituents();
        int[] tmpNodeDistances = new int[nnodes];
        ArrayList<Integer> stack = new ArrayList<>();
        int j, arrayPosition, depth;
        for (int i = 0; i < nnodes; i++) {
            for (j = 0; j < nnodes; j++) {
                tmpNodeDistances[j] = Integer.MAX_VALUE;
            }
            stack.add(i);
            depth = 0;
            while (stack.size() > 0) {
                j = stack.remove(0);
                depth++;
                for (Relation v : this.constituents.get(j).getOutgoingRelations()) {
                    Constituent c = v.getTarget();
                    arrayPosition = nodesIdToConstituents.get(c);
                    if (depth < tmpNodeDistances[arrayPosition]) {
                        tmpNodeDistances[arrayPosition] = depth;
                        stack.add(arrayPosition);
                        //System.out.println("SP: adding pair (" + i + ", " + arrayPosition + ") -> " + depth);
                    }
                }
            }
            for (j = 0; j < nnodes; j++) {
                depth = tmpNodeDistances[j];
                if (depth < Integer.MAX_VALUE) {
                    nodesDistances.put(new Pair(i, j), tmpNodeDistances[j]);
                }
            }
        }
    }

    public int getNodeDistance(int i, int j) {
        if(!nodesDistances.containsKey(new Pair(i, j)) )
            computeNodeDistances();
        return nodesDistances.get(new Pair(i, j));
    }

    public int getNodeDistance(Constituent c1, Constituent c2) {
        return getNodeDistance(nodesIdToConstituents.get(c1), nodesIdToConstituents.get(c2));
    }

}
