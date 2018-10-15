/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;

import java.util.*;

@SuppressWarnings("serial")
public class FeatureGenerators {

    public static FeatureExtractor hasVerb = new FeatureExtractor() {

        @Override
        public String getName() {
            return "#has-verb";
        }

        @Override
        public Set<Feature> getFeatures(Constituent c) throws EdisonException {
            boolean hasVerb = false;
            TextAnnotation ta = c.getTextAnnotation();

            for (int i = c.getStartSpan(); i < c.getEndSpan(); i++) {

                if (POSUtils.isPOSVerb(WordHelpers.getPOS(ta, i))) {
                    hasVerb = true;
                    break;
                }
            }
            Set<Feature> feats = new HashSet<>();

            if (hasVerb) {
                feats.add(DiscreteFeature.create(getName()));
            }
            return feats;
        }
    };

    public static final FeatureInputTransformer candidateBoundaryTransformer =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {
                    TextAnnotation ta = input.getTextAnnotation();

                    Constituent ce =
                            new Constituent("", "", ta, input.getEndSpan() - 1, input.getEndSpan());

                    Constituent cs =
                            new Constituent("", "", ta, input.getStartSpan(),
                                    input.getStartSpan() + 1);

                    new Relation("", cs, ce, 0);

                    return Collections.singletonList(ce);

                }

                @Override
                public String name() {
                    return "#b:";
                }
            };

    public static FeatureExtractor hyphenTagFeature = new FeatureExtractor() {

        @Override
        public String getName() {
            return "#nom-hyp";
        }

        @Override
        public Set<Feature> getFeatures(Constituent c) throws EdisonException {

            Set<Feature> features = new HashSet<>();
            String surfaceString = c.getSurfaceForm();

            if (surfaceString.contains("-") && c.length() == 1) {
                Constituent predicate = c.getIncomingRelations().get(0).getSource();

                String lemma = predicate.getAttribute(PredicateArgumentView.LemmaIdentifier);

                assert lemma != null;

                if (predicate.getSpan().equals(c.getSpan())) {
                    features.add(DiscreteFeature.create("pred-token"));
                }

                String[] parts = surfaceString.split("-");

                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];

                    if (part.contains(lemma)) {
                        features.add(DiscreteFeature.create(i + ":pred"));
                    } else {
                        String lowerCase = part.toLowerCase();
                        features.add(DiscreteFeature.create(lowerCase));
                        features.add(DiscreteFeature.create(i + ":" + lowerCase));
                    }
                }
            }

            return features;
        }
    };

    public static FeatureExtractor ppFeatures(final String parseViewName) {
        return new FeatureExtractor() {

            @Override
            public String getName() {
                return "#pp-feats";
            }

            @Override
            public Set<Feature> getFeatures(Constituent c) throws EdisonException {
                TextAnnotation ta = c.getTextAnnotation();

                TreeView parse = (TreeView) ta.getView(parseViewName);

                Set<Feature> feats = new HashSet<>();
                try {
                    Constituent phrase = parse.getParsePhrase(c);
                    // if the phrase is a PP, then the head word of its
                    // rightmost NP child.

                    List<Relation> rels = phrase.getOutgoingRelations();
                    for (int i = rels.size() - 1; i >= 0; i--) {
                        Relation relation = rels.get(i);
                        if (relation == null)
                            continue;
                        Constituent target = relation.getTarget();
                        if (ParseTreeProperties.isNominal(target.getLabel())) {
                            int head = CollinsHeadFinder.getInstance().getHeadWordPosition(phrase);

                            feats.add(DiscreteFeature.create("np-head:"
                                    + ta.getToken(head).toLowerCase()));
                            feats.add(DiscreteFeature.create("np-head-pos:"
                                    + WordHelpers.getPOS(ta, head)));

                            break;
                        }
                    }

                    // if the phrase's parent is a PP, then the head of that PP.
                    Constituent parent = phrase.getIncomingRelations().get(0).getSource();

                    if (parent.getLabel().equals("PP")) {
                        int head = CollinsHeadFinder.getInstance().getHeadWordPosition(phrase);
                        feats.add(DiscreteFeature.create("p-head:"
                                + ta.getToken(head).toLowerCase()));
                    }

                } catch (EdisonException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return feats;
            }
        };
    }

    public static FeatureInputTransformer getParseLeftSibling(final String parseViewName) {
        return new FeatureInputTransformer() {

            @Override
            public List<Constituent> transform(Constituent input) {
                TextAnnotation ta = input.getTextAnnotation();

                TreeView parse = (TreeView) ta.getView(parseViewName);

                List<Constituent> siblings = new ArrayList<>();
                try {
                    Constituent phrase = parse.getParsePhrase(input);
                    List<Relation> in = phrase.getIncomingRelations();

                    if (in.size() > 0) {
                        Constituent prev = null;
                        Relation relation = in.get(0);
                        List<Relation> outgoingRelations =
                                relation.getSource().getOutgoingRelations();

                        for (Relation r : outgoingRelations) {
                            if (r.getTarget() == phrase) {
                                break;
                            }
                            prev = r.getTarget();
                        }

                        if (prev != null)
                            siblings.add(prev);
                    }

                } catch (EdisonException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return siblings;
            }

            @Override
            public String name() {
                return "#lsis";
            }
        };
    }

    public static FeatureInputTransformer getParseRightSibling(final String parseViewName) {
        return new FeatureInputTransformer() {

            @Override
            public List<Constituent> transform(Constituent input) {
                TextAnnotation ta = input.getTextAnnotation();

                TreeView parse = (TreeView) ta.getView(parseViewName);

                List<Constituent> siblings = new ArrayList<>();
                try {
                    Constituent phrase = parse.getParsePhrase(input);
                    List<Relation> in = phrase.getIncomingRelations();

                    if (in.size() > 0) {
                        List<Relation> outgoingRelations =
                                in.get(0).getSource().getOutgoingRelations();
                        int id = -1;
                        for (int i = 0; i < outgoingRelations.size(); i++) {
                            Relation r = outgoingRelations.get(i);
                            if (r.getTarget() == phrase) {
                                id = i;
                                break;
                            }
                        }

                        if (id >= 0 && id + 1 < outgoingRelations.size())
                            siblings.add(outgoingRelations.get(id + 1).getTarget());
                    }

                } catch (EdisonException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return siblings;
            }

            @Override
            public String name() {
                return "#rsis";
            }
        };
    }

}
