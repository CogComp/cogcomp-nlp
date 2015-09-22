using System;
using System.Collections.Generic;
using System.Text;
using Pasternack.Collections.Generic.Specialized;
using Pasternack.Collections.Generic;
using Pasternack.Utility;
using Pasternack;
using System.IO;

namespace SPTransliteration
{
    internal static class WordCompression
    {
        public static void SaveSet(Set<string> set, string filename)
        {
            StreamWriter writer = new StreamWriter(filename);
            foreach (string word in set)
                writer.WriteLine(word);

            writer.Close();
        }

        public static List<string> GetNgrams(string example)
        {
            Dictionary<string, bool> ngramList = new Dictionary<string, bool>();
            for (int n = 1; n <= example.Length; n++)
                for (int i = 0; i <= example.Length - n; i++)
                    ngramList[example.Substring(i, n)] = true;

            return new List<string>(ngramList.Keys);
        }

        private static int MinChunkCount(string example, Set<string> chunks)
        {
            //jon
            int[] counts = new int[example.Length+1];
            for (int i = example.Length - 1; i >= 0; i--)
            {
                counts[i] = int.MaxValue;
                int maxLength = example.Length-i;
                for (int j = 1; j <= maxLength; j++)
                {
                    if (chunks.Contains(example.Substring(i, j)))
                        counts[i] = Math.Min(1 + counts[i + j], counts[i]);
                }
            }

            return counts[0];
        }

        private static string[] MinChunks(string example, Set<string> chunks)
        {
            //jon
            int[] counts = new int[example.Length + 1];
            string[] bestString = new string[example.Length];
            for (int i = example.Length - 1; i >= 0; i--)
            {
                counts[i] = int.MaxValue;
                int maxLength = example.Length - i;
                for (int j = 1; j <= maxLength; j++)
                {
                    string ss = example.Substring(i, j);
                    if (chunks.Contains(ss))
                    {
                        int addCount = 1 + counts[i + j];
                        if (addCount < counts[i])
                        {
                            counts[i] = addCount;
                            bestString[i] = ss;
                        }
                    }
                }
            }

            if (bestString[0] == null) return null;

            string[] result = new string[counts[0]];

            int nextOffset = 0;
            for (int i = 0; i < result.Length; i++)
            {
                result[i] = bestString[nextOffset];
                nextOffset += result[i].Length;
            }

            return result;
        }

        //private static int MinChunkCount(string example, Set<string> chunks, Dictionary<string, int> memoizationTable)
        //{
        //    if (example.Length == 0) return 0;

        //    int minimum = int.MaxValue;

        //    for (int i = 1; i <= example.Length; i++)
        //    {
        //        if (chunks.Contains(example.Substring(0, i)))
        //            minimum = Math.Min(minimum, 1+MinChunkCount(example.Substring(i), chunks, memoizationTable));
        //    }

        //    memoizationTable[example] = minimum;
        //    return minimum;
        //}

        //public static void Compress2(SparseDoubleVector<string> examples)
        //{
        //    InternDictionary<string> internTable = new InternDictionary<string>();

        //    //build a map from substrings to words
        //    Dictionary<string, List<string>> substringMap = new Dictionary<string, List<string>>();
        //    foreach (string example in examples.Keys)
        //        foreach (string ngram in GetNgrams(example))
        //        {
        //            List<string> wordList;
        //            if (!substringMap.TryGetValue(ngram, out wordList))
        //                wordList = substringMap[ngram] = new List<string>();

        //            wordList.Add(example);
        //        }

        //    //initialize the chunk set
        //    SparseDoubleVector<string> chunks = new SparseDoubleVector<string>();
        //    foreach (string ngram in substringMap.Keys)
        //        if (ngram.Length == 1) chunks.Add(ngram, 0);

        //    Dictionary<string, int> chunksRequired = new Dictionary<string, int>(examples.Count);
        //    int totalSegments = 0;
        //    foreach (string example in examples.Keys)
        //        totalSegments += ((int)examples[example]) * (chunksRequired[example] = MinChunkCount(example, chunks));

        //    int chunksLength = chunks.Count;

        //    double currentScore = totalSegments * Math.Log(chunks.Count, 2) + 8 * (chunksLength + chunks.Count); //initial score

        //    Console.WriteLine("Initial score = " + currentScore + "; " + totalSegments + " segments; " + chunks.Count + " chunks.");

        //    int round = 0;

        //    while (true)
        //    {
        //        round++;

        //        double bestScore = currentScore;
        //        string bestMove = null;
        //        //Set<string> chunksCopy = new Set<string>(chunks);
        //        foreach (string ngram in examples.Keys)
        //        {
        //            if (chunks.Contains(ngram))
        //            {
        //                chunks.Remove(ngram); //try deleting it
        //                chunksLength -= ngram.Length;
        //            }
        //            else
        //            {
        //                chunks.Add(ngram); //try adding it
        //                chunksLength += ngram.Length;
        //            }

        //            int chunkChange = 0;
        //            bool impossible = false;
        //            foreach (string example in substringMap[ngram])
        //            {
        //                int cc = MinChunkCount(example, chunks);
        //                if (cc == int.MaxValue)
        //                {
        //                    impossible = true;
        //                    break;
        //                }
        //                chunkChange += ((int)examples[example]) * (cc - chunksRequired[example]);
        //            }

        //            if (!impossible)
        //            {
        //                double newScore = (totalSegments + chunkChange) * Math.Log(chunks.Count, 2) + 8 * (chunksLength + chunks.Count);
        //                if (newScore < bestScore)
        //                {
        //                    bestScore = newScore;
        //                    bestMove = ngram;
        //                }
        //            }

        //            if (chunks.Contains(ngram))
        //            {
        //                chunks.Remove(ngram); //try deleting it
        //                chunksLength -= ngram.Length;
        //            }
        //            else
        //            {
        //                chunks.Add(ngram); //try adding it
        //                chunksLength += ngram.Length;
        //            }


        //        }

        //        if (bestMove == null)
        //        {
        //            Console.WriteLine("Finished.  Local max found.  Return to quit.");
        //            SaveSet(chunks, @"C:\Data\WikiTransliteration\Segmentation\chunks.txt");
        //            Console.ReadLine();
        //            return;
        //        }
        //        else
        //        {
        //            Console.WriteLine("Compressing (round " + round + "): Old score = " + currentScore + "; new score = " + bestScore);
        //            Console.WriteLine("Segments per word: " + (((double)totalSegments) / examples.Count) + "; " + totalSegments + " segments; " + chunks.Count + " chunks (length = " + chunksLength + ")");
        //            if (!chunks.Contains(bestMove)) Console.WriteLine("Adding " + bestMove);
        //            else Console.WriteLine("Removing " + bestMove);
        //            Console.WriteLine();

        //            currentScore = bestScore;

        //            if (chunks.Contains(bestMove))
        //            {
        //                chunks.Remove(bestMove); //try deleting it
        //                chunksLength -= bestMove.Length;
        //            }
        //            else
        //            {
        //                chunks.Add(bestMove); //try adding it
        //                chunksLength += bestMove.Length;
        //            }

        //            foreach (string example in substringMap[bestMove])
        //            {
        //                int cc = MinChunkCount(example, chunks);
        //                totalSegments += ((int)examples[example]) * (cc - chunksRequired[example]);
        //                chunksRequired[example] = cc;
        //            }
        //        }
        //    }
        //}

        public static void Compress(SparseDoubleVector<string> examples)
        {
            InternDictionary<string> internTable = new InternDictionary<string>();

            //build a map from substrings to words
            Dictionary<string, List<string>> substringMap = new Dictionary<string, List<string>>();
            foreach (string example in examples.Keys)
                foreach (string ngram in GetNgrams(example))
                {
                    List<string> wordList;
                    if (!substringMap.TryGetValue(ngram, out wordList))
                        wordList = substringMap[ngram] = new List<string>();

                    wordList.Add(example);
                }

            //initialize the chunk set
            Set<string> chunks = new Set<string>();
            foreach (string ngram in substringMap.Keys)
                if (ngram.Length == 1) chunks.Add(ngram);

            Dictionary<string, int> chunksRequired = new Dictionary<string, int>(examples.Count);
            int totalSegments = 0;
            foreach (string example in examples.Keys)
                totalSegments += ((int)examples[example]) * (chunksRequired[example] = MinChunkCount(example, chunks));

            int chunksLength = chunks.Count;

            double currentScore = totalSegments * Math.Log(chunks.Count, 2) + 8*(chunksLength+chunks.Count); //initial score

            Console.WriteLine("Initial score = " + currentScore + "; " + totalSegments + " segments; " + chunks.Count + " chunks.");

            int round = 0;

            while (true)
            {
                round++;

                double bestScore = currentScore;
                string bestMove = null;
                //Set<string> chunksCopy = new Set<string>(chunks);
                foreach (string ngram in examples.Keys)
                {
                    if (chunks.Contains(ngram))      
                    {              
                        chunks.Remove(ngram); //try deleting it
                        chunksLength-=ngram.Length;
                    }
                    else
                    {
                        chunks.Add(ngram); //try adding it
                        chunksLength+=ngram.Length;
                    }

                    int chunkChange = 0;
                    bool impossible = false;
                    foreach (string example in substringMap[ngram])                    
                    {
                        int cc = MinChunkCount(example,chunks);
                        if (cc == int.MaxValue)
                        {
                            impossible=true;
                            break;
                        }
                        chunkChange += ((int)examples[example]) * (cc - chunksRequired[example]);
                    }
                    
                    if (!impossible)
                    {
                        double newScore = (totalSegments + chunkChange) * Math.Log(chunks.Count, 2) + 8* (chunksLength+chunks.Count);
                        if (newScore < bestScore)
                        {
                            bestScore = newScore;
                            bestMove = ngram;
                        }
                    }

                    if (chunks.Contains(ngram))      
                    {              
                        chunks.Remove(ngram); //try deleting it
                        chunksLength-=ngram.Length;
                    }
                    else
                    {
                        chunks.Add(ngram); //try adding it
                        chunksLength+=ngram.Length;
                    }
                        
                    
                }

                if (bestMove == null)
                {
                    Console.WriteLine("Finished.  Local max found.  Return to quit.");
                    SaveSet(chunks, @"C:\Data\WikiTransliteration\Segmentation\chunks.txt");
                    Console.ReadLine();
                    return;
                }
                else
                {
                    Console.WriteLine("Compressing (round " + round + "): Old score = " + currentScore + "; new score = " + bestScore);
                    Console.WriteLine("Segments per word: " + ( ((double)totalSegments)/examples.Count) + "; " + totalSegments + " segments; " + chunks.Count + " chunks (length = " + chunksLength + ")");
                    if (!chunks.Contains(bestMove)) Console.WriteLine("Adding " + bestMove);
                    else Console.WriteLine("Removing " + bestMove);
                    Console.WriteLine();

                    currentScore = bestScore;

                    if (chunks.Contains(bestMove))
                    {
                        chunks.Remove(bestMove); //try deleting it
                        chunksLength -= bestMove.Length;
                    }
                    else
                    {
                        chunks.Add(bestMove); //try adding it
                        chunksLength += bestMove.Length;
                    }

                    foreach (string example in substringMap[bestMove])
                    {
                        int cc = MinChunkCount(example, chunks);                        
                        totalSegments += ((int)examples[example]) * (cc - chunksRequired[example]);
                        chunksRequired[example] = cc;
                    }
                }
            }
        }
    }
}
