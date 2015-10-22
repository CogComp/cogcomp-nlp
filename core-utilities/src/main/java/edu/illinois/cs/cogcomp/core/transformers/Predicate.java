package edu.illinois.cs.cogcomp.core.transformers;

/**
 * A {@code Predicate} is a special {@link ITransformer} that converts an object
 * of type {@code T} into a {@code boolean}.
 *
 * @author Vivek Srikumar
 *         <p/>
 *         Jun 25, 2009
 */
public abstract class Predicate<T> extends ITransformer<T, Boolean> {

    private static final long serialVersionUID = 8000284252021594481L;

    @SuppressWarnings("serial")
    public Predicate<T> and(final Predicate<T> arg) {
        final Predicate<T> me = this;
        return new Predicate<T>() {
            public Boolean transform(T input) {
                return me.transform(input) && arg.transform(input);
            }

        };
    }

    @SuppressWarnings("serial")
    public Predicate<T> or(final Predicate<T> arg) {
        final Predicate<T> me = this;
        return new Predicate<T>() {
            public Boolean transform(T input) {
                return me.transform(input) || arg.transform(input);
            }

        };
    }

    @SuppressWarnings("serial")
    public Predicate<T> negate() {
        final Predicate<T> me = this;
        return new Predicate<T>() {
            public Boolean transform(T input) {
                return !me.transform(input);
            }

        };
    }
}
