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
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by ctsai12 on 12/7/15.
 */
public class ChineseTokenizer implements Tokenizer {

    CRFClassifier<CoreLabel> segmenter;
    private Map<String, String> t2s;
    public ChineseTokenizer( String basedir ){
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", basedir);
        props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");
        segmenter = new CRFClassifier<>(props);
        segmenter.loadClassifierNoExceptions(basedir + "/ctb.gz", props);
        loadConversionMap();
    }

    public static boolean containsHanScript(String s) {
        return s.codePoints().anyMatch(
                codepoint -> Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
    }

    public static void main(String[] args) {

        String line = "   面对新世纪，  世界各国人民的共同愿望是：继续发展人类以往创造的一切文明成果。克服20世纪困扰着人类的战争和贫困问题，推进和平与发展的崇高事业，创造一个美好的世界。";
//        line = "2006年大西洋颶風季時間軸中記錄有全年大西洋盆地所有熱帶和亞熱帶氣旋形成、增強、減弱、登陸、轉變成溫帶氣旋以及消散的具體信息。2006年大西洋颶風季於2006年6月1日正式開始，同年11月30日結束，傳統上這樣的日期界定了一年中絕大多數熱帶氣旋在大西洋形成的時間段，這一颶風季是繼2001年大西洋颶風季以來第一個沒有任何一場颶風在美國登陸的大西洋颶風季，也是繼1994年大西洋颶風季以來第一次在整個十月份都沒有熱帶氣旋形成。美國國家颶風中心每年都會對前一年颶風季的所有天氣系統進行重新分析，並根據結果更新其風暴資料庫，因此時間軸中還包括實際操作中沒有發布的信息。包括最大持續風速、位置、距離在內的所有數字都是經四捨五入換算成整數。";
        line = "巴拉克 歐巴馬";
        line = "ab-cde";
//        line = "在古巴的美国代表机构是由哈瓦那的United States Interests Section（美国利益科）代理，在美国首都华盛顿有一个类似的Cuban Interests Section（古巴利益科），其则是瑞士大使馆的组成部分。";

        System.out.println(containsHanScript(line));
        String basedir = "/shared/experiments/ctsai12/workspace/stanford-segmenter-2015-04-20/data/";

        ChineseTokenizer ct = new ChineseTokenizer(basedir);
        TextAnnotation ta = ct.getTextAnnotation1(line);
        for(String t: ta.getTokens())
            System.out.println(t);

//        int tid = ta.getTokenIdFromCharacterOffset(5);
//        System.out.println("token id "+tid);
//        System.out.println("token: "+ta.getToken(tid));
//        IntPair offs = ta.getTokenCharacterOffset(tid);
//        System.out.println("start: "+offs.getFirst());
//        System.out.println("edn: "+offs.getSecond());

    }

    private void loadConversionMap(){
        try {
            ArrayList<String> lines = LineIO.read("/shared/bronte/ctsai12/multilingual/2015data/t2s");
            t2s = new HashMap<>();
            for(String line: lines){
                String[] tokens = line.split("\t");
                if(tokens[0].length()==1 && tokens[1].length()==1 && tokens[0]!=tokens[1])
                    t2s.put(tokens[0], tokens[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String trad2simp(String text){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < text.length(); i++){
            String c = text.substring(i, i+1);
            if(t2s.containsKey(c)) {
                sb.append(t2s.get(c));
            }
            else
                sb.append(c);

            if(sb.length()!=i+1) {
                System.out.println(c.length());
                System.out.println(t2s.get(c).length());
                System.out.println(sb.length() + " " + i);
                System.exit(-1);
            }
        }
        return sb.toString();
    }

    public TextAnnotation oldGetTextAnnotation(String text){

        if(text.trim().isEmpty()) return null;

//        text = trad2simp(text);
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        String[] lines = text.split("。");
        int idx = 0;
        for(int i = 0; i < lines.length; i++){
            String line = lines[i];
            List<String> segs = segmenter.segmentString(line);
            for(String seg: segs){
                if(seg.length()>1 && seg.endsWith("人")){
                    surfaces.add(seg.substring(0,seg.length()-1));
                    idx = text.indexOf(seg, idx);
                    offsets.add(new IntPair(idx, idx+seg.length()-1));

                    surfaces.add(seg.substring(seg.length()-1, seg.length()));
                    offsets.add(new IntPair(idx+seg.length()-1, idx+seg.length()));

                    idx += seg.length();
                }
                else
                {
                    surfaces.add(seg);
                    idx = text.indexOf(seg, idx);
                    offsets.add(new IntPair(idx, idx + seg.length()));
                    idx += seg.length();
                }
            }
            if(i < lines.length-1){
                surfaces.add("。");
                idx = text.indexOf("。", idx);
                offsets.add(new IntPair(idx, ++idx));
            }
            sen_ends.add(surfaces.size());
        }

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);
        int[] ends = new int[sen_ends.size()];
        for(int i = 0; i < sen_ends.size(); i++)
            ends[i] = sen_ends.get(i);
        if(surfs.length == 0) return null;
        TextAnnotation ta = new TextAnnotation("", "", text, offs,
                surfs, ends);
        return ta;
    }

    public TextAnnotation getTextAnnotation1(String text){

        if(text.trim().isEmpty()) return null;

        text = trad2simp(text);
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        String[] lines = text.split("\n");
        int idx = 0;
        for(String line: lines){
            if(line.trim().isEmpty()) continue;

            String[] sentences = line.split("。");
            for(int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i];
                if(sentence.trim().isEmpty()) continue;
                List<String> segs = segmenter.segmentString(sentence);
                for (String seg : segs) {
                    idx = text.indexOf(seg, idx);
                    if (!containsHanScript(seg)) {
                        surfaces.add(seg);
                        offsets.add(new IntPair(idx, idx + seg.length()));
                    } else {
                        for (int j = 0; j < seg.length(); j++) {
                            String ch = seg.substring(j, j + 1);
                            surfaces.add(ch);
                            offsets.add(new IntPair(idx + j, idx + j + 1));
                        }
                    }
                    idx += seg.length();
                }
                if(i < sentences.length-1){
                    surfaces.add("。");
                    idx = text.indexOf("。", idx);
                    offsets.add(new IntPair(idx, ++idx));
                }
                if(sen_ends.size()==0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size())
                    sen_ends.add(surfaces.size());
            }
        }

//        for(int i = 0; i < surfaces.size(); i++){
//            System.out.println(i+" "+surfaces.get(i)+" "+offsets.get(i));
//        }
//        System.out.println(sen_ends);
//        System.out.println(surfaces.size()+" "+offsets.size()+" "+sen_ends.size());

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);
        int[] ends = new int[sen_ends.size()];
        for(int i = 0; i < sen_ends.size(); i++)
            ends[i] = sen_ends.get(i);
        if(surfs.length == 0) return null;
        TextAnnotation ta = new TextAnnotation("", "", text, offs, surfs, ends);
        return ta;
    }

    /**
     * given a sentence, return a set of tokens and their character offsets
     *
     * @param text The sentence string
     * @return A {@link Pair} containing the array of tokens and their character offsets
     */
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String text) {
        if(text.trim().isEmpty())
            return new Pair(new String[]{}, new IntPair[]{});

        text = trad2simp(text);
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        String[] lines = text.split("\n");
        int idx = 0;
        for(String line: lines){
            if(line.trim().isEmpty()) continue;

            String[] sentences = line.split("。");
            for(int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i];
                if(sentence.trim().isEmpty()) continue;
                List<String> segs = segmenter.segmentString(sentence);
                for (String seg : segs) {
                    idx = text.indexOf(seg, idx);
                    if (!containsHanScript(seg)) {
                        surfaces.add(seg);
                        offsets.add(new IntPair(idx, idx + seg.length()));
                    } else {
                        for (int j = 0; j < seg.length(); j++) {
                            String ch = seg.substring(j, j + 1);
                            surfaces.add(ch);
                            offsets.add(new IntPair(idx + j, idx + j + 1));
                        }
                    }
                    idx += seg.length();
                }
                if(i < sentences.length-1){
                    surfaces.add("。");
                    idx = text.indexOf("。", idx);
                    offsets.add(new IntPair(idx, ++idx));
                }
            }
        }

//        for(int i = 0; i < surfaces.size(); i++){
//            System.out.println(i+" "+surfaces.get(i)+" "+offsets.get(i));
//        }
//        System.out.println(sen_ends);
//        System.out.println(surfaces.size()+" "+offsets.size()+" "+sen_ends.size());

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
     * @param text
     */
    @Override
    public Tokenization tokenizeTextSpan(String text) {

        if(text.trim().isEmpty())
            return new Tokenization(new String[]{}, new IntPair[]{}, new int[]{});

        text = trad2simp(text);
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        String[] lines = text.split("\n");
        int idx = 0;
        for(String line: lines){
            if(line.trim().isEmpty()) continue;

            String[] sentences = line.split("。");
            for(int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i];
                if(sentence.trim().isEmpty()) continue;
                List<String> segs = segmenter.segmentString(sentence);
                for (String seg : segs) {
                    idx = text.indexOf(seg, idx);
                    if (!containsHanScript(seg)) {
                        surfaces.add(seg);
                        offsets.add(new IntPair(idx, idx + seg.length()));
                    } else {
                        for (int j = 0; j < seg.length(); j++) {
                            String ch = seg.substring(j, j + 1);
                            surfaces.add(ch);
                            offsets.add(new IntPair(idx + j, idx + j + 1));
                        }
                    }
                    idx += seg.length();
                }
                if(i < sentences.length-1){
                    surfaces.add("。");
                    idx = text.indexOf("。", idx);
                    offsets.add(new IntPair(idx, ++idx));
                }
                if(sen_ends.size()==0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size())
                    sen_ends.add(surfaces.size());
            }
        }

//        for(int i = 0; i < surfaces.size(); i++){
//            System.out.println(i+" "+surfaces.get(i)+" "+offsets.get(i));
//        }
//        System.out.println(sen_ends);
//        System.out.println(surfaces.size()+" "+offsets.size()+" "+sen_ends.size());

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);
        int[] ends = new int[sen_ends.size()];
        for(int i = 0; i < sen_ends.size(); i++)
            ends[i] = sen_ends.get(i);

        return new Tokenization(surfs, offs, ends);
    }
}
