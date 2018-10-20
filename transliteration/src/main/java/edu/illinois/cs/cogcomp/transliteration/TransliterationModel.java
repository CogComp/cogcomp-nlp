/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Triple;

import java.util.List;

abstract class TransliterationModel {
    public abstract double GetProbability(String word1, String word2);

    public abstract TransliterationModel LearnModel(List<Triple<String, String, Double>> examples);
}

