using System;
using System.Collections.Generic;
using System.Text;
using Pasternack.Utility;

namespace SPTransliteration
{
    internal abstract class TransliterationModel
    {
        public abstract double GetProbability(string word1, string word2);
        public abstract TransliterationModel LearnModel(List<Triple<string, string, double>> examples);
    }
}
