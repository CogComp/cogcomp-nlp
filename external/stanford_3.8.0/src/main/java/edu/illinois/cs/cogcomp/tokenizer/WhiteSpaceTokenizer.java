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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ctsai12 on 11/29/15.
 */
public class WhiteSpaceTokenizer implements Tokenizer {

    public WhiteSpaceTokenizer(){

    }

    public static void main(String[] args) {
        String text = "  An aifi   Barack Hussain Obama ranar.... 4 ga watan Oguster na shekara ta 1961 a birnin n Honolulu dake yankin Hawai a ƙasar Amurika. Tun yana ɗan shekaru biyu da aihuwa iyayensa suka rabu, ya cigaba da zama tare da ma´aifiyarsa. Ubansa musulmi ƙan kasar Kenya a nahiyar Afirka, ya bar Amurika bayan ya rabu da matarsa ba´amurka inda ya koma gida Kenya, ya kuma zama minister a cikin gwamnatin shugaban ƙasa na farko, wato Jomo Keniyatta, kamin Allah ya amshi ransa, a shekara ta 1982 a cikin haɗarin mota.";
        WhiteSpaceTokenizer t = new WhiteSpaceTokenizer();
        TextAnnotation ta = t.getTextAnnotation(text);
        for(int i = 0; i < ta.getNumberOfSentences(); i++)
            System.out.println(ta.getSentence(i).getText());
//        SpanLabelView view = (SpanLabelView)ta.getView(ViewNames.TOKENS);
//        System.out.println(view.getConstituents().size());
//        for(Constituent c: view.getConstituents()){
//            System.out.println(c.getSurfaceForm());
//
//        }
//        System.out.println("#sen "+ta.getNumberOfSentences());
//        System.out.println(ta.getText());
//        System.out.println(ta.getTokensInSpan(3,4)[0]);
//        System.out.println(ta.getTokensInSpan(9,10)[0]);

    }

//    public TextAnnotation getTextAnnotation(String text){
//        text = text.replaceAll("\n", " ");
//        String[] sentences = text.split("\\.");
//        String new_text = "";
//        List<IntPair> offsets = new ArrayList<>();
//        List<String> surfaces = new ArrayList<>();
//        List<Integer> sen_ends = new ArrayList<>();
//        for(String sen: sentences){
//            String[] tokens = sen.trim().split("\\s+");
//            for(String token: tokens) {
//                offsets.add(new IntPair(text.length(), text.length()+token.length()));
//                surfaces.add(token);
//                new_text += token+" ";
//            }
//            sen_ends.add(offsets.size());
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
//        TextAnnotation ta = new TextAnnotation("", "", text, offs,
//                surfs, ends);
//        return ta;
//
//    }

    public TextAnnotation getTextAnnotation(String text){
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        String t = "";
        int t_start = -1;
        int i;
        for(i = 0; i < text.length(); i++){
            String c = text.substring(i, i+1);
            if(c.trim().isEmpty()){
                if(!t.isEmpty()){
                    surfaces.add(t);
                    offsets.add(new IntPair(t_start, i));
                    t = "";
                }
            }
            else if(c.equals(".") || c.equals("\n")){
                if(!t.isEmpty()){
                    surfaces.add(t);
                    offsets.add(new IntPair(t_start, i));
                }
                surfaces.add(c);
                offsets.add(new IntPair(i, i+1));
                t = "";
                sen_ends.add(surfaces.size());
            }
            else{
                if(t.isEmpty())
                    t_start = i;
                t+=c;
            }
        }
        if(!t.isEmpty()){
            surfaces.add(t);
            offsets.add(new IntPair(t_start, i));
            sen_ends.add(surfaces.size());
        }
        if(sen_ends.size() == 0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size()){
            sen_ends.add(surfaces.size());
        }

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);
        int[] ends = new int[sen_ends.size()];
        for(i = 0; i < sen_ends.size(); i++)
            ends[i] = sen_ends.get(i);


        if(ends[ends.length-1]!=surfaces.size()) {
            System.out.println(ends[ends.length - 1]);
            System.out.println(surfaces.size());
            System.exit(-1);
        }
        if(offs.length == 0 || surfs.length == 0)
            return null;
        TextAnnotation ta = new TextAnnotation("", "", text, offs,
                surfs, ends);
        return ta;
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
        String t = "";
        int t_start = -1;
        int i;
        for(i = 0; i < sentence.length(); i++){
            String c = sentence.substring(i, i+1);
            if(c.trim().isEmpty()){
                if(!t.isEmpty()){
                    surfaces.add(t);
                    offsets.add(new IntPair(t_start, i));
                    t = "";
                }
            }
            else if(c.equals(",") || c.equals("\"") || c.equals("'") || c.equals("(") || c.equals(")") || c.equals(";") || c.equals(":")){
                if(!t.isEmpty()){
                    surfaces.add(t);
                    offsets.add(new IntPair(t_start, i));
                }
                surfaces.add(c);
                offsets.add(new IntPair(i, i+1));
                t = "";
            }
            else{
                if(t.isEmpty())
                    t_start = i;
                t+=c;
            }
        }
        if(!t.isEmpty()){
            surfaces.add(t);
            offsets.add(new IntPair(t_start, i));
        }

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);

        return new Pair(surfs, offs);
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
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        int i;
        int prev = 0;
        String prevc = null;
        for(i = 0; i < textSpan.length(); i++){
            String c = textSpan.substring(i, i+1);
            if((c.equals(".") && prevc!=null && !prevc.toUpperCase().equals(prevc)) || c.equals("\n")){
                String sentence = textSpan.substring(prev, i);
                if(!sentence.trim().isEmpty()) {
                    Pair<String[], IntPair[]> tokens = tokenizeSentence(sentence);
                    for (String token : tokens.getFirst())
                        surfaces.add(token);
                    for (IntPair offset : tokens.getSecond())
                        offsets.add(new IntPair(offset.getFirst() + prev, offset.getSecond() + prev));

                    if (c.equals(".")) {
                        surfaces.add(".");
                        offsets.add(new IntPair(i, i + 1));
                    }
                    sen_ends.add(surfaces.size());
                }
                prev = i+1;
            }
            prevc = c;
        }

        if(prev < textSpan.length() && !textSpan.substring(prev, textSpan.length()).trim().isEmpty()){
            Pair<String[], IntPair[]> tokens = tokenizeSentence(textSpan.substring(prev, textSpan.length()));
            for(String token: tokens.getFirst())
                surfaces.add(token);
            for(IntPair offset: tokens.getSecond())
                offsets.add(new IntPair(offset.getFirst()+prev, offset.getSecond()+prev));
            sen_ends.add(surfaces.size());
        }

        if(sen_ends.size() == 0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size()){
            sen_ends.add(surfaces.size());
        }

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);
        int[] ends = new int[sen_ends.size()];
        for(i = 0; i < sen_ends.size(); i++)
            ends[i] = sen_ends.get(i);
        return new Tokenization(surfs, offs, ends);
    }
}
