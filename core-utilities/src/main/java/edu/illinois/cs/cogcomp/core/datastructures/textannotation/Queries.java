/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;

import java.util.*;

/**
 * This contains a collection of queries that can be passed to the where and select clauses of an
 * {@link IQueryable} over {@link Constituent}s like the
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View} class.
 *
 * @author Vivek Srikumar
 */
@SuppressWarnings("serial")
public abstract class Queries {

    /**
     * Returns a {@link edu.illinois.cs.cogcomp.core.transformers.Predicate} that will check if the
     * argument to the predicate contains the argument to this function.
     *
     * @param c The {@link Constituent} that should be contained in the argument to the Predicate
     *        for the predicate to return <code>true</code>
     * @return A Predicate
     */
    public static Predicate<Constituent> containsConstituent(final Constituent c) {
        return new Predicate<Constituent>() {
            public Boolean transform(final Constituent input) {
                return input.getStartSpan() <= c.getStartSpan()
                        && input.getEndSpan() >= c.getEndSpan();
            }
        };
    }

    public static Predicate<Constituent> sameSpanAsConstituent(final Constituent c) {
        return new Predicate<Constituent>() {
            public Boolean transform(final Constituent input) {
                return input.getStartSpan() == c.getStartSpan()
                        && input.getEndSpan() == c.getEndSpan();
            }
        };
    }

    public static Predicate<Constituent> before(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent input) {
                return input.getEndSpan() <= c.getStartSpan();
            }
        };
    }

    public static Predicate<Constituent> after(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent input) {
                return input.getStartSpan() >= c.getEndSpan();
            }
        };
    }

    public static Predicate<Constituent> containedInConstituent(final Constituent c) {
        return new Predicate<Constituent>() {
            public Boolean transform(final Constituent input) {
                return c.getStartSpan() <= input.getStartSpan()
                        && c.getEndSpan() >= input.getEndSpan();
            }
        };
    }

    public static Predicate<Constituent> containedInConstituentExclusive(final Constituent c) {
        return new Predicate<Constituent>() {
            public Boolean transform(final Constituent input) {
                return (c.getStartSpan() <= input.getStartSpan() && c.getEndSpan() >= input
                        .getEndSpan())
                        && (c.getStartSpan() != input.getStartSpan() || c.getEndSpan() != input
                                .getEndSpan());
            }
        };
    }

    public static Predicate<Constituent> containsConstituentExclusive(final Constituent c) {
        return new Predicate<Constituent>() {
            public Boolean transform(final Constituent input) {
                return (input.getStartSpan() <= c.getStartSpan() && input.getEndSpan() >= c
                        .getEndSpan())
                        && (c.getStartSpan() != input.getStartSpan() || c.getEndSpan() != input
                                .getEndSpan());
            }
        };
    }

    public static Predicate<Constituent> sameStartSpanAs(final Constituent c) {
        return new Predicate<Constituent>() {
            public Boolean transform(final Constituent input) {
                return c.getStartSpan() == input.getStartSpan();
            }
        };
    }

    public static Predicate<Constituent> sameEndSpanAs(final Constituent c) {
        return new Predicate<Constituent>() {
            public Boolean transform(final Constituent input) {
                return c.getEndSpan() == input.getEndSpan();
            }
        };
    }

    public static Predicate<Constituent> hasOverlap(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent input) {
                int s1 = c.getStartSpan();
                int e1 = c.getEndSpan();

                int s2 = input.getStartSpan();
                int e2 = input.getEndSpan();

                return s2 < e1 && s1 < e2;
            }
        };

    }

    public static Predicate<Constituent> hasNoOverlap(final Constituent c) {
        return hasOverlap(c).negate();
    }

    public static Predicate<Constituent> exclusivelyOverlaps(Constituent argument) {
        return Queries.hasOverlap(argument).and(Queries.containedInConstituent(argument).negate())
                .and(Queries.containsConstituent(argument).negate())
                .and(Queries.sameSpanAsConstituent(argument).negate());
    }

    public static Predicate<Constituent> hasLabel(final String label) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent arg0) {
                return arg0.getLabel().equals(label);
            }
        };
    }

    public static Predicate<Constituent> adjacentToAfter(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent arg0) {
                return arg0.getStartSpan() == c.getEndSpan();
            }
        };
    }

    public static Predicate<Constituent> adjacentToBefore(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent arg0) {
                return c.getStartSpan() == arg0.getEndSpan();
            }
        };
    }

    public static Predicate<Constituent> nThChildOf(final Constituent parent, final int n) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent arg0) {
                List<Relation> relations = parent.getOutgoingRelations();

                if (relations.size() <= n)
                    return false;
                return relations.get(n).getTarget() == parent;
            }
        };
    }

    public static Predicate<Constituent> lastChildOf(final Constituent parent) {
        return nThChildOf(parent, parent.getOutgoingRelations().size() - 1);
    }

    public static Predicate<Constituent> firstChildOf(final Constituent parent) {
        return nThChildOf(parent, 0);
    }

    public static Predicate<Constituent> isParentOf(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent p) {
                boolean found = false;
                for (Relation r : c.getIncomingRelations()) {
                    if (r.getSource() == p) {
                        found = true;
                        break;
                    }
                }

                return found;
            }
        };
    }

    public static Predicate<Constituent> isChildOf(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent c1) {
                boolean found = false;
                for (Relation r : c.getOutgoingRelations()) {
                    if (r.getTarget() == c1) {
                        found = true;
                        break;
                    }
                }

                return found;
            }
        };
    }

    public static Predicate<Constituent> hasParent() {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent input) {
                return input.getIncomingRelations().size() > 0;
            }
        };
    }

    public static Predicate<Constituent> hasChildren() {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent input) {
                return input.getOutgoingRelations().size() > 0;
            }
        };
    }

    public static Predicate<Constituent> isSiblingOf(final Constituent c) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent c1) {
                boolean value = false;

                if (c1 == c)
                    return true;

                for (Relation r0 : c.getIncomingRelations()) {
                    for (Relation r1 : c1.getIncomingRelations()) {
                        if (r0.getSource() == r1.getSource()) {
                            value = true;
                            break;
                        }
                    }
                    if (value)
                        break;
                }
                return value;
            }
        };
    }

    public static Predicate<Constituent> grandChildOf(final Constituent c0) {
        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent c1) {

                // c0 is the grandparent of c1
                boolean grandparent = false;
                for (Relation r1 : c1.getIncomingRelations()) {
                    Constituent parent = r1.getSource();
                    for (Relation r2 : parent.getIncomingRelations()) {
                        if (c0 == r2.getSource()) {
                            grandparent = true;
                            break;
                        }
                    }

                    if (grandparent)
                        break;
                }

                return grandparent;
            }
        };
    }

    public static Predicate<Constituent> descendantOf(final Constituent c0) {

        final Set<Constituent> descendants = new HashSet<>();

        Queue<Constituent> queue = new LinkedList<>();
        queue.add(c0);

        while (queue.peek() != null) {
            Constituent c = queue.remove();

            if (!descendants.contains(c)) {
                descendants.add(c);

                for (Relation r : c.getOutgoingRelations())
                    descendants.add(r.getTarget());
            }
        }

        return new Predicate<Constituent>() {

            @Override
            public Boolean transform(Constituent input) {
                return descendants.contains(input);
            }
        };
    }

}
