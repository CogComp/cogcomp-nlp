package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Triple;

/**
 * Represents a training example for the transliteration model.  Examples may be weighted to make them more or less
 * relatively important.
 */
public class Example {


    /**
     * Creates a new training example with weight 1.
     * @param sourceWord
     * @param transliteratedWord
     */
    public Example(String sourceWord, String transliteratedWord)
    {

        this(sourceWord, transliteratedWord, 1);
    }

    /**
     * Creates a new training example with the specified weight.
     * @param sourceWord
     * @param transliteratedWord
     * @param weight
     */
    public Example(String sourceWord, String transliteratedWord, double weight) {
        this.sourceWord = sourceWord;
        this.transliteratedWord = transliteratedWord;
        this.weight = weight;
    }

    /**
     * This used to be a field, with a get{} method.
     * @return
     */
    Triple<String, String, Double> Triple()
    {
        return new Triple<>(sourceWord, transliteratedWord, weight);
    }

    /**
     * Gets a "reversed" copy of this example, with the source and transliterated words swapped.
     * This used to be a field, with a get() method.
     */
    public Example Reverse(){
        return new Example(transliteratedWord, sourceWord, weight);
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
     * The word in the source language.
     */
    public String sourceWord;

    /**
     * The transliterated word.
     */
    public String transliteratedWord;

    /**
     * The relative important of this example.  A weight of 1 means normal importance.
     */
    public double weight;
}

