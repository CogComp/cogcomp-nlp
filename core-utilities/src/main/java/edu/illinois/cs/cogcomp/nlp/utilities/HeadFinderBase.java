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

import java.util.List;

/**
 * This is mainly an abstract class for the Collins' head finder. It allows for head information to
 * be found in other ways, though.
 * <p>
 * This file is based on the similar file from the Stanford NLP code, rewritten to talk nicely to
 * the other parts of this package.
 *
 * @author Vivek Srikumar
 */
public abstract class HeadFinderBase {

    protected static String getChildLabel(Constituent tree, int headCandidate) {
        Constituent child = tree.getOutgoingRelations().get(headCandidate).getTarget();
        return child.getLabel();
    }

    public enum HeadSearchDirection {
        Left {
            public int getHeadChildId(Constituent tree, String[] info, boolean getDefault) {
                int headId = 0;
                boolean foundHead = false;
                // traverse from left to right
                for (String anInfo : info) {
                    for (int headCandidate = 0; headCandidate < tree.getOutgoingRelations().size(); headCandidate++) {
                        String candidateLabel = getChildLabel(tree, headCandidate);
                        candidateLabel = ParseUtils.stripFunctionTags(candidateLabel);

                        if (anInfo.equals(candidateLabel)) {
                            headId = headCandidate;
                            foundHead = true;
                            break;
                        }
                    }
                    if (foundHead)
                        break;

                }// end outer for

                if (!foundHead) {
                    if (getDefault)
                        headId = 0;
                    else
                        headId = -1;
                }

                return headId;
            }

        },
        Right {
            public int getHeadChildId(Constituent tree, String[] info, boolean getDefault) {

                int headId = 0;
                boolean foundHead = false;
                // traverse from left to right
                for (String anInfo : info) {
                    for (int headCandidate = tree.getOutgoingRelations().size() - 1; headCandidate >= 0; headCandidate--) {
                        String candidateLabel = getChildLabel(tree, headCandidate);
                        candidateLabel = ParseUtils.stripFunctionTags(candidateLabel);

                        if (anInfo.equals(candidateLabel)) {
                            headId = headCandidate;
                            foundHead = true;
                            break;
                        }
                    }
                    if (foundHead)
                        break;

                }// end outer for

                if (!foundHead) {
                    if (getDefault)
                        headId = tree.getOutgoingRelations().size() - 1;
                    else
                        headId = -1;
                }

                return headId;
            }

        },
        LeftDis {
            public int getHeadChildId(Constituent tree, String[] info, boolean getDefault) {
                int headId = 0;
                boolean foundHead = false;
                // traverse from left to right

                for (int headCandidate = 0; headCandidate < tree.getOutgoingRelations().size(); headCandidate++) {
                    String candidateLabel = getChildLabel(tree, headCandidate);
                    candidateLabel = ParseUtils.stripFunctionTags(candidateLabel);

                    for (String anInfo : info) {
                        if (anInfo.equals(candidateLabel)) {
                            headId = headCandidate;
                            foundHead = true;
                            break;
                        }
                    }
                    if (foundHead)
                        break;

                }// end outer for

                if (!foundHead) {
                    if (getDefault)
                        headId = 0;
                    else
                        headId = -1;
                }

                return headId;
            }

        },
        RightDis {
            public int getHeadChildId(Constituent tree, String[] info, boolean getDefault) {
                int headId = 0;
                boolean foundHead = false;
                // traverse from left to right

                for (int headCandidate = tree.getOutgoingRelations().size() - 1; headCandidate >= 0; headCandidate--) {
                    String candidateLabel = getChildLabel(tree, headCandidate);
                    candidateLabel = ParseUtils.stripFunctionTags(candidateLabel);

                    for (String anInfo : info) {
                        if (anInfo.equals(candidateLabel)) {
                            headId = headCandidate;
                            foundHead = true;
                            break;
                        }
                    }
                    if (foundHead)
                        break;

                }// end outer for

                if (!foundHead) {
                    if (getDefault)
                        headId = tree.getOutgoingRelations().size() - 1;
                    else
                        headId = -1;
                }

                return headId;
            }

        };

        abstract int getHeadChildId(Constituent parseNode, String[] info, boolean getDefault);
    }

    protected Pair<HeadSearchDirection, String[]> defaultRule = new Pair<>(
            HeadSearchDirection.Left, new String[] {"S"});

    /**
     * Get the head node of a constituent belonging to a parse tree. The input constituent *must* be
     * from a parse tree view.
     *
     * @param parseNode A node from the parse tree view
     * @return The head child of the input node from the parse tree
     */
    public Constituent getHeadChild(Constituent parseNode) {

        Constituent predefinedHead = getPredefinedHead(parseNode);
        if (predefinedHead != null)
            return predefinedHead;

        // if it is the leaf, then return it.
        if (parseNode.getOutgoingRelations().size() == 0)
            return parseNode;

        // if only one child then easy
        if (parseNode.getOutgoingRelations().size() == 1)
            return parseNode.getOutgoingRelations().get(0).getTarget();

        // now it is not so easy. Do the thing with the head information.
        return findHead(parseNode);
    }

    public Constituent getHeadWord(Constituent tree) {
        while (tree.getOutgoingRelations().size() > 0) {
            tree = getHeadChild(tree);
        }
        return tree;
    }

    public int getHeadWordPosition(Constituent subTree) {
        return getHeadWord(subTree).getStartSpan();
    }

    protected Constituent getPredefinedHead(Constituent treeNode) {
        return null;
    }

    protected Constituent findHead(Constituent parseNode) {
        String label = parseNode.getLabel();

        label = ParseUtils.stripFunctionTags(label);

        List<Pair<HeadSearchDirection, String[]>> headInfo = getNonterminalHeadInformation(label);

        if (headInfo == null) {
            // Use default rule.
            if (defaultRule == null)
                return null;
            else
                return findHead(parseNode, defaultRule, true);
        }

        for (int i = 0; i < headInfo.size(); i++) {
            boolean getDefault = i == headInfo.size() - 1;

            Constituent head = findHead(parseNode, headInfo.get(i), getDefault);

            if (head != null)
                return head;
        }

        return null;
    }

    /**
     * This is the meat of the headfinder. Based on the head search direction, find the head.
     */
    private Constituent findHead(Constituent parseNode, Pair<HeadSearchDirection, String[]> rule,
            boolean getDefaultHeadChild) {

        int headChild =
                rule.getFirst().getHeadChildId(parseNode, rule.getSecond(), getDefaultHeadChild);

        if (headChild == -1)
            return null;

        headChild = fixHeadChildHacks(headChild, parseNode);

        return parseNode.getOutgoingRelations().get(headChild).getTarget();
    }

    protected int fixHeadChildHacks(int headChildId, Constituent node) {
        return headChildId;
    }

    /**
     * Return information about how the head is to be computed, given the non-terminal.
     * <p>
     * NOTE: If no rule is specified, return null.
     */
    public abstract List<Pair<HeadSearchDirection, String[]>> getNonterminalHeadInformation(
            String nonTerminal);

}
