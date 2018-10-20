/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.utils.InternDictionary;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

class WordCompression {

    public static List<String> GetNgrams(String example) {
        HashMap<String, Boolean> ngramList = new HashMap<>();
        for (int n = 1; n <= example.length(); n++)
            for (int i = 0; i <= example.length() - n; i++) {
                ngramList.put(example.substring(i, n), true);
            }

        return new ArrayList<>(ngramList.keySet());
    }

    private static int MinChunkCount(String example, HashSet<String> chunks) {
        //jon
        int[] counts = new int[example.length() + 1];
        for (int i = example.length() - 1; i >= 0; i--) {
            counts[i] = Integer.MAX_VALUE;
            int maxLength = example.length() - i;
            for (int j = 1; j <= maxLength; j++) {
                if (chunks.contains(example.substring(i, j)))
                    counts[i] = Math.min(1 + counts[i + j], counts[i]);
            }
        }

        return counts[0];
    }

    private static String[] MinChunks(String example, HashSet<String> chunks) {
        //jon
        int[] counts = new int[example.length() + 1];
        String[] bestString = new String[example.length()];
        for (int i = example.length() - 1; i >= 0; i--) {
            counts[i] = Integer.MAX_VALUE;
            int maxLength = example.length() - i;
            for (int j = 1; j <= maxLength; j++) {
                String ss = example.substring(i, j);
                if (chunks.contains(ss)) {
                    int addCount = 1 + counts[i + j];
                    if (addCount < counts[i]) {
                        counts[i] = addCount;
                        bestString[i] = ss;
                    }
                }
            }
        }

        if (bestString[0] == null) return null;

        String[] result = new String[counts[0]];

        int nextOffset = 0;
        for (int i = 0; i < result.length; i++) {
            result[i] = bestString[nextOffset];
            nextOffset += result[i].length();
        }

        return result;
    }

//    public static void Compress(SparseDoubleVector<String> examples) {
//        InternDictionary<String> internTable = new InternDictionary<String>();
//
//        //build a map from substrings to words
//        HashMap<String, List<String>> substringMap = new HashMap<String, List<String>>();
//        for (String example : examples.Keys)
//            for (String ngram : GetNgrams(example)) {
//                List<String> wordList;
//                if (!substringMap.TryGetValue(ngram, out wordList))
//                    wordList = substringMap[ngram] = new List<String>();
//
//                wordList.Add(example);
//            }
//
//        //initialize the chunk set
//        HashSet<String> chunks = new HashSet<String>();
//        for (String ngram : substringMap.Keys)
//            if (ngram.Length == 1) chunks.Add(ngram);
//
//        HashMap<String, Integer> chunksRequired = new HashMap<String, int>(examples.Count);
//        int totalSegments = 0;
//        for (String example : examples.Keys)
//        totalSegments += ((int) examples[example]) * (chunksRequired[example] = MinChunkCount(example, chunks));
//
//        int chunksLength = chunks.size();
//
//        double currentScore = totalSegments * Math.Log(chunks.size(), 2) + 8 * (chunksLength + chunks.size()); //initial score
//
//        System.out.println("Initial score = " + currentScore + "; " + totalSegments + " segments; " + chunks.size() + " chunks.");
//
//        int round = 0;
//
//        while (true) {
//            round++;
//
//            double bestScore = currentScore;
//            String bestMove = null;
//            //Set<String> chunksCopy = new Set<String>(chunks);
//            for (String ngram : examples.Keys)
//            {
//                if (chunks.contains(ngram)) {
//                    chunks.Remove(ngram); //try deleting it
//                    chunksLength -= ngram.Length;
//                } else {
//                    chunks.Add(ngram); //try adding it
//                    chunksLength += ngram.Length;
//                }
//
//                int chunkChange = 0;
//                Boolean impossible = false;
//                for (String example : substringMap[ngram]) {
//                    int cc = MinChunkCount(example, chunks);
//                    if (cc == int.MaxValue) {
//                        impossible = true;
//                        break;
//                    }
//                    chunkChange += ((int) examples[example]) * (cc - chunksRequired[example]);
//                }
//
//                if (!impossible) {
//                    double newScore = (totalSegments + chunkChange) * Math.log(chunks.size(), 2) + 8 * (chunksLength + chunks.size());
//                    if (newScore < bestScore) {
//                        bestScore = newScore;
//                        bestMove = ngram;
//                    }
//                }
//
//                if (chunks.contains(ngram)) {
//                    chunks.remove(ngram); //try deleting it
//                    chunksLength -= ngram.Length;
//                } else {
//                    chunks.add(ngram); //try adding it
//                    chunksLength += ngram.Length;
//                }
//
//
//            }
//
//            if (bestMove == null) {
//                System.out.println("Finished.  Local max found.  Return to quit.");
//                SaveSet(chunks, @ "C:\Data\WikiTransliteration\Segmentation\chunks.txt");
//                Console.ReadLine();
//                return;
//            } else {
//                System.out.println("Compressing (round " + round + "): Old score = " + currentScore + "; new score = " + bestScore);
//                System.out.println("Segments per word: " + (((double) totalSegments) / examples.Count) + "; " + totalSegments + " segments; " + chunks.size() + " chunks (length = " + chunksLength + ")");
//                if (!chunks.contains(bestMove))
//                    System.out.println("Adding " + bestMove);
//                else System.out.println("Removing " + bestMove);
//                System.out.println();
//
//                currentScore = bestScore;
//
//                if (chunks.contains(bestMove)) {
//                    chunks.remove(bestMove); //try deleting it
//                    chunksLength -= bestMove.Length;
//                } else {
//                    chunks.add(bestMove); //try adding it
//                    chunksLength += bestMove.Length;
//                }
//
//                for (String example : substringMap[bestMove]) {
//                    int cc = MinChunkCount(example, chunks);
//                    totalSegments += ((int) examples[example]) * (cc - chunksRequired[example]);
//                    chunksRequired[example] = cc;
//                }
//            }
//        }
//    }
}

