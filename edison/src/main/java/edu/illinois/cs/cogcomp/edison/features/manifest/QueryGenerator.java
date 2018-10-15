/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryGenerator {

    private static final Map<String, KnownQueryHandler> knownQueries = new HashMap<>();

    static {
        knownQueries.put("contains-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.containsConstituent(c);
            }
        });
        knownQueries.put("contained-in-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.containedInConstituent(c);
            }
        });

        knownQueries.put("before-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.before(c);
            }
        });
        knownQueries.put("after-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.after(c);
            }
        });
        knownQueries.put("same-span-as-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.sameSpanAsConstituent(c);
            }
        });
        knownQueries.put("ends-at-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.sameEndSpanAs(c);
            }
        });

        knownQueries.put("starts-at-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.sameStartSpanAs(c);
            }
        });
        knownQueries.put("overlaps-with-target", new KnownQueryHandler(0) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.hasOverlap(c);
            }
        });
        knownQueries.put("has-label", new KnownQueryHandler(1) {

            @Override
            protected Predicate<Constituent> getQuery(List<String> args, Constituent c) {
                return Queries.hasLabel(args.get(0));
            }
        });

    }

    public static Predicate<Constituent> generateQuery(Tree<String> tree, Constituent c,
            Map<String, String> variables) throws EdisonException {
        String label = tree.getLabel();
        switch (label) {
            case "and":
                return and(tree.getChildren(), c, variables);
            case "or":
                return or(tree.getChildren(), c, variables);
            case "not":
                if (tree.getNumberOfChildren() != 1) {
                    throw new EdisonException("Negation can take only one argument");
                }
                return not(tree.getChild(0), c, variables);
            default:
                return knownQuery(tree, c, variables);
        }
    }

    private static Predicate<Constituent> knownQuery(Tree<String> tree, Constituent c,
            Map<String, String> variables) throws EdisonException {

        String label = tree.getLabel();
        if (knownQueries.containsKey(label))
            return knownQueries.get(label).create(tree, c, variables);
        else {

            throw new EdisonException("Unknown query '" + label + "'. Expecting one of "
                    + knownQueries.keySet() + ".\n" + tree);
        }

    }

    private static Predicate<Constituent> not(Tree<String> child, Constituent c,
            Map<String, String> variables) throws EdisonException {
        return generateQuery(child, c, variables).negate();
    }

    private static Predicate<Constituent> and(List<Tree<String>> children, Constituent c,
            Map<String, String> variables) throws EdisonException {
        Predicate<Constituent> predicate = generateQuery(children.get(0), c, variables);

        for (int i = 1; i < children.size(); i++) {
            predicate = predicate.and(generateQuery(children.get(i), c, variables));
        }

        return predicate;
    }

    private static Predicate<Constituent> or(List<Tree<String>> children, Constituent c,
            Map<String, String> variables) throws EdisonException {
        Predicate<Constituent> predicate = generateQuery(children.get(0), c, variables);

        for (int i = 1; i < children.size(); i++) {
            predicate = predicate.or(generateQuery(children.get(i), c, variables));
        }

        return predicate;
    }

    private static abstract class KnownQueryHandler {

        private int nArgs;

        public KnownQueryHandler(int nArgs) {
            this.nArgs = nArgs;

        }

        Predicate<Constituent> create(Tree<String> tree, Constituent c,
                Map<String, String> variables) throws EdisonException {
            return getQuery(getArgs(tree, variables), c);
        }

        protected abstract Predicate<Constituent> getQuery(List<String> args, Constituent c);

        private List<String> getArgs(Tree<String> tree, Map<String, String> variables)
                throws EdisonException {
            List<String> args = new ArrayList<>();
            if (tree.getNumberOfChildren() != nArgs)
                throw new EdisonException("Invalid syntax for '" + tree.getLabel()
                        + "'. Expecting " + nArgs + " arguments, found + "
                        + tree.getNumberOfChildren() + " instead.\n" + tree);

            for (int i = 0; i < nArgs; i++) {
                String label = tree.getChild(i).getLabel();
                if (variables.containsKey(label))
                    label = variables.get(label);
                args.add(label);
            }
            return args;
        }
    }

}
