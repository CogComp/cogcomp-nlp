/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.core;

import edu.illinois.cs.cogcomp.core.datastructures.Option;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.annotators.WordNetPlusLemmaViewGenerator;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A predicate detector implemented by heuristics.
 * 
 * @author Vivek Srikumar
 * 
 */
public class PredicateDetector {

    private final SenseManager manager;

    public PredicateDetector(SenseManager manager) {
        this.manager = manager;
    }

    public Option<String> getLemma(TextAnnotation ta, int tokenId) {
        String pos = WordHelpers.getPOS(ta, tokenId);
        String token = ta.getToken(tokenId).toLowerCase();
        String lemma = WordHelpers.getLemma(ta, tokenId);

        boolean predicate = false;

        // any token that is a verb is a predicate
        if (POSUtils.isPOSVerb(pos) && !pos.equals("AUX")) {
            if (token.equals("'s") || token.equals("'re") || token.equals("'m"))
                lemma = "be";
            else if (token.equals("'d") || lemma.equals("wo") || lemma.equals("'ll"))
                lemma = "xmodal";

            predicate = !(lemma.equals("xmodal") || pos.equals("MD") || token.equals("'ve"));

            // ignore all instances of has + "to be" if they are followed by a
            // verb or if the token is "be" followed by a verb

            boolean doVerb = lemma.equals("do");
            boolean be = lemma.equals("be");
            boolean have = lemma.equals("have");

            if (tokenId < ta.size() - 1) {

                if (be) {
                    SpanLabelView chunk = (SpanLabelView) ta.getView(ViewNames.SHALLOW_PARSE);
                    for (Constituent c : chunk.getConstituentsCoveringToken(tokenId)) {
                        // if the token under consideration is not the last
                        // token, then there is another verb here
                        if (c.getEndSpan() - 1 != tokenId) {
                            predicate = false;
                            break;
                        }
                    }
                }

                // ignore "have + be"
                if (have && WordHelpers.getLemma(ta, tokenId + 1).equals("be")) {
                    predicate = false;
                }

                // ignore "have/do + verb"
                if ((have || doVerb) && POSUtils.isPOSVerb(WordHelpers.getPOS(ta, tokenId + 1)))
                    predicate = false;

                // for some reason "according" in 'according to' is tagged as a
                // verb. we want to avoid this.

                if (token.equals("according")
                        && ta.getToken(tokenId + 1).toLowerCase().equals("to"))
                    predicate = false;
            }

            if (tokenId < ta.size() - 2) {
                // ignore don't + V or haven't + V
                if (doVerb || have) {
                    String nextToken = ta.getToken(tokenId + 1).toLowerCase();

                    if ((nextToken.equals("n't") || nextToken.equals("not"))
                            && POSUtils.isPOSVerb(WordHelpers.getPOS(ta, tokenId + 2)))
                        predicate = false;
                }
            }
        } else if (token.startsWith("re-")) {
            String trim = token.replace("re-", "");
            predicate = WordNetPlusLemmaViewGenerator.lemmaDict.contains(trim);
        }

        if (predicate) {
            return new Option<>(lemma);
        } else {
            return Option.empty();
        }
    }

    public List<Constituent> getPredicates(TextAnnotation ta) throws Exception {
        List<Constituent> list = new ArrayList<>();

        for (int i = 0; i < ta.size(); i++) {
            Option<String> opt = getLemma(ta, i);

            if (opt.isPresent()) {
                Constituent c = new Constituent("", "", ta, i, i + 1);
                c.addAttribute(PredicateArgumentView.LemmaIdentifier, opt.get());
                list.add(c);
            }
        }
        return list;
    }

    public SenseManager getManager() {
        return manager;
    }
}
