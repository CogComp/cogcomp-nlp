/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.io;

import edu.illinois.cs.cogcomp.bigdata.lucene.Lucene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.util.CharArraySet;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Utilities for interfacing different sources of stop words
 * definitions
 * @author cheng88
 *
 */
public class StopWords implements Predicate<String>{
    
    private final Set<Object> stopWords;
    
    private static final List<String> words = new ArrayList<String>();
//    static{
//        try {
//            words.addAll(IOUtils.readLines(StopWords.class.getResourceAsStream("stopwords_big")));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public static final Set<String> DEFAULT_BIG_STOPWORDS = new HashSet<String>(words);
    public static final CharArraySet DEFAULT_BIG_LUCENE_STOPS = CharArraySet.copy(DEFAULT_BIG_STOPWORDS);
    
    /**
     * Constructs the default stop words set
     */
    public StopWords(){
        this(DEFAULT_BIG_STOPWORDS);
    }

    public StopWords(Set<?> stopSet){
        stopWords = new HashSet<Object>(stopSet);
    }
    
    public StopWords(String filename) {
        stopWords = new HashSet<Object>();
        InFile in = new InFile(filename);
        List<String> words = in.readLineTokens();
        while (words != null) {
            for (String word : words) {
                stopWords.add(word.toLowerCase());
            }
            words = in.readLineTokens();
        }
    }

    public boolean isStopword(String s) {
        return stopWords.contains(s);
    }

    public Iterable<String> filter(Iterable<String> words) {
        if (words == null)
            return null;
        return Iterables.filter(words, this);
    }
    
    public boolean apply(String input){
        return isStopword(input);
    }
    
}
