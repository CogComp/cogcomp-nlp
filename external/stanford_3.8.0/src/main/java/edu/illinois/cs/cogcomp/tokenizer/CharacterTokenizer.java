/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.tokenizer;

import com.github.stuxuhai.jpinyin.ChineseHelper;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ctsai12 on 11/29/15.
 */
public class CharacterTokenizer implements Tokenizer {

    public CharacterTokenizer(){

    }

    public static TextAnnotation getTextAnnotation(String text){

        TokenizerTextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new CharacterTokenizer());
        return tab.createTextAnnotation("default", "default", text);

//        text = ChineseHelper.convertToSimplifiedChinese(text);
//
//        List<IntPair> offsets = new ArrayList<>();
//        List<String> surfaces = new ArrayList<>();
//        List<Integer> sen_ends = new ArrayList<>();
//        for(int i = 0; i < text.length(); i++){
//            String c = text.substring(i, i+1).trim();
//            if(!c.isEmpty() && !c.equals("　")){
//                surfaces.add(c);
//                offsets.add(new IntPair(i, i+1));
//                if(c.equals(".") || c.equals("。") || c.equals("？") || c.equals("！"))
//                    sen_ends.add(surfaces.size());
//            }
//        }
//        if(sen_ends.size() == 0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size()){
//            sen_ends.add(surfaces.size());
//        }
//
//        IntPair[] offs = new IntPair[offsets.size()];
//        offs = offsets.toArray(offs);
//        String[] surfs = new String[surfaces.size()];
//        surfs = surfaces.toArray(surfs);
//        int[] ends = new int[sen_ends.size()];
//        for(int i = 0; i < sen_ends.size(); i++)
//            ends[i] = sen_ends.get(i);
//
//
//        if(ends[ends.length-1]!=surfaces.size()) {
//            System.out.println(ends[ends.length - 1]);
//            System.out.println(surfaces.size());
//            System.exit(-1);
//        }
//        if(offs.length == 0 || surfs.length == 0)
//            return null;
//        TextAnnotation ta = new TextAnnotation("", "", text, offs, surfs, ends);
//        return ta;
    }


    public static void main(String[] args) {
        String text = "  An aifi   Barack Hussain Obama ranar.... 4 ga watan Oguster na shekara ta 1961 a birnin n Honolulu dake yankin Hawai a ƙasar Amurika. Tun yana ɗan shekaru biyu da aihuwa iyayensa suka rabu, ya cigaba da zama tare da ma´aifiyarsa. Ubansa musulmi ƙan kasar Kenya a nahiyar Afirka, ya bar Amurika bayan ya rabu da matarsa ba´amurka inda ya koma gida Kenya, ya kuma zama minister a cikin gwamnatin shugaban ƙasa na farko, wato Jomo Keniyatta, kamin Allah ya amshi ransa, a shekara ta 1982 a cikin haɗarin mota.";
        text = "與chown命令不同，chgrp允許普通用戶改變文件所屬的組，只要該用戶是該組的一員。";
        System.out.println(ChineseHelper.convertToSimplifiedChinese(text));
//        CharacterTokenizer t = new CharacterTokenizer();
//        TextAnnotation ta = t.getTextAnnotation(text);
//        for(int i = 0; i < ta.getNumberOfSentences(); i++) {
//            System.out.println(ta.getSentence(i).getText());
//            System.out.println(Arrays.asList(ta.getTokens()));
//        }
//        SpanLabelView view = (SpanLabelView)ta.getView(ViewNames.TOKENS);
//        System.out.println(view.getConstituents().size());
//        for(Constituent c: view.getConstituents()){
//            System.out.println(c.getSurfaceForm());

//        }
//        System.out.println("#sen "+ta.getNumberOfSentences());
//        System.out.println(ta.getText());
//        System.out.println(ta.getTokensInSpan(3,4)[0]);
//        System.out.println(ta.getTokensInSpan(9,10)[0]);

    }

    /**
     * given a sentence, return a set of tokens and their character offsets
     *
     * @param sentence The sentence string
     * @return A {@link Pair} containing the array of tokens and their character offsets
     */
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentence) {
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        for(int i = 0; i < sentence.length(); i++){
            String c = sentence.substring(i, i+1).trim();
            if(!c.isEmpty() && !c.equals("　")){
                surfaces.add(c);
                offsets.add(new IntPair(i, i+1));
            }
        }
        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);

        return new Pair<>(surfs, offs);
    }

    /**
     * given a span of text, return a list of Pair{@literal < String[], IntPair[] >} corresponding
     * to tokenized sentences, where the String[] is the ordered list of sentence tokens and the
     * IntPair[] is the corresponding list of character offsets with respect to <b>the original
     * text</b>.
     *
     * @param text text to tokenize
     */
    @Override
    public Tokenization tokenizeTextSpan(String text) {
        text = ChineseHelper.convertToSimplifiedChinese(text);

        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        for(int i = 0; i < text.length(); i++){
            String c = text.substring(i, i+1).trim();
            if(!c.isEmpty() && !c.equals("　")){
                surfaces.add(c);
                offsets.add(new IntPair(i, i+1));
                if(c.equals(".") || c.equals("。") || c.equals("？") || c.equals("！"))
                    sen_ends.add(surfaces.size());
            }
        }
        if(sen_ends.size() == 0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size()){
            sen_ends.add(surfaces.size());
        }

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);
        int[] ends = new int[sen_ends.size()];
        for(int i = 0; i < sen_ends.size(); i++)
            ends[i] = sen_ends.get(i);


        if(ends[ends.length-1]!=surfaces.size()) {
            System.out.println(ends[ends.length - 1]);
            System.out.println(surfaces.size());
            throw new IllegalStateException( "sentence ends don't make sense... ends: " + ends[ends.length-1] +
                ", surfaces: " + surfaces.size() );
        }
        return new Tokenization(surfs, offs, ends);
    }
}
