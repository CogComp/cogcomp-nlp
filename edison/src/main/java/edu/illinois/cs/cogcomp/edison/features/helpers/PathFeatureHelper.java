/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PathFeatureHelper {

    /**
     * This string indicates a path going up a parse tree in the path string.
     */
    public final static String PATH_UP_STRING = "^";

    /**
     * This string indicates a path going down a parse tree in the path string.
     */
    public final static String PATH_DOWN_STRING = "v";

    /**
     * Gets the path (that is, a sequence of constituents) from the given node to the root node. If
     * the path is longer than maxDepth, then it is truncated.
     * <p>
     * Assumptions:
     * <ol>
     * <li>This function follow traces the path from the given node along the <i>first</i> incoming
     * edge. This is okay if we are dealing with trees.</li>
     * <li>The "root" node is defined as a node which does not have any incoming edges. In Edison
     * terms, it is a constituent that does not have any incoming relations.</li>
     * </ol>
     *
     * @return A list of {@link Constituent}s, where the first element is the one that is given to
     *         the function and the last element is the root.
     */
    public static List<Constituent> getPathToRoot(Constituent node, int maxDepth) {
        List<Constituent> path = new ArrayList<>();

        int depth = 0;
        Constituent t = node;
        while (t.getIncomingRelations().size() > 0) {
            path.add(t);
            t = t.getIncomingRelations().get(0).getSource();

            depth++;
            if (depth > maxDepth) {
                break;
            }
        }

        path.add(t);

        return path;
    }

    /**
     * Get the paths from two constituents to their common ancestor. Each path is truncated to a
     * length of maxDepth if it is longer than that.
     * <p>
     * <b>Note:</b> This function requires the two constituents to be from the same {@link View}.
     *
     * @throws IllegalArgumentException If no common ancestor is found.
     * @see edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper#getPathToRoot(Constituent,
     *      int)
     */
    public static Pair<List<Constituent>, List<Constituent>> getPathsToCommonAncestor(
            Constituent start, Constituent end, int maxDepth) {

        assert start.getView() == end.getView() : "Cannot find paths across different views. "
                + "The start and end constituents should be from the same view.";

        List<Constituent> p1 = getPathToRoot(start, maxDepth);
        List<Constituent> p2 = getPathToRoot(end, maxDepth);

        Set<Constituent> s1 = new LinkedHashSet<>(p1);
        Set<Constituent> s2 = new LinkedHashSet<>(p2);

        boolean foundAncestor = false;
        List<Constituent> pathUp = new ArrayList<>();

        for (Constituent aP1 : p1) {
            if (!foundAncestor) {
                pathUp.add(aP1);
            }
            if (s2.contains(aP1)) {
                foundAncestor = true;
                break;
            }
        }
        if (!foundAncestor)
            throw new IllegalArgumentException("Common ancestor not found in path down.");

        List<Constituent> pathDown = new ArrayList<>();
        foundAncestor = false;

        for (Constituent aP2 : p2) {
            if (!foundAncestor) {
                pathDown.add(aP2);
            }
            if (s1.contains(aP2)) {
                foundAncestor = true;
                break;
            }
        }

        if (!foundAncestor)
            throw new IllegalArgumentException("Common ancestor not found in path up.");

        return new Pair<>(pathUp, pathDown);
    }

    /**
     * Returns the common ancestor of the two constituent.
     * <p>
     * It internally uses
     * {@link edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper#getPathsToCommonAncestor(Constituent, Constituent, int)}
     * . So, the restrictions related to that function apply here too.
     *
     */
    public static Constituent getCommonAncestor(Constituent start, Constituent end, int maxDepth) {
        Pair<List<Constituent>, List<Constituent>> paths =
                getPathsToCommonAncestor(start, end, maxDepth);

        return paths.getFirst().get(paths.getFirst().size() - 1);
    }

    /**
     * Returns the path from start to end as a String. The two boolean parameters
     * (includeConstituentLabel and includeRelationlabel) specify if the node and edge labels should
     * be in the string representation. At least one of them should be true. Othwewise, the function
     * throws an IllegalArgumentException.
     * <p>
     * It internally uses
     * {@link edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper#getPathsToCommonAncestor(Constituent, Constituent, int)}
     * . So, the restrictions related to that function apply here too.
     */
    public static String getPathString(Constituent start, Constituent end, int maxDepth,
            boolean includeConstituentLabel, boolean includeRelationLabel) {

        if (!includeConstituentLabel && !includeRelationLabel)
            throw new IllegalArgumentException("Path string should include at least one of "
                    + "constituent labels and relation labels");

        Pair<List<Constituent>, List<Constituent>> paths =
                getPathsToCommonAncestor(start, end, maxDepth);

        return getPathString(paths, includeConstituentLabel, includeRelationLabel);

    }

    public static String getPathString(Pair<List<Constituent>, List<Constituent>> paths,
            boolean includeConstituentLabel, boolean includeRelationLabel) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paths.getFirst().size() - 1; i++) {

            Constituent c = paths.getFirst().get(i);
            if (includeConstituentLabel)
                sb.append(c.getLabel());

            if (includeRelationLabel)
                sb.append(":").append(c.getIncomingRelations().get(0).getRelationName());

            sb.append(PATH_UP_STRING);
        }

        Constituent top = paths.getFirst().get(paths.getFirst().size() - 1);

        if (includeConstituentLabel)
            sb.append(top.getLabel());

        if (includeRelationLabel)
            sb.append("ROOT");

        if (paths.getSecond().size() > 1) {
            for (int i = paths.getSecond().size() - 2; i >= 0; i--) {
                Constituent c = paths.getSecond().get(i);

                sb.append(PATH_DOWN_STRING);

                if (includeConstituentLabel)
                    sb.append(c.getLabel());

                if (includeRelationLabel)
                    sb.append(":").append(c.getIncomingRelations().get(0).getRelationName());

            }
        }

        return sb.toString();
    }

    public static List<Constituent> getPathConstituents(Constituent start, Constituent end,
            int maxDepth) {

        Pair<List<Constituent>, List<Constituent>> paths =
                getPathsToCommonAncestor(start, end, maxDepth);
        List<Constituent> list = new ArrayList<>();

        for (int i = 0; i < paths.getFirst().size() - 1; i++) {
            list.add(paths.getFirst().get(i));
        }

        Constituent top = paths.getFirst().get(paths.getFirst().size() - 1);
        list.add(top);

        for (int i = paths.getSecond().size() - 2; i >= 0; i--) {
            Constituent c = paths.getSecond().get(i);
            list.add(c);
        }

        return list;
    }

    /**
     * Get the dependency path between two Constituents as a String. Both the start and end
     * constituents should be in a dependency view.
     */
    public static String getDependencyPathString(Constituent startDependencyConstituent,
            Constituent endDependencyCOnstituent, int maxDepth) {

        return getPathString(startDependencyConstituent, endDependencyCOnstituent, maxDepth, false,
                true);
    }

    /**
     * Get the path between Constituents belonging to a parse tree as a String.
     */
    public static String getFullParsePathString(Constituent startParseConstituent,
            Constituent endParseConstituent, int maxDepth) {

        return getPathString(startParseConstituent, endParseConstituent, maxDepth, true, false);
    }
}
