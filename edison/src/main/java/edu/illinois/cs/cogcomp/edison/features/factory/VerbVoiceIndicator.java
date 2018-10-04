/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.ParseHelper;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordLists;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Uses the parse tree to figure out whether a verb is an active or a passive voice. If the word is
 * not a verb or not in a VP, then the word is marked as having "unknown" voice.
 * <p>
 * A predicate is either active or passive. We use a simple rule to determine this: if the verb
 * follows a "to-be" verb in the same verb phrase and its POS tag is "VBN", then the predicate is
 * passive. The verb may also be nested in other verb phrases that are themselves nested in the verb
 * phrase containing the "to-be" verb, so long as the only phrases found in between the "to-be" verb
 * and the target verb contain the target verb.
 *
 * @author Vivek Srikumar, Gourab Kundu
 */
public class VerbVoiceIndicator extends WordFeatureExtractor {

    private static final String ACTIVE = "A";
    private static final String PASSIVE = "P";
    private static final String UNKNOWN = "X";
    public static VerbVoiceIndicator CHARNIAK = new VerbVoiceIndicator(ViewNames.PARSE_CHARNIAK);
    public static VerbVoiceIndicator STANFORD = new VerbVoiceIndicator(ViewNames.PARSE_STANFORD);

    private final String parseViewName;

    public VerbVoiceIndicator(String parseViewName) {
        this.parseViewName = parseViewName;
    }

    @Override
    public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
        Sentence sentence = ta.getSentenceFromToken(wordPosition);
        int sentenceStart = sentence.getStartSpan();

        int predicatePosition = wordPosition - sentenceStart;

        Tree<String> tree = ParseHelper.getParseTree(parseViewName, sentence);
        Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);

        Tree<Pair<String, IntPair>> currentNode =
                spanLabeledTree.getYield().get(predicatePosition).getParent();

        String f = getVoice(currentNode);

        return new LinkedHashSet<Feature>(Collections.singletonList(DiscreteFeature.create(f)));
    }

    private String getVoice(Tree<Pair<String, IntPair>> currentNode) {
        String pos = currentNode.getLabel().getFirst();

        if (!POSUtils.isPOSVerb(pos))
            return UNKNOWN;

        if (pos.equals("VBN")) {
            Tree<Pair<String, IntPair>> parent = currentNode.getParent();
            Tree<Pair<String, IntPair>> sentinel = currentNode;

            while ((parent != null) && !(parent.getLabel().getFirst().equals("VP"))) {
                sentinel = parent;
                parent = parent.getParent();
            }

            if (parent == null) {
                return UNKNOWN;
            }

            while (parent != null && parent.getLabel().getFirst().equals("VP")) {
                Vector<Tree<Pair<String, IntPair>>> children = new Vector<>();
                for (int i = 0; i < sentinel.getPositionAmongParentsChildren(); i++)
                    children.addElement(parent.getChild(i));

                for (int i = children.size() - 1; i >= 0; --i) {
                    if (children.elementAt(i).getChild(0).isLeaf()) {
                        if (WordLists.toBeVerbs.contains(children.elementAt(i).getChild(0)
                                .getLabel().getFirst()))
                            return PASSIVE;
                    }
                }
                sentinel = parent;
                parent = parent.getParent();
            }
        }

        return ACTIVE;
    }

    @Override
    public String getName() {
        return "#vb-voice" + parseViewName + "#";
    }
}
