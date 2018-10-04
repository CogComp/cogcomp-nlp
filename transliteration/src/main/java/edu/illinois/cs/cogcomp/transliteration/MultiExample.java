/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is meant to fulfill the same role as the Example class, but allows multiple
 * targetwords to be associated with a single source word.
 *
 * This is intended primarily for testing on the NEWS dataset.
 * Created by mayhew2 on 11/13/15.
 */
public class MultiExample {

    /**
     * The word in the source language.
     */
    public String sourceWord;

    /**
     * The transliterated word.
     */
    public List<String> transliteratedWords;

    public List<String> getTransliteratedWords(){
        return this.transliteratedWords;
    }

    /**
     * The relative important of this example.  A weight of 1 means normal importance.
     */
    public double weight;

    public MultiExample(String sourceWord, List<String> transliteratedWords, double weight){
        this.sourceWord = sourceWord;
        this.transliteratedWords = transliteratedWords;
        this.weight = weight;

    }

    public MultiExample(String sourceWord, List<String> transliteratedWords){
        this(sourceWord, transliteratedWords, 1);
    }

    public MultiExample(String sourceWord, String transliteratedWord, double weight){
        this.sourceWord = sourceWord;
        this.transliteratedWords = new ArrayList<>();
        this.transliteratedWords.add(transliteratedWord);
        this.weight = weight;
    }

    public MultiExample(String sourceWord, String transliteratedWord){
        this(sourceWord, transliteratedWord, 1);
    }

    public void addTransliteratedWord(String tlw){
        this.transliteratedWords.add(tlw);
    }

    /**
     * Normalizes a Hebrew word by replacing end-form characters with their in-word equivalents.
     * @param hebrewWord
     * @return
     */
    public static String NormalizeHebrew(String hebrewWord) {
        return Program.NormalizeHebrew(hebrewWord);
    }

    /**
     * Removes accents from characters.
     * This can be a useful fallback method if the model cannot make a prediction
     * over a given word because it has not seen a particular accented character before.
     * @param word
     * @return
     */
    public static String StripAccents(String word) {
        return Program.StripAccent(word);
    }

    /**
     * Converts this MultiExample into a list of Examples, one for each transliterated word. Each Example has
     * the same sourceWord.
     * @return
     */
    public List<Example> toExampleList(){
        List<Example> out = new ArrayList<>();

        for(String t : transliteratedWords){
            out.add(new Example(this.sourceWord, t, this.weight));
        }

        return out;
    }

    @Override
    public String toString() {
        String classname = this.getClass().getSimpleName();

        return classname + "{" +
                "sourceWord='" + sourceWord + '\'' +
                ", transliteratedWords=" + transliteratedWords +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiExample that = (MultiExample) o;

        if (Double.compare(that.weight, weight) != 0) return false;
        if (sourceWord != null ? !sourceWord.equals(that.sourceWord) : that.sourceWord != null) return false;
        return transliteratedWords != null ? transliteratedWords.equals(that.transliteratedWords) : that.transliteratedWords == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = sourceWord != null ? sourceWord.hashCode() : 0;
        result = 31 * result + (transliteratedWords != null ? transliteratedWords.hashCode() : 0);
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
