package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Triple;

import java.util.List;

abstract class TransliterationModel {
    public abstract double GetProbability(String word1, String word2);

    public abstract TransliterationModel LearnModel(List<Triple<String, String, Double>> examples);
}

