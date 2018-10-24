/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.edison.annotators.HeadFinderDependencyViewGenerator;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
@SuppressWarnings("serial")
public abstract class FeatureInputTransformer extends ITransformer<Constituent, List<Constituent>> {

    public static final FeatureInputTransformer identity = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent c) {

            Constituent c1 = c.cloneForNewView(c.getViewName());
            new Relation("", c, c1, 0.0);
            return Collections.singletonList(c1);
        }

        @Override
        public String name() {
            return "";
        }
    };
    public static final FeatureInputTransformer dependencyModifiers =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);
                    return getModifiers(c, dependency);
                }

                @Override
                public String name() {
                    return "dep-mod";
                }
            };
    public static final FeatureInputTransformer easyFirstDependencyModifiers =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);
                    return getModifiers(c, dependency);
                }

                @Override
                public String name() {
                    return "easy-first-mod";
                }
            };
    public static final FeatureInputTransformer stanfordDependencyModifiers =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY_STANFORD);
                    return getModifiers(c, dependency);
                }

                @Override
                public String name() {
                    return "st-dep-mod";
                }

            };
    public static final FeatureInputTransformer charniakHead = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent c) {
            TextAnnotation ta = c.getTextAnnotation();
            TreeView tree = (TreeView) ta.getView(ViewNames.PARSE_CHARNIAK);
            try {
                Constituent phrase = tree.getParsePhrase(c);
                int head = CollinsHeadFinder.getInstance().getHeadWordPosition(phrase);
                Constituent c1 = new Constituent("", "", ta, head, head + 1);

                return Collections.singletonList(addPointerToSource(c, c1));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String name() {
            return "ch-hd";
        }
    };
    public static final FeatureInputTransformer berkeleyHead = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent c) {
            TextAnnotation ta = c.getTextAnnotation();
            TreeView tree = (TreeView) ta.getView(ViewNames.PARSE_BERKELEY);
            try {
                Constituent phrase = tree.getParsePhrase(c);
                int head = CollinsHeadFinder.getInstance().getHeadWordPosition(phrase);
                Constituent c1 = new Constituent("", "", ta, head, head + 1);

                return Collections.singletonList(addPointerToSource(c, c1));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public String name() {
            return "be-hd";
        }
    };
    public static final FeatureInputTransformer dependencyHead =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);
                    return Collections.singletonList(getHead(c, dependency));
                }

                @Override
                public String name() {
                    return "dep-hd";
                }
            };
    public static final FeatureInputTransformer easyFirstDependencyHead =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);
                    return Collections.singletonList(getHead(c, dependency));
                }

                @Override
                public String name() {
                    return "easy-first-hd";
                }
            };
    public static final FeatureInputTransformer stanfordDependencyHead =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY_STANFORD);
                    return Collections.singletonList(getHead(c, dependency));
                }

                @Override
                public String name() {
                    return "st-dep-hd";
                }

            };
    public static final FeatureInputTransformer dependencyGovernor =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TreeView dependency =
                            (TreeView) input.getTextAnnotation().getView(ViewNames.DEPENDENCY);
                    return getGovernor(input, dependency);
                }

                @Override
                public String name() {
                    return "dep-gov";
                }
            };
    public static final FeatureInputTransformer easyFirstDependencyGovernor =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TreeView dependency =
                            (TreeView) input.getTextAnnotation().getView(ViewNames.DEPENDENCY);
                    return getGovernor(input, dependency);
                }

                @Override
                public String name() {
                    return "easy-first-gov";
                }
            };
    public static final FeatureInputTransformer stanfordDependencyGovernor =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TreeView dependency =
                            (TreeView) input.getTextAnnotation().getView(
                                    ViewNames.DEPENDENCY_STANFORD);

                    return getGovernor(input, dependency);
                }

                @Override
                public String name() {
                    return "st-dep-gov";
                }

            };
    public static final FeatureInputTransformer dependencyObject =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TreeView dependency =
                            (TreeView) input.getTextAnnotation().getView(ViewNames.DEPENDENCY);

                    return getObject(input, dependency, "obj");
                }

                @Override
                public String name() {
                    return "dep-obj";
                }

            };
    public static final FeatureInputTransformer easyFirstDependencyObject =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TreeView dependency =
                            (TreeView) input.getTextAnnotation().getView(ViewNames.DEPENDENCY);

                    return getObject(input, dependency, "obj");
                }

                @Override
                public String name() {
                    return "easy-first-obj";
                }

            };
    public static final FeatureInputTransformer stanfordDependencyObject =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TreeView dependency =
                            (TreeView) input.getTextAnnotation().getView(
                                    ViewNames.DEPENDENCY_STANFORD);

                    return getObject(input, dependency, "obj");
                }

                @Override
                public String name() {
                    return "st-dep-obj";
                }
            };
    public static final FeatureInputTransformer dependencyNeighboringPP =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    int tokenPosition = c.getStartSpan();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);

                    Constituent prepositionDepConstituent =
                            dependency.getConstituentsCoveringToken(tokenPosition).get(0);

                    List<Relation> incomingRelations =
                            prepositionDepConstituent.getIncomingRelations();

                    List<Constituent> list = new ArrayList<>();
                    if (incomingRelations != null && incomingRelations.size() > 0) {

                        Constituent parent = incomingRelations.get(0).getSource();

                        for (Relation out : parent.getOutgoingRelations()) {
                            if (out == incomingRelations.get(0))
                                continue;

                            String label = out.getRelationName();

                            if (label.contains("prep")) {
                                Constituent ppNode = out.getTarget();

                                list.add(addPointerToSource(c, ppNode));

                                // get the first child of the pp and add this
                                List<Relation> ppOut = ppNode.getOutgoingRelations();

                                if (ppOut != null && ppOut.size() != 0) {

                                    Constituent child = ppOut.get(0).getTarget();
                                    list.add(addPointerToSource(c, child));

                                }
                            }
                        }
                    }

                    return list;

                }

                @Override
                public String name() {
                    return "dep-pp-N";
                }
            };
    public static final FeatureInputTransformer easyFirstNeighboringPP =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent c) {
                    TextAnnotation ta = c.getTextAnnotation();
                    int tokenPosition = c.getStartSpan();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);

                    Constituent prepositionDepConstituent =
                            dependency.getConstituentsCoveringToken(tokenPosition).get(0);

                    List<Relation> incomingRelations =
                            prepositionDepConstituent.getIncomingRelations();

                    List<Constituent> list = new ArrayList<>();
                    if (incomingRelations != null && incomingRelations.size() > 0) {

                        Constituent parent = incomingRelations.get(0).getSource();

                        for (Relation out : parent.getOutgoingRelations()) {
                            if (out == incomingRelations.get(0))
                                continue;

                            String label = out.getRelationName();

                            if (label.contains("prep")) {
                                Constituent ppNode = out.getTarget();

                                list.add(addPointerToSource(c, ppNode));

                                // get the first child of the pp and add this
                                List<Relation> ppOut = ppNode.getOutgoingRelations();

                                if (ppOut != null && ppOut.size() != 0) {

                                    Constituent child = ppOut.get(0).getTarget();
                                    list.add(addPointerToSource(c, child));

                                }
                            }
                        }
                    }

                    return list;

                }

                @Override
                public String name() {
                    return "pp-N";
                }
            };
    public final static FeatureInputTransformer dependencySubjectOfDominatingVerb =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TextAnnotation ta = input.getTextAnnotation();
                    int tokenPosition = input.getStartSpan();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);

                    Constituent verbNode =
                            dependency.getConstituentsCoveringToken(tokenPosition).get(0);
                    boolean done = false;

                    while (!done) {
                        String pos = WordHelpers.getPOS(ta, verbNode.getStartSpan());

                        if (POSUtils.isPOSVerb(pos)) {
                            done = true;
                        } else {
                            List<Relation> incoming = verbNode.getIncomingRelations();
                            if (incoming == null || incoming.size() == 0) {
                                return new ArrayList<>();
                            } else
                                verbNode = incoming.get(0).getSource();
                        }
                    }

                    return Collections.singletonList(addPointerToSource(input, verbNode));
                }

                @Override
                public String name() {
                    return "dep-sbj-dom-v";
                }
            };
    public final static FeatureInputTransformer easyFirstSubjectOfDominatingVerb =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {

                    TextAnnotation ta = input.getTextAnnotation();
                    int tokenPosition = input.getStartSpan();
                    TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);

                    Constituent verbNode =
                            dependency.getConstituentsCoveringToken(tokenPosition).get(0);
                    boolean done = false;

                    while (!done) {
                        String pos = WordHelpers.getPOS(ta, verbNode.getStartSpan());

                        if (POSUtils.isPOSVerb(pos)) {
                            done = true;
                        } else {
                            List<Relation> incoming = verbNode.getIncomingRelations();
                            if (incoming == null || incoming.size() == 0) {
                                return new ArrayList<>();
                            } else
                                verbNode = incoming.get(0).getSource();
                        }
                    }

                    return Collections.singletonList(addPointerToSource(input, verbNode));
                }

                @Override
                public String name() {
                    return "sbj-dom-v";
                }
            };
    public final static FeatureInputTransformer previousWord = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {
            int tokenId = input.getStartSpan();
            if (tokenId > 0) {
                Constituent c =
                        new Constituent("", "", input.getTextAnnotation(), tokenId - 1, tokenId);
                return Collections.singletonList(addPointerToSource(input, c));
            } else
                return new ArrayList<>();
        }

        @Override
        public String name() {
            return "w[-1]";
        }
    };
    public final static FeatureInputTransformer nextWord = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {
            int tokenId = input.getEndSpan();
            TextAnnotation ta = input.getTextAnnotation();
            Sentence sentence = ta.getSentence(input.getSentenceId());
            if (tokenId < sentence.size()) {
                Constituent c = new Constituent("", "", ta, tokenId, tokenId + 1);
                return Collections.singletonList(addPointerToSource(input, c));
            } else
                return new ArrayList<>();
        }

        @Override
        public String name() {
            return "w[+1]";
        }
    };
    public static final FeatureInputTransformer eachWordInConstituent =
            new FeatureInputTransformer() {

                @Override
                public List<Constituent> transform(Constituent input) {
                    List<Constituent> list = new ArrayList<>();
                    TextAnnotation ta = input.getTextAnnotation();

                    for (int i = input.getStartSpan(); i < input.getEndSpan(); i++) {
                        list.add(new Constituent("", "", ta, i, i + 1));
                    }

                    return list;
                }

                @Override
                public String name() {
                    return "each-word";
                }
            };
    private static final Annotator CHARNIAK_DEPENDENCIES = new HeadFinderDependencyViewGenerator(
            ViewNames.PARSE_CHARNIAK);
    public static final FeatureInputTransformer charniakGovernor = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {

            TextAnnotation ta = input.getTextAnnotation();

            TreeView dependency = getDependencyView(ta, CHARNIAK_DEPENDENCIES);

            return getGovernor(input, dependency);
        }

        @Override
        public String name() {
            return "ch-gov";
        }

    };
    public static final FeatureInputTransformer charniakObject = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {
            TextAnnotation ta = input.getTextAnnotation();

            return getObject(input, getDependencyView(ta, CHARNIAK_DEPENDENCIES), null);
        }

        @Override
        public String name() {
            return "ch-obj";
        }

    };
    private static final Annotator STANFORD_DEPENDENCIES = new HeadFinderDependencyViewGenerator(
            ViewNames.PARSE_STANFORD);
    public static final FeatureInputTransformer stanfordGovernor = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {

            TextAnnotation ta = input.getTextAnnotation();

            TreeView dependency = getDependencyView(ta, STANFORD_DEPENDENCIES);

            return getGovernor(input, dependency);
        }

        @Override
        public String name() {
            return "st-gov";
        }

    };
    public static final FeatureInputTransformer stanfordObject = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {
            TextAnnotation ta = input.getTextAnnotation();

            return getObject(input, getDependencyView(ta, STANFORD_DEPENDENCIES), null);
        }

        @Override
        public String name() {
            return "st-obj";
        }

    };
    private static final Annotator BERKELEY_DEPENDENCIES = new HeadFinderDependencyViewGenerator(
            ViewNames.PARSE_BERKELEY);
    public static final FeatureInputTransformer berkeleyGovernor = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {

            TextAnnotation ta = input.getTextAnnotation();

            return getGovernor(input, getDependencyView(ta, BERKELEY_DEPENDENCIES));
        }

        @Override
        public String name() {
            return "be-gov";
        }

    };
    public static final FeatureInputTransformer berkeleyObject = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {
            TextAnnotation ta = input.getTextAnnotation();

            return getObject(input, getDependencyView(ta, BERKELEY_DEPENDENCIES), null);
        }

        @Override
        public String name() {
            return "be-obj";
        }

    };
    public static FeatureInputTransformer constituentParent = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {

            List<Constituent> c = new ArrayList<>();
            for (Relation r : input.getIncomingRelations()) {
                c.add(addPointerToSource(input, r.getSource()));
            }
            return c;
        }

        @Override
        public String name() {
            return "p";
        }

    };
    public static FeatureInputTransformer constituentChild = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {

            List<Constituent> c = new ArrayList<>();
            for (Relation r : input.getOutgoingRelations()) {
                c.add(addPointerToSource(input, r.getTarget()));
            }
            return c;
        }

        @Override
        public String name() {
            return "c";
        }
    };
    public static FeatureInputTransformer firstWord = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {
            return Collections.singletonList(new Constituent("", "", input.getTextAnnotation(),
                    input.getStartSpan(), input.getStartSpan() + 1));
        }

        @Override
        public String name() {
            return "#^w";
        }
    };
    public static FeatureInputTransformer lastWord = new FeatureInputTransformer() {

        @Override
        public List<Constituent> transform(Constituent input) {
            return Collections.singletonList(new Constituent("", "", input.getTextAnnotation(),
                    input.getEndSpan() - 1, input.getEndSpan()));
        }

        @Override
        public String name() {
            return "#w$";
        }
    };

    private static TreeView getDependencyView(TextAnnotation ta, Annotator viewGenerator) {
        if (!ta.hasView(viewGenerator.getViewName())) {
            synchronized (FeatureInputTransformer.class) {
                if (!ta.hasView(viewGenerator.getViewName())) {
                    try {
                        ta.addView(viewGenerator);
                    } catch (AnnotatorException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return (TreeView) ta.getView(viewGenerator.getViewName());
    }

    private static Constituent getHead(Constituent c, TreeView dependency) {
        Constituent end = null;
        List<Constituent> constituentsCovering = dependency.getConstituentsCovering(c);
        for (Constituent d : constituentsCovering) {
            List<Relation> in = d.getIncomingRelations();
            if (in.size() == 0) {
                end = d;
                break;
            } else {
                Constituent parent = in.get(0).getSource();

                int parentToken = parent.getStartSpan();
                if (c.getStartSpan() <= parentToken && parentToken < c.getEndSpan())
                    continue;

                if (end == null) {
                    end = d;
                } else if (end.getStartSpan() < d.getStartSpan()) {
                    end = d;
                }
            }
        }

        Constituent c1;
        if (end == null)
            c1 = constituentsCovering.get(0).cloneForNewView("");
        else
            c1 = end.cloneForNewView("");

        return addPointerToSource(c, c1);

    }

    private static List<Constituent> getObject(Constituent input, TreeView dependency, String label) {
        List<Constituent> constituentsCovering = dependency.getConstituentsCovering(input);

        if (constituentsCovering.size() == 0)
            return new ArrayList<>();

        Constituent c = constituentsCovering.get(0);

        List<Relation> outgoingRelations = c.getOutgoingRelations();

        if (outgoingRelations == null || outgoingRelations.size() == 0)
            return new ArrayList<>();
        else {

            for (Relation r : outgoingRelations) {
                if (label != null && r.getRelationName().contains(label))
                    return Collections.singletonList(addPointerToSource(input, r.getTarget()));
            }

            return Collections.singletonList(addPointerToSource(input, outgoingRelations.get(0)
                    .getTarget()));

        }
    }

    private static List<Constituent> getModifiers(Constituent input, TreeView dependency) {
        List<Constituent> constituentsCovering = dependency.getConstituentsCovering(input);

        if (constituentsCovering.size() == 0)
            return new ArrayList<>();

        Constituent c = constituentsCovering.get(0);

        List<Relation> outgoingRelations = c.getOutgoingRelations();

        if (outgoingRelations == null || outgoingRelations.size() == 0)
            return new ArrayList<>();
        else {

            for (Relation r : outgoingRelations) {
                if (r.getRelationName().contains("mod"))
                    return Collections.singletonList(addPointerToSource(input, r.getTarget()));
            }

            return Collections.singletonList(addPointerToSource(input, outgoingRelations.get(0)
                    .getTarget()));

        }
    }

    private static List<Constituent> getGovernor(Constituent input, TreeView dependency) {
        List<Constituent> constituentsCovering = dependency.getConstituentsCovering(input);

        if (constituentsCovering.size() == 0)
            return new ArrayList<>();

        Constituent c = constituentsCovering.get(0);

        List<Relation> incomingRelations = c.getIncomingRelations();

        if (incomingRelations == null || incomingRelations.size() == 0)
            return new ArrayList<>();
        else
            return Collections.singletonList(addPointerToSource(input, incomingRelations.get(0)
                    .getSource()));
    }

    private static Constituent addPointerToSource(Constituent source, Constituent c) {
        Constituent c1 = c.cloneForNewView(c.getViewName());
        new Relation("", source, c1, 0.0);
        return c1;
    }

    public abstract String name();
}
