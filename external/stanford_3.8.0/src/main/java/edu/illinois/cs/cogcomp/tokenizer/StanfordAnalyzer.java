/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.tokenizer;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by ctsai12 on 11/21/15.
 */
public class StanfordAnalyzer implements Tokenizer {
    StanfordCoreNLP pipeline;
    public StanfordAnalyzer(){
        FileInputStream input = null;
        Properties props = null;
        try {
            input = new FileInputStream("config/spanish.properties");
            props = new Properties();
            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pipeline = new StanfordCoreNLP(props);
    }

    public TextAnnotation getTextAnnotation(String text){
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreLabel> tokens = new ArrayList<>();
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        int[] sen_ends = new int[sentences.size()];
        int sen_idx = 0;
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                tokens.add(token);
            }
            sen_ends[sen_idx++] = tokens.size();
        }
        String[] surfaces = new String[tokens.size()];
        IntPair[] tokenCharOffsets = new IntPair[tokens.size()];
        for(int i = 0; i < tokens.size(); i++){

            surfaces[i] = tokens.get(i).originalText();
            tokenCharOffsets[i] = new IntPair(tokens.get(i).beginPosition(), tokens.get(i).endPosition());
//            System.out.println(surfaces[i]);
//            System.out.println(tokenCharOffsets[i]);
        }
//        System.out.println(sen_ends[0]);
        TextAnnotation ta = new TextAnnotation("", "", text, tokenCharOffsets,
                surfaces, sen_ends);
        return ta;
    }

    /**
     * given a sentence, return a set of tokens and their character offsets
     *
     * @param sentenceText The sentence string
     * @return A {@link Pair} containing the array of tokens and their character offsets
     */
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentenceText) {
        Annotation document = new Annotation(sentenceText);
        pipeline.annotate(document);

        List<CoreLabel> tokens = new ArrayList<>();
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        int[] sen_ends = new int[sentences.size()];
        int sen_idx = 0;
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                tokens.add(token);
            }
            sen_ends[sen_idx++] = tokens.size();
        }
        String[] surfaces = new String[tokens.size()];
        IntPair[] tokenCharOffsets = new IntPair[tokens.size()];
        for(int i = 0; i < tokens.size(); i++){

            surfaces[i] = tokens.get(i).originalText();
            tokenCharOffsets[i] = new IntPair(tokens.get(i).beginPosition(), tokens.get(i).endPosition());
//            System.out.println(surfaces[i]);
//            System.out.println(tokenCharOffsets[i]);
        }
        return new Pair(surfaces, tokenCharOffsets);
    }

    /**
     * given a span of text, return a list of Pair{@literal < String[], IntPair[] >} corresponding
     * to tokenized sentences, where the String[] is the ordered list of sentence tokens and the
     * IntPair[] is the corresponding list of character offsets with respect to <b>the original
     * text</b>.
     *
     * @param textSpan
     */
    @Override
    public Tokenization tokenizeTextSpan(String textSpan) {
        Annotation document = new Annotation(textSpan);
        pipeline.annotate(document);

        List<CoreLabel> tokens = new ArrayList<>();
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        int[] sen_ends = new int[sentences.size()];
        int sen_idx = 0;
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                tokens.add(token);
            }
            sen_ends[sen_idx++] = tokens.size();
        }
        String[] surfaces = new String[tokens.size()];
        IntPair[] tokenCharOffsets = new IntPair[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {

            surfaces[i] = tokens.get(i).originalText();
            tokenCharOffsets[i] = new IntPair(tokens.get(i).beginPosition(), tokens.get(i).endPosition());
//            System.out.println(surfaces[i]);
//            System.out.println(tokenCharOffsets[i]);
        }
        return new Tokenization(surfaces, tokenCharOffsets, sen_ends);
    }
}
