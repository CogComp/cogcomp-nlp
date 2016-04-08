package edu.illinois.cs.cogcomp.core.datastructures;

public class Option<T> {
    private final T s;

    public Option(T s) {
        this.s = s;
    }

    private Option() {
        this.s = null;
    }

    public boolean isPresent() {
        return s != null;
    }

    public T get() {
        assert isPresent();
        return s;
    }

    public static <T> Option<T> empty() {
        return new Option<>();
    }

    @Override
    public String toString() {
        if (s == null)
            return "None";
        else
            return "Some[" + s + "]";
    }
}
