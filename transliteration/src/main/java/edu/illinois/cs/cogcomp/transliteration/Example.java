/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Triple;

/**
 * Represents a training example for the transliteration model.  Examples may be weighted to make them more or less
 * relatively important.
 */
public class Example extends MultiExample {

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
        super(sourceWord, transliteratedWord, weight);
    }

    public String getTransliteratedWord(){
        return this.transliteratedWords.get(0);
    }

    /**
     * This used to be a field, with a get{} method.
     * @return
     */
    Triple<String, String, Double> Triple()
    {
        return new Triple<>(sourceWord, transliteratedWords.get(0), weight);
    }

    /**
     * Gets a "reversed" copy of this example, with the source and transliterated words swapped.
     * This used to be a field, with a get() method.
     */
    public Example Reverse(){
        return new Example(transliteratedWords.get(0), sourceWord, weight);
    }


}

