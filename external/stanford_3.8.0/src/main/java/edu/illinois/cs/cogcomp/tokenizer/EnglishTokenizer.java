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
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * Created by ctsai12 on 12/13/15.
 */
public class EnglishTokenizer implements Tokenizer {

//    private TextAnnotationBuilder tab;
    private TokenizerTextAnnotationBuilder tab;
    public EnglishTokenizer(){
        tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
    }

    public TextAnnotation getTextAnnotation(String text){
        throw new IllegalStateException( "USE TokenizerTextAnnotationBuilder( StatefulTokenizer) instead." );
//        TextAnnotation ta = null;
//        try {
//            ta = tab.createTextAnnotation(text);
//        } catch (Exception e){
//            System.out.println(text);
//        }
//
//        return ta;
    }

    /**
     * given a sentence, return a set of tokens and their character offsets
     *
     * @param sentence The sentence string
     * @return A {@link Pair} containing the array of tokens and their character offsets
     */
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentence) {
        return null;
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
        return null;
    }
}
