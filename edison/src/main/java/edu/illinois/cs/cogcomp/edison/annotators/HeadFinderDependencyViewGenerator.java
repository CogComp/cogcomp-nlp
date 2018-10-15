/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadDependencyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class HeadFinderDependencyViewGenerator extends Annotator {
    private final static Logger logger = LoggerFactory
            .getLogger(HeadFinderDependencyViewGenerator.class);
    static String viewGenerator = "HeadFinderDependencies";
    private final String parseViewName;

    public HeadFinderDependencyViewGenerator(String parseViewName) {
        super(buildViewName(parseViewName), new String[] {parseViewName});
        this.parseViewName = parseViewName;

    }

    public static TreeView getDependencyTree(TextAnnotation input, String parseViewName,
            String dependencyViewName) {
        CollinsHeadDependencyParser depParser = new CollinsHeadDependencyParser(false);

        TreeView parseTreeView = (TreeView) input.getView(parseViewName);

        TreeView depTreeView = new TreeView(dependencyViewName, viewGenerator, input, 1d);

        int size = 0;

        for (int i = 0; i < input.getNumberOfSentences(); i++) {
            if (parseTreeView.getTree(i) != null) {

                Constituent parseTreeRoot = parseTreeView.getRootConstituent(i);
                Tree<Pair<String, Integer>> labeledDependencyTree =
                        depParser.getLabeledDependencyTree(parseTreeRoot);

                try {
                    depTreeView.setDependencyTree(i, labeledDependencyTree);

                } catch (IllegalStateException e) {
                    System.err.println(parseTreeView);
                    System.err.println("Unlabeled dependency tree (for debugging): ");
                    System.err.println(depParser.getDependencyTree(parseTreeRoot));

                    throw e;
                }

                size += input.getSentence(i).size();
                int nConstituents = depTreeView.getNumberOfConstituents();
                if (nConstituents != size) {

                    logger.error("{} nodes in dependency tree, " + "{} tokens in text so far",
                            nConstituents, size);

                    Set<Integer> set = new LinkedHashSet<>();
                    for (int tokenId = 0; tokenId < size; tokenId++) {
                        set.add(tokenId);
                    }

                    for (Constituent c : depTreeView.getConstituents()) {
                        set.remove(c.getStartSpan());
                    }

                    StringBuilder sb = new StringBuilder();
                    for (int tokenId : set) {
                        sb.append(input.getToken(tokenId)).append(" ");
                    }
                    logger.error("Dependency tree does not cover tokens: {}", sb.toString());

                }
            }
        }
        return depTreeView;
    }

    private static String buildViewName(String parseViewName) {
        return ViewNames.DEPENDENCY_HEADFINDER + ":" + parseViewName;
    }

    /**
     * noop.
     *
     * @param rm configuration parameters
     */
    @Override
    public void initialize(ResourceManager rm) {}

    @Override
    public void addView(TextAnnotation ta) {
        ta.addView(getViewName(), getDependencyTree(ta, parseViewName, getViewName()));
    }

    @Override
    public String getViewName() {
        return buildViewName(parseViewName);
    }
}
