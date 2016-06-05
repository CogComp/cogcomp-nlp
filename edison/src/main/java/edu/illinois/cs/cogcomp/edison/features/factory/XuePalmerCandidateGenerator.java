package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/**
 * Ported from {@code illinois-srl}
 *
 * @author Vivek Srikumar
 * @author Christos Christodoulopoulos
 */
public class XuePalmerCandidateGenerator {
    private static final Logger log = LoggerFactory.getLogger(XuePalmerCandidateGenerator.class);

    private static final String viewName = "XuePalmerHeuristicView";

    public static List<Constituent> generateCandidates(Constituent predicate, Tree<String> tree){
        Constituent predicateClone = predicate.cloneForNewView(viewName);
        TextAnnotation ta = predicateClone.getTextAnnotation();
        int sentenceId = ta.getSentenceId(predicateClone);
        Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);
        int sentenceStart = ta.getSentence(sentenceId).getStartSpan();
        int predicatePosition = predicateClone.getStartSpan() - sentenceStart;
        HashSet<Constituent> out = new HashSet<>();
        List<Tree<Pair<String, IntPair>>> yield = spanLabeledTree.getYield();
        if(predicatePosition >= yield.size()) {
            log.error("Predicate position ({}) greater than the yield of the tree ({})", predicatePosition, yield.size());
            throw new RuntimeException();
        }

        Tree<Pair<String, IntPair>> predicateTree = yield.get(predicatePosition);

        Tree<Pair<String, IntPair>> currentNode = predicateTree.getParent();

        boolean done = false;
        while (!done) {
            if (currentNode.isRoot())
                done = true;
            else {
                List<Constituent> candidates = new ArrayList<>();

                for (Tree<Pair<String, IntPair>> sibling : currentNode.getParent().getChildren()) {
                    Pair<String, IntPair> siblingNode = sibling.getLabel();

                    // do not take the predicate as the argument
                    IntPair siblingSpan = siblingNode.getSecond();
                    if (siblingSpan.equals(predicateClone.getSpan()))
                        continue;

                    // do not take any constituent including the predicate as an argument
                    if ((predicatePosition >= siblingSpan.getFirst())
                            && (predicateClone.getEndSpan() <= siblingSpan.getSecond()))
                        continue;

                    String siblingLabel = siblingNode.getFirst();

                    int start = siblingSpan.getFirst() + sentenceStart;
                    int end = siblingSpan.getSecond() + sentenceStart;

                    candidates.add(getNewConstituent(ta, predicateClone, start, end));

                    if (siblingLabel.startsWith("PP")) {
                        for (Tree<Pair<String, IntPair>> child : sibling.getChildren()) {
                            int candidateStart = child.getLabel().getSecond().getFirst() + sentenceStart;
                            int candidateEnd = child.getLabel().getSecond().getSecond() + sentenceStart;
                            candidates.add(getNewConstituent(ta, predicateClone, candidateStart, candidateEnd));
                        }
                    }
                }
                out.addAll(candidates);
                currentNode = currentNode.getParent();
            }
        }

        // Punctuations maketh an argument not!
        List<Constituent> output = new ArrayList<>();
        for (Constituent c : out) {
            if (!ParseTreeProperties.isPunctuationToken(c.getSurfaceForm()))
                output.add(c);
        }

        if (log.isDebugEnabled()) {
            Exception ex = new Exception();
            String callerClass = ex.getStackTrace()[1].getClassName();
            String callerMethod = ex.getStackTrace()[1].getMethodName();
            int lineNumber = ex.getStackTrace()[1].getLineNumber();
            String caller = callerClass + "." + callerMethod + ":" + lineNumber;

            log.debug("Candidates for {} from heuristic: {}. Call from {}",
                    new String[] { predicateClone.toString(), output.toString(), caller });
        }

        return output;
    }

    private static Constituent getNewConstituent(TextAnnotation ta, Constituent predicate, int start, int end) {
        Constituent newConstituent = new Constituent("", 1.0, viewName, ta, start, end);
        new Relation("ChildOf", predicate, newConstituent, 1.0);
        return newConstituent;
    }
}
