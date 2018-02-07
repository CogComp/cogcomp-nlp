package edu.illinois.cs.cogcomp.finetyper.wsd.datastructure;


/**
 * Created by haowu4 on 2/6/18.
 * Pair of word and its part of speech tag.
 */
public class WordAndPOS {
    private String word;
    private String pos;

    public WordAndPOS(String word, String pos) {
        this.word = word;
        this.pos = pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordAndPOS that = (WordAndPOS) o;

        if (!word.equals(that.word)) return false;
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        int result = word.hashCode();
        result = 31 * result + pos.hashCode();
        return result;
    }

    public String getWord() {
        return word;
    }

    public String getPos() {
        return pos;
    }
}
