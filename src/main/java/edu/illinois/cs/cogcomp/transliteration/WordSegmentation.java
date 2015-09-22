using System;
using System.Collections.Generic;
using System.Text;
using Pasternack.Collections.Generic;
using Pasternack;
using System.IO;
using System.Text.RegularExpressions;
using System.Globalization;
using Pasternack.Utility;
using Pasternack.Collections.Generic.Specialized;

namespace SPTransliteration
{
    internal class WordSegmentation
    {
        public static Dictionary<string,int> GetNgramCounts(IEnumerable<string> examples)
        {
            int maxLength=0;
            foreach (string example in examples) { maxLength = Math.Max(maxLength, example.Length); }

            Dictionary<string,int> result = new Dictionary<string,int>();

            for (int i = 1; i <= maxLength; i++)
                Dictionaries.AddTo<string>(result, WikiTransliteration.GetNgramCounts(i, examples, false), 1);

            return result;
        }

        public static SparseDoubleVector<string> GetNgramCounts(SparseDoubleVector<string> examples)
        {
            SparseDoubleVector<string> result = new SparseDoubleVector<string>();
            foreach (KeyValuePair<string, double> example in examples)
                for (int n = 1; n <= example.Key.Length; n++)
                    for (int i = 0; i <= example.Key.Length - n; i++)
                        result[example.Key.Substring(i, n)] += example.Value;

            return result;
        }



        private static string PadExample(string example, int contextSize)
        {
            return new string('_', contextSize) + example + new string('_', contextSize);
        }

        private static Triple<string,string,string> GetContextTriple(string originalWord, int index, int length, int contextSize)
        {
            return new Triple<string,string,string>(WikiTransliteration.GetLeftContext(originalWord,index,contextSize),
                originalWord.Substring(index,length),
                WikiTransliteration.GetRightContext(originalWord,index+length,contextSize));
        }

        public static SparseDoubleVector<Triple<string,string,string>> GetNgramCounts(SparseDoubleVector<string> examples, int contextSize, InternDictionary<string> internTable)
        {
            SparseDoubleVector<Triple<string,string,string>> result = new SparseDoubleVector<Triple<string,string,string>>();
            foreach (KeyValuePair<string, double> example in examples)
            {
                string paddedExample = PadExample(example.Key, contextSize);
                for (int n = 1; n <= example.Key.Length; n++)
                    for (int i = contextSize; i <= example.Key.Length - n + contextSize; i++)
                        result[ InternTriple( GetContextTriple(paddedExample, i, n, contextSize), internTable )] += example.Value;
            }

            return result;
        }

        public static void WriteVector(string filename, SparseDoubleVector<string> vector)
        {
            BinaryWriter writer = new BinaryWriter(new FileStream(filename, FileMode.Create));

            writer.Write(vector.Count);

            foreach (KeyValuePair<string, double> pair in vector)
            {
                writer.Write(pair.Key); writer.Write(pair.Value);
            }

            writer.Close();
        }

        public static SparseDoubleVector<string> ReadVector(string filename)
        {
            BinaryReader reader = new BinaryReader(new FileStream(filename, FileMode.Open));
            int count = reader.ReadInt32();
            SparseDoubleVector<string> result = new SparseDoubleVector<string>(count);

            for (int i = 0; i < count; i++)
                result[reader.ReadString()] = reader.ReadDouble();

            return result;
        }

        public static SparseDoubleVector<string> GetSegCounts(SparseDoubleVector<string> examples)
        {
            SparseDoubleVector<string> result = new SparseDoubleVector<string>();

            foreach (string example in examples.Keys)            
                result.Add(examples[example], GetSegCounts(example, new Dictionary<string, SparseDoubleVector<string>>()) / Math.Pow(2, example.Length - 1));                      

            return result;
        }

        public static SparseDoubleVector<string> GetSegCounts(string word, Dictionary<string, SparseDoubleVector<string>> memoizationTable)
        {
            SparseDoubleVector<string> result;
            if (memoizationTable.TryGetValue(word, out result)) return result;
            result = new SparseDoubleVector<string>();
            if (word.Length==0) return result;

            for (int i = 1; i <= word.Length; i++)
            {
                string ss = word.Substring(0, i);
                result[ss] += 1;
                result.Add(GetSegCounts(word.Substring(i),memoizationTable));
            }

            memoizationTable[word] = result;
            return result;
        }

        public static char[] vowels = new char[] { 'a', 'e', 'i', 'o', 'u', 'y' };

        public static int CountSyllables(string word)
        {
            word = StripAccent(word);
            int lastVowel = -2;

            int count = 0;

            for (int i = 0; i < word.Length; i++)
                if (Array.IndexOf<char>(vowels, word[i]) >= 0)
                {
                    if (i > lastVowel + 1)
                        count++;

                    lastVowel = i;
                }

            return count;
        }

        static string StripAccent(string stIn)
        {
            string normalized = stIn.Normalize(NormalizationForm.FormD);
            StringBuilder sb = new StringBuilder();

            foreach (char c in normalized)
            {
                UnicodeCategory uc = CharUnicodeInfo.GetUnicodeCategory(c);
                if (uc != UnicodeCategory.NonSpacingMark)
                {
                    sb.Append(c);
                }
            }
            return (sb.ToString());
        } 

        public static SparseDoubleVector<string> GetSegCounts(SparseDoubleVector<string> examples, SparseDoubleVector<string> probs)
        {
            SparseDoubleVector<string> result = new SparseDoubleVector<string>();

            SparseDoubleVector<string> occurences = GetNgramCounts(examples);

            foreach (string example in examples.Keys)
            {
                Pair<SparseDoubleVector<string>, double> raw = GetSegCounts(example, CountSyllables(example), probs, new Dictionary<Pair<int,string>,Pair<SparseDoubleVector<string>,double>>());
                
                int i;

                if (raw.y != 0)
                    result.Add(examples[example] / raw.y, raw.x);
                else
                    i = 0; //noop breakpoint
                    
            }

            //return result;
            return result / occurences;
        }

        public static SparseDoubleVector<string> GetSyllabicSegCounts(SparseDoubleVector<string> examples, SparseDoubleVector<string> probs)
        {
            SparseDoubleVector<string> result = new SparseDoubleVector<string>();

            SparseDoubleVector<string> occurences = GetNgramCounts(examples);

            foreach (string example in examples.Keys)
            {
                Pair<SparseDoubleVector<string>, double> raw = GetSyllabicSegCounts(example, probs, new Dictionary<string,Pair<SparseDoubleVector<string>,double>>());

                int i;

                if (raw.y != 0)
                    result.Add(examples[example] / raw.y, raw.x);
                else
                    i = 0; //noop breakpoint

            }

            //return result;
            return result / occurences;
        }

        public static Triple<string, string, string> InternTriple(Triple<string, string, string> triple, InternDictionary<string> internTable)
        {
            triple.x = internTable.Intern(triple.x);
            triple.y = internTable.Intern(triple.y);
            triple.z = internTable.Intern(triple.z);

            return triple;
        }

        public static SparseDoubleVector<Triple<string,string,string>> GetSyllabicSegCounts(SparseDoubleVector<string> examples, SparseDoubleVector<Triple<string,string,string>> occurences, SparseDoubleVector<Triple<string,string,string>> probs, int contextSize, InternDictionary<string> internTable)
        {
            //InternDictionary<string> internTable = new InternDictionary<string>();

            SparseDoubleVector<Triple<string, string, string>> result = new SparseDoubleVector<Triple<string,string,string>>();

            //SparseDoubleVector<Triple<string,string,string>> occurences = GetNgramCounts(examples, contextSize, internTable);

            foreach (string example in examples.Keys)
            {
                double weight = examples[example];
                string paddedExample = PadExample(example,contextSize);
                Pair<SparseDoubleVector<Pair<int, string>>, double> raw = GetSyllabicSegCounts(paddedExample,contextSize, example,contextSize, probs, new Dictionary<int,Pair<SparseDoubleVector<Pair<int,string>>,double>>());

                if (raw.y == 0)
                    continue; //shouldn't happen

                //SparseDoubleVector<Triple<string, string, string>> rawTriples = new SparseDoubleVector<Triple<string, string, string>>(raw.x.Count);
                foreach (KeyValuePair<Pair<int, string>, double> pair in raw.x)
                    result[ InternTriple( GetContextTriple(paddedExample, pair.Key.x, pair.Key.y.Length, contextSize), internTable)] += weight * (pair.Value / raw.y);                                

            }

            //return result/result.PNorm(1);
            return result / occurences;
        }

        public static Pair<SparseDoubleVector<Pair<int,string>>, double> GetSyllabicSegCounts(string originalWord, int position, string word, int contextSize, SparseDoubleVector<Triple<string,string,string>> probs, Dictionary<int, Pair<SparseDoubleVector<Pair<int,string>>, double>> memoizationTable)
        {
            Pair<SparseDoubleVector<Pair<int, string>>, double> result;
            if (memoizationTable.TryGetValue(position, out result)) return result;
            result = new Pair<SparseDoubleVector<Pair<int, string>>, double>(new SparseDoubleVector<Pair<int, string>>(), 0);

            if (word.Length == 0)
            {
                result.y = 1;
                return result;
            }

            int firstVowel = -1; int secondVowel = -1;
            for (int i = 0; i < word.Length; i++)
                if (Array.IndexOf<char>(vowels, word[i]) >= 0)
                    firstVowel = i;
                else if (firstVowel >= 0)
                    break;

            if (firstVowel == -1)
                firstVowel = word.Length - 1; //no vowels!

            for (int i = firstVowel + 1; i < word.Length; i++)
                if (Array.IndexOf<char>(vowels, word[i]) >= 0)
                {
                    secondVowel = i;
                    break;
                }

            if (secondVowel == -1 || (secondVowel == word.Length-1 && word[secondVowel] == 'e')) //if only one vowel, only consider the entire thing; note consideration of silent 'e' at end of words
            {
                firstVowel = word.Length - 1;
                secondVowel = word.Length;
            }            

            for (int i = firstVowel + 1; i <= secondVowel; i++)
            {                
                Triple<string,string,string> triple = GetContextTriple(originalWord,position,i,contextSize);

                double prob;
                if (probs.Count == 0) prob = 1;                
                else if (!probs.TryGetValue(triple, out prob) || prob == 0) continue;

                Pair<SparseDoubleVector<Pair<int, string>>, double> remainder = GetSyllabicSegCounts(originalWord, position + i, word.Substring(i), contextSize, probs, memoizationTable);

                result.x[new Pair<int,string>(position,triple.y)] += prob * remainder.y;
                result.x.Add(prob, remainder.x);
                result.y += prob * remainder.y;
            }

            memoizationTable[position] = result;
            return result;
        }

        public static Pair<SparseDoubleVector<string>, double> GetSyllabicSegCounts(string word, SparseDoubleVector<string> probs, Dictionary<string, Pair<SparseDoubleVector<string>, double>> memoizationTable)
        {
            Pair<SparseDoubleVector<string>, double> result;
            if (memoizationTable.TryGetValue(word, out result)) return result;
            result = new Pair<SparseDoubleVector<string>, double>(new SparseDoubleVector<string>(), 0);
            if (word.Length == 0)
            {
                result.y = 1;
                return result;
            }

            int firstVowel=-1; int secondVowel=-1;
            for (int i = 0; i < word.Length; i++)
                if (Array.IndexOf<char>(vowels,word[i])>=0)
                    firstVowel=i;
                else if (firstVowel >= 0)
                    break;

            if (firstVowel==-1) 
                firstVowel = word.Length-1; //no vowels!
      
            for (int i = firstVowel+1; i < word.Length; i++)
                if (Array.IndexOf<char>(vowels,word[i])>=0)
                {
                    secondVowel=i;
                    break;
                }

            if (secondVowel == -1) //if only one vowel, only consider the entire thing
            {
                firstVowel = word.Length - 1;
                secondVowel = word.Length;
            }


            for (int i = firstVowel+1; i <= secondVowel; i++)
            {                
                string ss = word.Substring(0, i);
                double prob;
                if (probs.Count == 0) prob = 1;
                else if (!probs.TryGetValue(ss, out prob) || prob == 0) continue;

                Pair<SparseDoubleVector<string>, double> remainder = GetSyllabicSegCounts(word.Substring(i), probs, memoizationTable);

                result.x[ss] += prob * remainder.y;
                result.x.Add(prob, remainder.x);
                result.y += prob * remainder.y;
            }

            memoizationTable[word] = result;
            return result;
        }

        public static Pair<SparseDoubleVector<string>, double> GetSegCounts(string word, int segmentCount, SparseDoubleVector<string> probs, Dictionary<Pair<int,string>, Pair<SparseDoubleVector<string>, double>> memoizationTable)
        {
            Pair<SparseDoubleVector<string>, double> result;
            if (memoizationTable.TryGetValue(new Pair<int,string>(segmentCount, word), out result)) return result;
            result = new Pair<SparseDoubleVector<string>, double>(new SparseDoubleVector<string>(), 0);
            if (word.Length == 0)
            {
                result.y = 1;
                return result;
            }
            else if (segmentCount == 0)
                return result; //probability 0

            for (int i = (segmentCount > 1 ? 1 : word.Length); i <= word.Length; i++)
            {
                string ss = word.Substring(0, i);
                double prob;
                if (!probs.TryGetValue(ss, out prob) || prob == 0) continue;

                Pair<SparseDoubleVector<string>, double> remainder = GetSegCounts(word.Substring(i), segmentCount - 1, probs, memoizationTable);

                result.x[ss] += prob * remainder.y;
                result.x.Add(prob,remainder.x);
                result.y += prob * remainder.y;
            }

            memoizationTable[new Pair<int,string>(segmentCount,word)] = result;
            return result;
        }

        public static void Learn(SparseDoubleVector<string> examples)
        {
            int contextSize = 3;
            //filter out examples with no vowels
            examples = examples.Filter(delegate(string arg) { for (int j = 0; j < arg.Length; j++) if (Array.IndexOf<char>(vowels, arg[j]) >= 0) return true; return false; });

            //examples = examples.Sign();

            InternDictionary<string> internTable = new InternDictionary<string>();
            SparseDoubleVector<Triple<string, string, string>> occurences = GetNgramCounts(examples, contextSize, internTable);

            SparseDoubleVector<Triple<string, string, string>> probs = GetSyllabicSegCounts(examples, occurences, new SparseDoubleVector<Triple<string, string, string>>(), contextSize, internTable);
            //SparseDoubleVector<string> probs = ConvertToVector( GetNgramCounts(examples) );           
            //probs /= probs.PNorm(1);

            int i = 0;
            while (true)
            {
                //probs /= probs.PNorm(1);

                Console.WriteLine("EM Iteration #" + i);
                OutputSegmentations("ninja", 5, probs,contextSize);
                OutputSegmentations("richard", 5, probs,contextSize);
                OutputSegmentations("sarah", 5, probs,contextSize);
                Console.WriteLine();
                Console.WriteLine();

                probs = GetSyllabicSegCounts(examples, occurences, probs, contextSize, internTable);
                
                i++;
            }

            Console.WriteLine("Finished.");
            Console.ReadLine();
        }

        public static void WriteCounts(string filename, Dictionary<string, int> counts)
        {
            StreamWriter writer = new StreamWriter(filename);
            foreach (KeyValuePair<string, int> pair in counts)
                writer.WriteLine(pair.Key + "\t" + pair.Value);

            writer.Close();
        }

        public static Dictionary<string, int> ReadCounts(string filename)
        {
            Dictionary<string, int> result = new Dictionary<string, int>();
            StreamReader reader = new StreamReader(filename);
            string line;
            while ((line = reader.ReadLine()) != null)
            {
                string[] segs = line.Split('\t');
                result.Add(segs[0], int.Parse(segs[1]));
            }

            return result;
        }

        public static SparseDoubleVector<string> ConvertToVector(Dictionary<string, int> counts)
        {
            SparseDoubleVector<string> vector = new SparseDoubleVector<string>(counts.Count);
            foreach (KeyValuePair<string, int> pair in counts) vector.Add(pair.Key, pair.Value);
            return vector;
        }

        public static SparseDoubleVector<string> Normalize(SparseDoubleVector<string> probs)
        {
            Dictionary<int, SparseDoubleVector<string>> sVectors = probs.Split<int>(delegate(string arg) { return arg.Length; });
            probs = new SparseDoubleVector<string>(probs.Count);
            foreach (KeyValuePair<int, SparseDoubleVector<string>> pair in sVectors)
                probs.Add(pair.Value / pair.Value.PNorm(1));

            return probs;
        }

        public static void OutputSegmentations(string word, int count, SparseDoubleVector<string> probs)
        {
            List<KeyValuePair<string, double>> segs = GetSegmentations(word, probs);
            Console.WriteLine("Segmenting: " + word);
            for (int i = 0; i < count && i < segs.Count; i++)
                Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            Console.WriteLine();
        }

        public static void OutputSegmentations(string word, int count, SparseDoubleVector<Triple<string,string,string>> probs, int contextSize)
        {
            List<KeyValuePair<string, double>> segs = GetSegmentations(word, probs, contextSize);
            Console.WriteLine("Segmenting: " + word);
            for (int i = 0; i < count && i < segs.Count; i++)
                Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            Console.WriteLine();
        }

        public static void Interactive2(string filename)
        {
            SparseDoubleVector<string> probs = ReadVector(filename);

            probs = probs / probs.PNorm(1);



            Console.WriteLine("Enter a word to segment and press return:");

            while (true)
            {
                string nextLine = Console.ReadLine();
                if (nextLine.Length == 0) return;

                List<KeyValuePair<string, double>> segs = GetSegmentations(nextLine, probs);
                for (int i = 0; i < 20 && i < segs.Count; i++)
                    Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            }
        }

        public static  void Interactive(string filename)
        {
            SparseDoubleVector<string> probs = ConvertToVector(ReadCounts(filename));

            //Dictionary<int, SparseDoubleVector<string>> sVectors = probs.Split<int>(delegate(string arg) { return arg.Length; });
            //probs = new SparseDoubleVector<string>(probs.Count);
            //foreach (KeyValuePair<int, SparseDoubleVector<string>> pair in sVectors)
            //    probs.Add(pair.Value * pair.Key);            

            //probs = Normalize(probs);

            probs = probs / probs.PNorm(1);

            //SparseDoubleVector<string> nProbs = new SparseDoubleVector<string>(probs.Count);
            //foreach (KeyValuePair<string, double> pair in probs)
            //    if (pair.Key.Length == 1) nProbs.Add(pair.Key, pair.Value);
            //    else  nProbs.Add(pair.Key, pair.Value * (pair.Value / (probs[pair.Key.Substring(0, pair.Key.Length - 1)] * probs[pair.Key.Substring(pair.Key.Length - 1)])));

            //probs = nProbs / nProbs.PNorm(1);

            //probs = Normalize(nProbs);

            //probs = probs / probs.PNorm(1);

            Console.WriteLine("Enter a word to segment and press return:");

            while (true)
            {
                string nextLine = Console.ReadLine();
                if (nextLine.Length==0) return;

                List<KeyValuePair<string, double>> segs = GetSegmentations(nextLine, probs);
                for (int i = 0; i < 20 && i < segs.Count; i++)
                    Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            }
        }

        public static List<KeyValuePair<string, double>> GetSegmentations(string word, SparseDoubleVector<string> probs)
        {
            List<KeyValuePair<string, double>> result = GetSegmentations(word, probs, new Dictionary<string, List<KeyValuePair<string, double>>>());
            result.Sort(new Comparison<KeyValuePair<string, double>>(delegate(KeyValuePair<string, double> a, KeyValuePair<string, double> b) { return Math.Sign( b.Value - a.Value ); }));
            return result;
        }

        private static List<KeyValuePair<string, double>> GetSegmentations(string word, SparseDoubleVector<string> probs, Dictionary<string, List<KeyValuePair<string,double>>> memoizationTable)
        {
            List<KeyValuePair<string,double>> result;
            if (memoizationTable.TryGetValue(word, out result))
                return result;

            result = new List<KeyValuePair<string,double>>();
            if (word.Length==0)
            {
                result.Add(new KeyValuePair<string,double>("",1));
                return result;
            }            

            for (int i = 1; i <= word.Length; i++)
            {
                List<KeyValuePair<string, double>> remainder = GetSegmentations(word.Substring(i), probs, memoizationTable);
                string ss = word.Substring(0,i);
                double prob = probs[ss];
                if (prob==0) continue;
                foreach (KeyValuePair<string,double> pair in remainder)                                                                            
                    result.Add(new KeyValuePair<string,double>(ss+ (pair.Key.Length > 0 ? "-"+pair.Key : ""),prob*pair.Value));
            }

            memoizationTable[word] = result;
            return result;

        }

        public static List<KeyValuePair<string, double>> GetSegmentations(string word, SparseDoubleVector<Triple<string,string,string>> probs, int contextSize)
        {
            List<KeyValuePair<string, double>> result = GetSegmentations(contextSize,PadExample(word,contextSize), contextSize, probs, new Dictionary<int, List<KeyValuePair<string, double>>>());
            result.Sort(new Comparison<KeyValuePair<string, double>>(delegate(KeyValuePair<string, double> a, KeyValuePair<string, double> b) { return Math.Sign(b.Value - a.Value); }));
            return result;
        }

        private static List<KeyValuePair<string, double>> GetSegmentations(int position, string originalWord, int contextSize, SparseDoubleVector<Triple<string,string,string>> probs, Dictionary<int, List<KeyValuePair<string, double>>> memoizationTable)
        {
            List<KeyValuePair<string, double>> result;
            if (memoizationTable.TryGetValue(position, out result))
                return result;

            result = new List<KeyValuePair<string, double>>();
            if (originalWord.Length -contextSize == position)
            {
                result.Add(new KeyValuePair<string, double>("", 1));
                return result;
            }

            for (int i = 1; i <= originalWord.Length - contextSize - position; i++)
            {
                List<KeyValuePair<string, double>> remainder = GetSegmentations(position + i, originalWord, contextSize, probs, memoizationTable);
                Triple<string, string, string> triple = GetContextTriple(originalWord, position, i, contextSize);                
                double prob = probs[triple];
                if (prob == 0) continue;
                foreach (KeyValuePair<string, double> pair in remainder)
                    result.Add(new KeyValuePair<string, double>(triple.y + (pair.Key.Length > 0 ? "-" + pair.Key : ""), prob * pair.Value));
            }

            memoizationTable[position] = result;
            return result;

        }


        public static bool ProperCase(string word)
        {
            for (int i = 1; i < word.Length; i++)
                if (char.IsUpper(word, i)) return false;

            return true;
        }

        public static List<string> SplitWords(string text)
        {
         
            string[] words = Regex.Split(text, "\\W|·|\\d", RegexOptions.Compiled);
            List<string> result = new List<string>(words.Length);
            foreach (string word in words)
                if (word.Length > 0 && ProperCase(word))
                    result.Add(word);

            return result;
        }
    }
}
