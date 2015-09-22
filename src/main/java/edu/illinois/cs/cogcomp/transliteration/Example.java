using System;
using System.Collections.Generic;
using System.Text;
using Pasternack.Utility;

namespace SPTransliteration
{
    /// <summary>
    /// Represents a training example for the transliteration model.  Examples may be weighted to make them more or less
    /// relatively important.
    /// </summary>
    [Serializable]
    public struct Example
    {
        /// <summary>
        /// Normalizes a Hebrew word by replacing end-form characters with their in-word equivalents.
        /// </summary>
        /// <param name="hebrewWord"></param>
        /// <returns></returns>
        public static string NormalizeHebrew(string hebrewWord)
        {
            return Program.NormalizeHebrew(hebrewWord);
        }

        /// <summary>
        /// Removes accents from characters.
        /// This can be a useful fallback method if the model cannot make a prediction
        /// over a given word because it has not seen a particular accented character before.
        /// </summary>
        /// <param name="word"></param>
        /// <returns></returns>
        public static string StripAccents(string word)
        {
            return Program.StripAccent(word);
        }

        /// <summary>
        /// Creates a new training example with weight 1.
        /// </summary>
        /// <param name="sourceWord"></param>
        /// <param name="transliteratedWord"></param>
        public Example(string sourceWord, string transliteratedWord)
            : this(sourceWord, transliteratedWord, 1) { }

        /// <summary>
        /// Creates a new training example with the specified weight.
        /// </summary>
        /// <param name="sourceWord"></param>
        /// <param name="transliteratedWord"></param>
        /// <param name="weight"></param>
        public Example(string sourceWord, string transliteratedWord, double weight)
        {
            this.sourceWord = sourceWord;
            this.transliteratedWord = transliteratedWord;
            this.weight = weight;
        }

        internal Triple<string, string, double> Triple
        {
            get { return new Triple<string, string, double>(sourceWord, transliteratedWord, weight); }
        }

        /// <summary>
        /// Gets a "reversed" copy of this example, with the source and transliterated words swapped.
        /// </summary>
        public Example Reverse
        {
            get { return new Example(transliteratedWord, sourceWord, weight); }
        }

        /// <summary>
        /// The word in the source language.
        /// </summary>
        public string sourceWord;

        /// <summary>
        /// The transliterated word.
        /// </summary>
        public string transliteratedWord;

        /// <summary>
        /// The relative important of this example.  A weight of 1 means normal importance.
        /// </summary>
        public double weight;
    }
}
