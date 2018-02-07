package edu.illinois.cs.cogcomp.finetyper.finer.components.typers;

import java.util.Arrays;

/**
 * Created by haowu4 on 5/15/17.
 */
public class NGramPattern {
    private int before;
    private int after;
    private String[] tokens;

    public NGramPattern(int before, int after, String[] tokens) {
        this.before = before;
        this.after = after;
        this.tokens = tokens;
    }

    public int getBefore() {
        return before;
    }

    public int getAfter() {
        return after;
    }

    public String[] getTokens() {
        return tokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NGramPattern that = (NGramPattern) o;

        if (before != that.before) return false;
        if (after != that.after) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(tokens, that.tokens);
    }

    @Override
    public int hashCode() {
        int result = before;
        result = 31 * result + after;
        result = 31 * result + Arrays.hashCode(tokens);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < before; i++) {
            sb.append("* ");
        }

        sb.append(String.join(" ", tokens));

        for (int i = 0; i < after; i++) {
            sb.append(" *");
        }
        return sb.toString();
    }
}
