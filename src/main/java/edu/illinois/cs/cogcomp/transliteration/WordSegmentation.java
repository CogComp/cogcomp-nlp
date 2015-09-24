package edu.illinois.cs.cogcomp.transliteration;


import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

class WordSegmentation
    {
        public static HashMap<String,Integer> GetNgramCounts(Iterable<String> examples)
        {
            int maxLength=0;
            for (String example : examples) { maxLength = Math.max(maxLength, example.length()); }

            HashMap<String,Integer> result = new HashMap<String,Integer>();

            for (int i = 1; i <= maxLength; i++)
                Dictionaries.AddTo<String>(result, WikiTransliteration.GetNgramCounts(i, examples, false), 1);

            return result;
        }

        public static SparseDoubleVector<String> GetNgramCounts(SparseDoubleVector<String> examples)
        {
            SparseDoubleVector<String> result = new SparseDoubleVector<String>();
            for (KeyValuePair<String, double> example : examples)
                for (int n = 1; n <= example.Key.Length; n++)
                    for (int i = 0; i <= example.Key.Length - n; i++)
                        result[example.Key.Substring(i, n)] += example.Value;

            return result;
        }



        private static String PadExample(String example, int contextSize)
        {
            return StringUtils.repeat('_', contextSize) + example + StringUtils.repeat('_', contextSize);
        }

        private static Triple<String,String,String> GetContextTriple(String originalWord, int index, int length, int contextSize)
        {
            return new Triple<String,String,String>(WikiTransliteration.GetLeftContext(originalWord,index,contextSize),
                originalWord.Substring(index,length),
                WikiTransliteration.GetRightContext(originalWord,index+length,contextSize));
        }

        public static SparseDoubleVector<Triple<String,String,String>> GetNgramCounts(SparseDoubleVector<String> examples, int contextSize, InternDictionary<String> internTable)
        {
            SparseDoubleVector<Triple<String,String,String>> result = new SparseDoubleVector<Triple<String,String,String>>();
            for (KeyValuePair<String, Double> example : examples)
            {
                String paddedExample = PadExample(example.Key, contextSize);
                for (int n = 1; n <= example.Key.Length; n++)
                    for (int i = contextSize; i <= example.Key.Length - n + contextSize; i++)
                        result[ InternTriple( GetContextTriple(paddedExample, i, n, contextSize), internTable )] += example.Value;
            }

            return result;
        }

        public static void WriteVector(String filename, SparseDoubleVector<String> vector)
        {
            BinaryWriter writer = new BinaryWriter(new FileStream(filename, FileMode.Create));

            writer.Write(vector.Count);

            for (KeyValuePair<String, double> pair : vector)
            {
                writer.Write(pair.Key); writer.Write(pair.Value);
            }

            writer.Close();
        }

        public static SparseDoubleVector<String> ReadVector(String filename)
        {
            BinaryReader reader = new BinaryReader(new FileStream(filename, FileMode.Open));
            int count = reader.ReadInt32();
            SparseDoubleVector<String> result = new SparseDoubleVector<String>(count);

            for (int i = 0; i < count; i++)
                result[reader.ReadString()] = reader.ReadDouble();

            return result;
        }

        public static SparseDoubleVector<String> GetSegCounts(SparseDoubleVector<String> examples)
        {
            SparseDoubleVector<String> result = new SparseDoubleVector<String>();

            for (String example : examples.Keys)
                result.Add(examples[example], GetSegCounts(example, new Dictionary<String, SparseDoubleVector<String>>()) / Math.Pow(2, example.Length - 1));

            return result;
        }

        public static SparseDoubleVector<String> GetSegCounts(String word, Dictionary<String, SparseDoubleVector<String>> memoizationTable)
        {
            SparseDoubleVector<String> result;
            if (memoizationTable.TryGetValue(word, out result)) return result;
            result = new SparseDoubleVector<String>();
            if (word.Length==0) return result;

            for (int i = 1; i <= word.Length; i++)
            {
                String ss = word.Substring(0, i);
                result[ss] += 1;
                result.Add(GetSegCounts(word.Substring(i),memoizationTable));
            }

            memoizationTable[word] = result;
            return result;
        }

        public static char[] vowels = new char[] { 'a', 'e', 'i', 'o', 'u', 'y' };

        public static int CountSyllables(String word)
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

        static String StripAccent(String stIn)
        {
            String normalized = stIn.Normalize(NormalizationForm.FormD);
            StringBuilder sb = new StringBuilder();

            for (char c : normalized)
            {
                UnicodeCategory uc = CharUnicodeInfo.GetUnicodeCategory(c);
                if (uc != UnicodeCategory.NonSpacingMark)
                {
                    sb.Append(c);
                }
            }
            return (sb.ToString());
        } 

        public static SparseDoubleVector<String> GetSegCounts(SparseDoubleVector<String> examples, SparseDoubleVector<String> probs)
        {
            SparseDoubleVector<String> result = new SparseDoubleVector<String>();

            SparseDoubleVector<String> occurences = GetNgramCounts(examples);

            for (String example : examples.Keys)
            {
                Pair<SparseDoubleVector<String>, Double> raw = GetSegCounts(example, CountSyllables(example), probs, new Dictionary<Pair<int,String>,Pair<SparseDoubleVector<String>,double>>());
                
                int i;

                if (raw.y != 0)
                    result.Add(examples[example] / raw.y, raw.x);
                else
                    i = 0; //noop breakpoint
                    
            }

            //return result;
            return result / occurences;
        }

        public static SparseDoubleVector<String> GetSyllabicSegCounts(SparseDoubleVector<String> examples, SparseDoubleVector<String> probs)
        {
            SparseDoubleVector<String> result = new SparseDoubleVector<String>();

            SparseDoubleVector<String> occurences = GetNgramCounts(examples);

            for (String example : examples.Keys)
            {
                Pair<SparseDoubleVector<String>, Double> raw = GetSyllabicSegCounts(example, probs, new Dictionary<String,Pair<SparseDoubleVector<String>,double>>());

                int i;

                if (raw.y != 0)
                    result.Add(examples[example] / raw.y, raw.x);
                else
                    i = 0; //noop breakpoint

            }

            //return result;
            return result / occurences;
        }

        public static Triple<String, String, String> InternTriple(Triple<String, String, String> triple, InternDictionary<String> internTable)
        {
            triple.x = internTable.Intern(triple.x);
            triple.y = internTable.Intern(triple.y);
            triple.z = internTable.Intern(triple.z);

            return triple;
        }

        public static SparseDoubleVector<Triple<String,String,String>> GetSyllabicSegCounts(SparseDoubleVector<String> examples, SparseDoubleVector<Triple<String,String,String>> occurences, SparseDoubleVector<Triple<String,String,String>> probs, int contextSize, InternDictionary<String> internTable)
        {
            //InternDictionary<String> internTable = new InternDictionary<String>();

            SparseDoubleVector<Triple<String, String, String>> result = new SparseDoubleVector<Triple<String,String,String>>();

            //SparseDoubleVector<Triple<String,String,String>> occurences = GetNgramCounts(examples, contextSize, internTable);

            for (String example : examples.Keys)
            {
                double weight = examples[example];
                String paddedExample = PadExample(example,contextSize);
                Pair<SparseDoubleVector<Pair<int, String>>, double> raw = GetSyllabicSegCounts(paddedExample,contextSize, example,contextSize, probs, new Dictionary<int,Pair<SparseDoubleVector<Pair<int,String>>,double>>());

                if (raw.y == 0)
                    continue; //shouldn't happen

                //SparseDoubleVector<Triple<String, String, String>> rawTriples = new SparseDoubleVector<Triple<String, String, String>>(raw.x.Count);
                for (KeyValuePair<Pair<int, String>, double> pair in raw.x)
                    result[ InternTriple( GetContextTriple(paddedExample, pair.Key.x, pair.Key.y.Length, contextSize), internTable)] += weight * (pair.Value / raw.y);                                

            }

            //return result/result.PNorm(1);
            return result / occurences;
        }

        public static Pair<SparseDoubleVector<Pair<int,String>>, double> GetSyllabicSegCounts(String originalWord, int position, String word, int contextSize, SparseDoubleVector<Triple<String,String,String>> probs, Dictionary<int, Pair<SparseDoubleVector<Pair<int,String>>, double>> memoizationTable)
        {
            Pair<SparseDoubleVector<Pair<int, String>>, double> result;
            if (memoizationTable.TryGetValue(position, out result)) return result;
            result = new Pair<SparseDoubleVector<Pair<int, String>>, double>(new SparseDoubleVector<Pair<int, String>>(), 0);

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
                Triple<String,String,String> triple = GetContextTriple(originalWord,position,i,contextSize);

                double prob;
                if (probs.Count == 0) prob = 1;                
                else if (!probs.TryGetValue(triple, out prob) || prob == 0) continue;

                Pair<SparseDoubleVector<Pair<int, String>>, double> remainder = GetSyllabicSegCounts(originalWord, position + i, word.Substring(i), contextSize, probs, memoizationTable);

                result.x[new Pair<int,String>(position,triple.y)] += prob * remainder.y;
                result.x.Add(prob, remainder.x);
                result.y += prob * remainder.y;
            }

            memoizationTable[position] = result;
            return result;
        }

        public static Pair<SparseDoubleVector<String>, double> GetSyllabicSegCounts(String word, SparseDoubleVector<String> probs, Dictionary<String, Pair<SparseDoubleVector<String>, double>> memoizationTable)
        {
            Pair<SparseDoubleVector<String>, double> result;
            if (memoizationTable.TryGetValue(word, out result)) return result;
            result = new Pair<SparseDoubleVector<String>, double>(new SparseDoubleVector<String>(), 0);
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
                String ss = word.Substring(0, i);
                double prob;
                if (probs.Count == 0) prob = 1;
                else if (!probs.TryGetValue(ss, out prob) || prob == 0) continue;

                Pair<SparseDoubleVector<String>, double> remainder = GetSyllabicSegCounts(word.Substring(i), probs, memoizationTable);

                result.x[ss] += prob * remainder.y;
                result.x.Add(prob, remainder.x);
                result.y += prob * remainder.y;
            }

            memoizationTable[word] = result;
            return result;
        }

        public static Pair<SparseDoubleVector<String>, double> GetSegCounts(String word, int segmentCount, SparseDoubleVector<String> probs, Dictionary<Pair<int,String>, Pair<SparseDoubleVector<String>, double>> memoizationTable)
        {
            Pair<SparseDoubleVector<String>, double> result;
            if (memoizationTable.TryGetValue(new Pair<int,String>(segmentCount, word), out result)) return result;
            result = new Pair<SparseDoubleVector<String>, double>(new SparseDoubleVector<String>(), 0);
            if (word.Length == 0)
            {
                result.y = 1;
                return result;
            }
            else if (segmentCount == 0)
                return result; //probability 0

            for (int i = (segmentCount > 1 ? 1 : word.Length); i <= word.Length; i++)
            {
                String ss = word.Substring(0, i);
                double prob;
                if (!probs.TryGetValue(ss, out prob) || prob == 0) continue;

                Pair<SparseDoubleVector<String>, double> remainder = GetSegCounts(word.Substring(i), segmentCount - 1, probs, memoizationTable);

                result.x[ss] += prob * remainder.y;
                result.x.Add(prob,remainder.x);
                result.y += prob * remainder.y;
            }

            memoizationTable[new Pair<int,String>(segmentCount,word)] = result;
            return result;
        }

        public static void Learn(SparseDoubleVector<String> examples)
        {
            int contextSize = 3;
            //filter out examples with no vowels
            examples = examples.Filter(delegate(String arg) { for (int j = 0; j < arg.Length; j++) if (Array.IndexOf<char>(vowels, arg[j]) >= 0) return true; return false; });

            //examples = examples.Sign();

            InternDictionary<String> internTable = new InternDictionary<String>();
            SparseDoubleVector<Triple<String, String, String>> occurences = GetNgramCounts(examples, contextSize, internTable);

            SparseDoubleVector<Triple<String, String, String>> probs = GetSyllabicSegCounts(examples, occurences, new SparseDoubleVector<Triple<String, String, String>>(), contextSize, internTable);
            //SparseDoubleVector<String> probs = ConvertToVector( GetNgramCounts(examples) );
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

        public static void WriteCounts(String filename, Dictionary<String, int> counts)
        {
            StreamWriter writer = new StreamWriter(filename);
            for (KeyValuePair<String, int> pair in counts)
                writer.WriteLine(pair.Key + "\t" + pair.Value);

            writer.Close();
        }

        public static Dictionary<String, int> ReadCounts(String filename)
        {
            Dictionary<String, int> result = new Dictionary<String, int>();
            StreamReader reader = new StreamReader(filename);
            String line;
            while ((line = reader.ReadLine()) != null)
            {
                String[] segs = line.Split('\t');
                result.Add(segs[0], int.Parse(segs[1]));
            }

            return result;
        }

        public static SparseDoubleVector<String> ConvertToVector(Dictionary<String, int> counts)
        {
            SparseDoubleVector<String> vector = new SparseDoubleVector<String>(counts.Count);
            for (KeyValuePair<String, int> pair in counts) vector.Add(pair.Key, pair.Value);
            return vector;
        }

        public static SparseDoubleVector<String> Normalize(SparseDoubleVector<String> probs)
        {
            Dictionary<int, SparseDoubleVector<String>> sVectors = probs.Split<int>(delegate(String arg) { return arg.Length; });
            probs = new SparseDoubleVector<String>(probs.Count);
            for (KeyValuePair<int, SparseDoubleVector<String>> pair in sVectors)
                probs.Add(pair.Value / pair.Value.PNorm(1));

            return probs;
        }

        public static void OutputSegmentations(String word, int count, SparseDoubleVector<String> probs)
        {
            List<KeyValuePair<String, double>> segs = GetSegmentations(word, probs);
            Console.WriteLine("Segmenting: " + word);
            for (int i = 0; i < count && i < segs.Count; i++)
                Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            Console.WriteLine();
        }

        public static void OutputSegmentations(String word, int count, SparseDoubleVector<Triple<String,String,String>> probs, int contextSize)
        {
            List<KeyValuePair<String, double>> segs = GetSegmentations(word, probs, contextSize);
            Console.WriteLine("Segmenting: " + word);
            for (int i = 0; i < count && i < segs.Count; i++)
                Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            Console.WriteLine();
        }

        public static void Interactive2(String filename)
        {
            SparseDoubleVector<String> probs = ReadVector(filename);

            probs = probs / probs.PNorm(1);



            Console.WriteLine("Enter a word to segment and press return:");

            while (true)
            {
                String nextLine = Console.ReadLine();
                if (nextLine.Length == 0) return;

                List<KeyValuePair<String, double>> segs = GetSegmentations(nextLine, probs);
                for (int i = 0; i < 20 && i < segs.Count; i++)
                    Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            }
        }

        public static  void Interactive(String filename)
        {
            SparseDoubleVector<String> probs = ConvertToVector(ReadCounts(filename));

            //Dictionary<int, SparseDoubleVector<String>> sVectors = probs.Split<int>(delegate(String arg) { return arg.Length; });
            //probs = new SparseDoubleVector<String>(probs.Count);
            //foreach (KeyValuePair<int, SparseDoubleVector<String>> pair in sVectors)
            //    probs.Add(pair.Value * pair.Key);            

            //probs = Normalize(probs);

            probs = probs / probs.PNorm(1);

            //SparseDoubleVector<String> nProbs = new SparseDoubleVector<String>(probs.Count);
            //foreach (KeyValuePair<String, double> pair in probs)
            //    if (pair.Key.Length == 1) nProbs.Add(pair.Key, pair.Value);
            //    else  nProbs.Add(pair.Key, pair.Value * (pair.Value / (probs[pair.Key.Substring(0, pair.Key.Length - 1)] * probs[pair.Key.Substring(pair.Key.Length - 1)])));

            //probs = nProbs / nProbs.PNorm(1);

            //probs = Normalize(nProbs);

            //probs = probs / probs.PNorm(1);

            Console.WriteLine("Enter a word to segment and press return:");

            while (true)
            {
                String nextLine = Console.ReadLine();
                if (nextLine.Length==0) return;

                List<KeyValuePair<String, double>> segs = GetSegmentations(nextLine, probs);
                for (int i = 0; i < 20 && i < segs.Count; i++)
                    Console.WriteLine("==>\t" + segs[i].Key + "\t\t" + segs[i].Value);
            }
        }

        public static List<KeyValuePair<String, double>> GetSegmentations(String word, SparseDoubleVector<String> probs)
        {
            List<KeyValuePair<String, double>> result = GetSegmentations(word, probs, new Dictionary<String, List<KeyValuePair<String, double>>>());
            result.Sort(new Comparison<KeyValuePair<String, double>>(delegate(KeyValuePair<String, double> a, KeyValuePair<String, double> b) { return Math.Sign( b.Value - a.Value ); }));
            return result;
        }

        private static List<KeyValuePair<String, double>> GetSegmentations(String word, SparseDoubleVector<String> probs, Dictionary<String, List<KeyValuePair<String,double>>> memoizationTable)
        {
            List<KeyValuePair<String,double>> result;
            if (memoizationTable.TryGetValue(word, out result))
                return result;

            result = new List<KeyValuePair<String,double>>();
            if (word.Length==0)
            {
                result.Add(new KeyValuePair<String,double>("",1));
                return result;
            }            

            for (int i = 1; i <= word.Length; i++)
            {
                List<KeyValuePair<String, double>> remainder = GetSegmentations(word.Substring(i), probs, memoizationTable);
                String ss = word.Substring(0,i);
                double prob = probs[ss];
                if (prob==0) continue;
                for (KeyValuePair<String,double> pair in remainder)
                    result.Add(new KeyValuePair<String,double>(ss+ (pair.Key.Length > 0 ? "-"+pair.Key : ""),prob*pair.Value));
            }

            memoizationTable[word] = result;
            return result;

        }

        public static List<KeyValuePair<String, double>> GetSegmentations(String word, SparseDoubleVector<Triple<String,String,String>> probs, int contextSize)
        {
            List<KeyValuePair<String, double>> result = GetSegmentations(contextSize,PadExample(word,contextSize), contextSize, probs, new Dictionary<int, List<KeyValuePair<String, double>>>());
            result.Sort(new Comparison<KeyValuePair<String, double>>(delegate(KeyValuePair<String, double> a, KeyValuePair<String, double> b) { return Math.Sign(b.Value - a.Value); }));
            return result;
        }

        private static List<KeyValuePair<String, double>> GetSegmentations(int position, String originalWord, int contextSize, SparseDoubleVector<Triple<String,String,String>> probs, Dictionary<int, List<KeyValuePair<String, double>>> memoizationTable)
        {
            List<KeyValuePair<String, double>> result;
            if (memoizationTable.TryGetValue(position, out result))
                return result;

            result = new List<KeyValuePair<String, double>>();
            if (originalWord.Length -contextSize == position)
            {
                result.Add(new KeyValuePair<String, double>("", 1));
                return result;
            }

            for (int i = 1; i <= originalWord.Length - contextSize - position; i++)
            {
                List<KeyValuePair<String, double>> remainder = GetSegmentations(position + i, originalWord, contextSize, probs, memoizationTable);
                Triple<String, String, String> triple = GetContextTriple(originalWord, position, i, contextSize);
                double prob = probs[triple];
                if (prob == 0) continue;
                for (KeyValuePair<String, double> pair in remainder)
                    result.Add(new KeyValuePair<String, double>(triple.y + (pair.Key.Length > 0 ? "-" + pair.Key : ""), prob * pair.Value));
            }

            memoizationTable[position] = result;
            return result;

        }


        public static bool ProperCase(String word)
        {
            for (int i = 1; i < word.Length; i++)
                if (char.IsUpper(word, i)) return false;

            return true;
        }

        public static List<String> SplitWords(String text)
        {
         
            String[] words = Regex.Split(text, "\\W|�|\\d", RegexOptions.Compiled);
            List<String> result = new List<String>(words.Length);
            for (String word in words)
                if (word.Length > 0 && ProperCase(word))
                    result.Add(word);

            return result;
        }
    }
}
