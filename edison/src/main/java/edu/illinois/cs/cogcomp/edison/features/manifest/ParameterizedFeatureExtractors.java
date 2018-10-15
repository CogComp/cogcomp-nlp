/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ParameterizedFeatureExtractors {

    private static final String PARSE_VIEW = ":parse-view";
    private static final ParameterizedFeatureGenerator voiceGenerator =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    return new VerbVoiceIndicator(attributes.get(PARSE_VIEW));
                }
            };
    private static final ParameterizedFeatureGenerator clauseGenerator =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    String parse = attributes.get(PARSE_VIEW);

                    switch (parse) {
                        case ViewNames.PARSE_CHARNIAK:
                            return ClauseFeatureExtractor.CHARNIAK;
                        case ViewNames.PARSE_BERKELEY:
                            return ClauseFeatureExtractor.BERKELEY;
                        case ViewNames.PARSE_STANFORD:
                            return ClauseFeatureExtractor.STANFORD;
                        default:
                            throw new EdisonException("Cannot generate clause view for using "
                                    + parse);
                    }

                }
            };
    private static final ParameterizedFeatureGenerator attributeGenerator =
            new ParameterizedFeatureGenerator(":name") {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    return new AttributeFeature(attributes.get(":name"));
                }
            };
    private static final ParameterizedFeatureGenerator subcatGenerator =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    return new SubcategorizationFrame(attributes.get(PARSE_VIEW));
                }
            };
    private static final ParameterizedFeatureGenerator parsePhraseLabel =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    return new ParsePhraseType(attributes.get(PARSE_VIEW));
                }
            };
    private static final ParameterizedFeatureGenerator parsePath =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {

                    return new ParsePath(attributes.get(PARSE_VIEW));
                }
            };
    private static final ParameterizedFeatureGenerator syntacticFrame =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    return new SyntacticFrame(attributes.get(PARSE_VIEW));
                }
            };
    private static final ParameterizedFeatureGenerator dependencyPathGenerator =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    return new DependencyPath(attributes.get(PARSE_VIEW));
                }
            };
    private static final ParameterizedFeatureGenerator headFeatures =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {

                    if (fex == null) {
                        throw new EdisonException("Invalid definition of head-features\n" + tree);
                    }

                    String parseView = attributes.get(PARSE_VIEW);
                    if (parseView.equals(ViewNames.PARSE_GOLD)
                            || parseView.equals(ViewNames.PARSE_STANFORD))
                        return new ParseHeadWordFeatureExtractor(parseView, fex);

                    FeatureInputTransformer transformer;
                    switch (parseView) {
                        case ViewNames.PARSE_CHARNIAK:
                            transformer = FeatureInputTransformer.charniakHead;
                            break;
                        case ViewNames.DEPENDENCY_STANFORD:
                            transformer = FeatureInputTransformer.stanfordDependencyHead;
                            break;
                        case ViewNames.DEPENDENCY:
                            transformer = FeatureInputTransformer.easyFirstDependencyHead;
                            break;
                        case ViewNames.PARSE_BERKELEY:
                            transformer = FeatureInputTransformer.berkeleyHead;
                            break;
                        default:
                            throw new EdisonException("Invalid parse view: " + parseView + "\n"
                                    + tree);
                    }

                    return new FeatureCollection("", transformer, fex);

                }
            };
    private static final ParameterizedFeatureGenerator governorFeatures =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {

                    if (fex == null) {
                        throw new EdisonException("Invalid definition of head-features\n" + tree);
                    }

                    String parseView = attributes.get(PARSE_VIEW);

                    FeatureInputTransformer transformer;
                    switch (parseView) {
                        case ViewNames.PARSE_CHARNIAK:
                            transformer = FeatureInputTransformer.charniakGovernor;
                            break;
                        case ViewNames.PARSE_STANFORD:
                            transformer = FeatureInputTransformer.stanfordGovernor;
                            break;
                        case ViewNames.PARSE_BERKELEY:
                            transformer = FeatureInputTransformer.berkeleyGovernor;
                            break;
                        case ViewNames.DEPENDENCY_STANFORD:
                            transformer = FeatureInputTransformer.stanfordDependencyGovernor;
                            break;
                        case ViewNames.DEPENDENCY:
                            transformer = FeatureInputTransformer.easyFirstDependencyGovernor;
                            break;
                        default:
                            throw new EdisonException("Invalid parse view: " + parseView + "\n"
                                    + tree);
                    }

                    return new FeatureCollection("", transformer, fex);

                }
            };
    private static final ParameterizedFeatureGenerator objectFeatures =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {

                    if (fex == null) {
                        throw new EdisonException("Invalid definition of head-features\n" + tree);
                    }

                    String parseView = attributes.get(PARSE_VIEW);

                    FeatureInputTransformer transformer;
                    if (parseView.equals(ViewNames.PARSE_CHARNIAK))
                        transformer = FeatureInputTransformer.charniakObject;
                    else if (parseView.equals(ViewNames.PARSE_STANFORD))
                        transformer = FeatureInputTransformer.stanfordObject;
                    else if (parseView.equals(ViewNames.PARSE_BERKELEY))
                        transformer = FeatureInputTransformer.berkeleyObject;
                    else if (parseView.equals(ViewNames.DEPENDENCY_STANFORD))
                        transformer = FeatureInputTransformer.stanfordDependencyObject;
                    else if (parseView.equals(ViewNames.DEPENDENCY))
                        transformer = FeatureInputTransformer.easyFirstDependencyObject;
                    else
                        throw new EdisonException("Invalid parse view: " + parseView + "\n" + tree);

                    return new FeatureCollection("", transformer, fex);

                }
            };
    private static final ParameterizedFeatureGenerator dependencyPathNgramsGenerator =
            new ParameterizedFeatureGenerator(PARSE_VIEW, ":n") {

                @Override
                public FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {

                    String viewName = attributes.get(PARSE_VIEW);
                    int n = Integer.parseInt(attributes.get(":n"));

                    return new DependencyPathNgrams(viewName, n);
                }
            };
    private static final ParameterizedFeatureGenerator context = new ParameterizedFeatureGenerator(
            ":size", ":include-index", ":ignore-center") {

        @Override
        FeatureExtractor getFeatureExtractor(Map<String, String> attributes, Tree<String> tree,
                FeatureExtractor fex) throws EdisonException {

            int size = Integer.parseInt(attributes.get(":size"));
            boolean specifyIndex = Boolean.parseBoolean(attributes.get(":include-index"));
            boolean ignoreConstituent = Boolean.parseBoolean(attributes.get(":ignore-center"));

            ContextFeatureExtractor f =
                    new ContextFeatureExtractor(size, specifyIndex, ignoreConstituent);

            f.addFeatureExtractor(fex);
            return f;

        }
    };
    static Map<String, ParameterizedFeatureGenerator> fexes = new HashMap<>();
    private static ParameterizedFeatureGenerator dependencyModifiers =
            new ParameterizedFeatureGenerator(PARSE_VIEW) {

                @Override
                FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    String viewName = attributes.get(PARSE_VIEW);

                    return new DependencyModifierFeatureExtractor(viewName, fex);
                }
            };

    private static ParameterizedFeatureGenerator parseSiblings = new ParameterizedFeatureGenerator(
            PARSE_VIEW) {

        @Override
        FeatureExtractor getFeatureExtractor(Map<String, String> attributes, Tree<String> tree,
                FeatureExtractor fex) throws EdisonException {
            String viewName = attributes.get(PARSE_VIEW);

            return new ParseSiblings(viewName);
        }
    };

    private static ParameterizedFeatureGenerator regexFeatureGenerator =
            new ParameterizedFeatureGenerator(":regex") {

                @Override
                FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                        Tree<String> tree, FeatureExtractor fex) throws EdisonException {
                    return new RegexFeatureExtractor(attributes.get(":regex"));
                }
            };

    static {
        fexes.put("attribute", attributeGenerator);
        fexes.put("voice", voiceGenerator);
        fexes.put("clauses", clauseGenerator);
        fexes.put("subcat-frame", subcatGenerator);
        fexes.put("parse-phrase-features", parsePhraseLabel);
        fexes.put("parse-siblings", parseSiblings);
        fexes.put("parse-path", parsePath);
        fexes.put("syntactic-frame", syntacticFrame);
        fexes.put("head-features", headFeatures);
        fexes.put("governor-features", governorFeatures);
        fexes.put("object-features", objectFeatures);
        fexes.put("dependency-path", dependencyPathGenerator);
        fexes.put("dependency-path-ngrams", dependencyPathNgramsGenerator);
        fexes.put("context", context);
        fexes.put("dependency-modifiers", dependencyModifiers);
        fexes.put("regex-feature", regexFeatureGenerator);
    }

    static FeatureExtractor getParameterizedFeatureExtractor(Tree<String> tree,
            FeatureExtractor fex, HashMap<String, String> variables) throws EdisonException {

        ParameterizedFeatureGenerator gen = fexes.get(tree.getLabel());

        return gen.getFeatureExtractor(tree, fex, variables);

    }

    public static List<String> getKnownFeatureExtractors() {
        return new ArrayList<>(fexes.keySet());
    }

    public static String[] getRequiredArguments(String fexName) {
        return fexes.get(fexName).getRequiredArguments();
    }

    private static abstract class ParameterizedFeatureGenerator {

        protected String[] preReqs;

        public ParameterizedFeatureGenerator(String... preReqs) {
            this.preReqs = preReqs;
        }

        public String[] getRequiredArguments() {
            return preReqs;
        }

        FeatureExtractor getFeatureExtractor(Tree<String> tree, FeatureExtractor fex,
                HashMap<String, String> variables) throws EdisonException {
            Map<String, String> attributes = getAttributes(tree, variables);
            for (String preReq : preReqs) {
                checkPrerequisites(attributes, tree, preReq);
            }
            return getFeatureExtractor(attributes, tree, fex);
        }

        abstract FeatureExtractor getFeatureExtractor(Map<String, String> attributes,
                Tree<String> tree, FeatureExtractor fex) throws EdisonException;

        private void checkPrerequisites(Map<String, String> attributes, Tree<String> tree,
                String name) throws EdisonException {
            if (!attributes.containsKey(name))
                throw new EdisonException("Required parameter" + name + " missing\n" + tree);
        }

        private Map<String, String> getAttributes(Tree<String> tree,
                HashMap<String, String> variables) throws EdisonException {

            Map<String, String> attribs = new HashMap<>();
            int childId = 0;
            while (childId < tree.getNumberOfChildren()) {
                Tree<String> child = tree.getChild(childId++);

                String label = child.getLabel();
                if (label.startsWith(":")) {

                    if (!child.isLeaf())
                        throw new EdisonException("Parameter " + label + " should be a leaf\n"
                                + tree);

                    if (childId == tree.getNumberOfChildren())
                        throw new EdisonException("Parameter " + label + " is missing a value\n"
                                + tree);

                    child = tree.getChild(childId++);
                    String value = child.getLabel();
                    if (variables.containsKey(value))
                        value = variables.get(value);

                    attribs.put(label, value);
                }
            }

            return attribs;
        }
    }
}
