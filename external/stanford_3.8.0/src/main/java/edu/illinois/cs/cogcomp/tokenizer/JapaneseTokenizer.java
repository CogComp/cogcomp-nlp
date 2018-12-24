/**
 * 
 */
package edu.illinois.cs.cogcomp.tokenizer;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

import com.atilika.kuromoji.ipadic.Token;

/**
 * This tokenizer is a wrapper for the open source kuromoji tokenizer. Sentences
 * are split on the Japanese sentence terminator, as well as some other english 
 * punctuations.
 * @author redman
 */
public class JapaneseTokenizer implements Tokenizer {
    
    /** this is the kuromoji tokenizer. NOT cogcomp tokenizer. */
    private com.atilika.kuromoji.ipadic.Tokenizer tokenizer;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        JapaneseTokenizer jt = new JapaneseTokenizer();
        String text = "\"ペンシルベニアドイツ語\",\"text\":\"ペンシルベニアドイツ語（標準ドイ"
                        + "ツ語：Pennsylvania-Dutch, Pennsilfaani-Deitsch、アレマン語：Pennsylvania-Ditsch、英語：Pennsylvania-German）"
                        + "は、北アメリカのカナダおよびアメリカ中西部でおよそ15万から25万人の人びとに話されているドイツ語の系統である。高地ドイツ語の"
                        + "うち上部ドイツ語の一派アレマン語の一方言である。ペンシルベニアアレマン語(Pennsilfaani-Alemanisch, Pennsylvania-Alemannic)"
                        + "とも呼ばれる。";
        Tokenization tokenized = jt.tokenizeTextSpan(text);
        
        // DEBUG print the results
        int tokIdx = 0;
        int[] sentenceEnds = tokenized.getSentenceEndTokenIndexes();
        String[] toks = tokenized.getTokens();
        IntPair [] charOffsetArray = tokenized.getCharacterOffsets();
        System.out.println("HOLY CRAP");
        for (int sentIdx = 0; sentIdx < sentenceEnds.length; sentIdx++) {
            
            // print the sentence.
            for (int tokoff = tokIdx; tokoff < sentenceEnds[sentIdx]; tokoff++)
                System.out.print(toks[tokoff]);
            System.out.println();
            for (; tokIdx < sentenceEnds[sentIdx]; tokIdx++)
                System.out.println(toks[tokIdx]+" = "+text.substring(charOffsetArray[tokIdx].getFirst(),charOffsetArray[tokIdx].getSecond()));
        }
    }

    /**
     * instantiate the kuromoji tokenizer here.
     */
    public JapaneseTokenizer() {
        tokenizer = new com.atilika.kuromoji.ipadic.Tokenizer();
    }
    
    /** 
     * This method will tokenize the sentences first, then will tokenize each sentence
     * separately. Sentence spliting is done using a simple regex including the 
     * japenese sentence ending denotation, 
     */
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentence) {
        if(sentence.trim().isEmpty())
            return new Pair<String[], IntPair[]>(new String[0], new IntPair[0]);
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<IntPair> offsets = new ArrayList<>();
        List<Token> ktokens = tokenizer.tokenize(sentence);
        for (Token t : ktokens) {
            String surfaceForm = t.getSurface();
            if (surfaceForm.trim().length() != 0) {
                tokens.add(surfaceForm);
                int start = t.getPosition();
                IntPair tokenBoundry = new IntPair(start, start + surfaceForm.length());
                offsets.add(tokenBoundry);
            }
        }
        return new Pair<String[],IntPair[]>(
                        tokens.toArray(new String[tokens.size()]),
                        offsets.toArray(new IntPair[tokens.size()]));
    }

    /**
     * The text annotation requires all tokens and sentences to be references in terms of their
     * offsets from the start of the text they represent. This method will first identify and record
     * the sentence offsets, it will then use the kuromoji tokenizer to tokenize words within
     * sentences.
     */
    @Override
    public Tokenization tokenizeTextSpan(String textSpan) {
        if(textSpan.trim().isEmpty())
            return null;
        
        // first identify sentence ends, as required by TextAnnotation.
        ArrayList<Integer> lastTokenIndexes = new ArrayList<>();
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<IntPair> tokenBoundries = new ArrayList<>();
        int lastCharPosition = 0;
        int tokenCounter = 0;
        for (int i = 0; i < textSpan.length(); i++) {
            switch (textSpan.charAt(i)) {
                case '。':
                case '！':
                case '？':
                case '．':
                case '；':
                    // found the end of a sentence, process and convert to global
                    Pair<String[], IntPair[]> tokenDefs = this.tokenizeSentence(textSpan.substring(lastCharPosition, i+1));
                    
                    // adjust all the offsets, increment the IntPair by the sentence starting char offset.
                    int which = 0;
                    for (IntPair ip : tokenDefs.getSecond()) {
                        ip.setFirst(ip.getFirst()+lastCharPosition);
                        ip.setSecond(ip.getSecond()+lastCharPosition);
                        
                        // add our token, and it's offset to the arrays.
                        tokens.add(tokenDefs.getFirst()[which++]);
                        tokenBoundries.add(ip);
                    }
                    tokenCounter += tokenDefs.getFirst().length;
                    lastTokenIndexes.add(tokenCounter);
                    lastCharPosition = i+1;
                    break;
            }
        }
        
        // if there is dangling text, just add it to the next sentence, we force a sentence termination at end of text
        if (lastCharPosition != textSpan.length()) {
            // found the end of a sentence, process and convert to global
            Pair<String[], IntPair[]> tokenDefs = this.tokenizeSentence(textSpan.substring(lastCharPosition, textSpan.length()));
            
            // adjust all the offsets, increment the IntPair by the sentence starting char offset.
            int which = 0;
            for (IntPair ip : tokenDefs.getSecond()) {
                ip.setFirst(ip.getFirst()+lastCharPosition);
                ip.setSecond(ip.getSecond()+lastCharPosition);
                
                // add our token, and it's offset to the arrays.
                tokens.add(tokenDefs.getFirst()[which++]);
                tokenBoundries.add(ip);
            }
            tokenCounter += tokenDefs.getFirst().length;
            lastTokenIndexes.add(tokenCounter);
        }
        int[] sentenceEnds = new int[lastTokenIndexes.size()];
        int i = 0;
        for (Integer lastToken : lastTokenIndexes) {
            sentenceEnds[i++] = lastToken;
        }
        String[] toks = tokens.toArray(new String[tokens.size()]);
        IntPair[] charOffsetArray = tokenBoundries.toArray(new IntPair[tokenBoundries.size()]);
        Tokenization tokenized = new Tokenization(toks, charOffsetArray, sentenceEnds);
        return tokenized;
    }
}
