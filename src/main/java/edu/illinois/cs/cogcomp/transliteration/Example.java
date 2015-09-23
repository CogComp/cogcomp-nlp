package edu.illinois.cs.cogcomp.transliteration;

/// <summary>
/// Represents a training example for the transliteration model.  Examples may be weighted to make them more or less
/// relatively important.
/// </summary>
public class Example {
    /// <summary>
    /// Normalizes a Hebrew word by replacing end-form characters with their in-word equivalents.
    /// </summary>
    /// <param name="hebrewWord"></param>
    /// <returns></returns>
    public static String NormalizeHebrew(String hebrewWord) {
        return Program.NormalizeHebrew(hebrewWord);
    }

    /// <summary>
    /// Removes accents from characters.
    /// This can be a useful fallback method if the model cannot make a prediction
    /// over a given word because it has not seen a particular accented character before.
    /// </summary>
    /// <param name="word"></param>
    /// <returns></returns>
    public static String StripAccents(String word) {
        return Program.StripAccent(word);
    }

    /// <summary>
    /// Creates a new training example with weight 1.
    /// </summary>
    /// <param name="sourceWord"></param>
    /// <param name="transliteratedWord"></param>
    public Example(String sourceWord, String transliteratedWord)
    {
        this(sourceWord,transliteratedWord,1);
    }

    /// <summary>
    /// Creates a new training example with the specified weight.
    /// </summary>
    /// <param name="sourceWord"></param>
    /// <param name="transliteratedWord"></param>
    /// <param name="weight"></param>
    public Example(String sourceWord, String transliteratedWord, double weight) {
        this.sourceWord = sourceWord;
        this.transliteratedWord = transliteratedWord;
        this.weight = weight;
    }

    // SWM: what is this?
    internal Triple<String, String,double> Triple
    {
        get {
        return new Triple<String, String, double>(sourceWord, transliteratedWord, weight);
    }
    }

    /// <summary>
    /// Gets a "reversed" copy of this example, with the source and transliterated words swapped.
    /// </summary>
    public Example Reverse

    {
        get {
        return new Example(transliteratedWord, sourceWord, weight);
    }
    }

    /// <summary>
    /// The word in the source language.
    /// </summary>
    public String sourceWord;

    /// <summary>
    /// The transliterated word.
    /// </summary>
    public String transliteratedWord;

    /// <summary>
    /// The relative important of this example.  A weight of 1 means normal importance.
    /// </summary>
    public double weight;
}

