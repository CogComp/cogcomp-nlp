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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ctsai12 on 3/24/16.
 */
public class ThaiTokenizer implements Tokenizer {

    public ThaiTokenizer(){

    }

    public static void main(String[] args) {

        String text = "สตาร์คราฟต์   เป็นวิดีโอเกมประเภทวางแผนเรียลไทม์และบันเทิงคดีวิทยาศาสตร์การทหาร พัฒนาและจัดจำหน่ายโดยบลิซซาร์ด เอ็นเตอร์เทนเมนต์ ออกบนระบบปฏิบัติการไมโครซอฟท์ วินโดวส์เมื่อวันที่ 31 มีนาคม 2541 ต่อมา เกมขยายเป็นแฟรนไชส์ และเป็นเกมแรกของซีรีส์สตาร์คราฟต์ รุ่นแมคโอเอสออกในเดือนมีนาคม 2542 และรุ่นดัดแปลงนินเทนโด 64 ซึ่งพัฒนาร่วมกับแมสมีเดีย ออกในวันที่ 13 มิถุนายน 2543 การพัฒนาเกมนี้เริ่มขึ้นไม่นานหลังวอร์คราฟต์ 2: ไทด์สออฟดาร์กเนส ออกในปี 2538 สตาร์คราฟต์เปิดตัวในงานอี3 ปี 2539 ซึ่งเป็นที่ชื่นชอบน้อยกว่าวอร์คราฟต์ 2 ฉะนั้น โครงการจึงถูกพลิกโฉมทั้งหมดแล้วแสดงต่อสาธารณะในต้นปี 2540 ซึ่งได้รับการตอบรับดีกว่ามาก";
        text = "    2507  การสืบสวนของคณะกรรมการสมาชิกผู้แทนราษฎรสหรัฐว่าด้วยการลอบสังหารประธานาธิบดี (hsca) ระหว่าง - พศ 2522  และการสืบสวนของรัฐบาล สรุปว่าประธานาธิบดีถูกลอบสังหารโดยลี ฮาร์วีย์ ออสวอลด์ ซึ่งในเวล\n";
        ThaiTokenizer token = new ThaiTokenizer();
        TextAnnotation ta = token.getTextAnnotation(text);
        for(Sentence sen: ta.sentences()){
            System.out.println(sen.getTokenizedText());
        }

    }

    public TextAnnotation getTextAnnotation(String text) {
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        BreakIterator boundary = BreakIterator.getWordInstance(new Locale("th", "TH", "TH"));
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
//            System.out.println(start+" "+end+" "+text.length());
            String sur = text.substring(start, end);
            if(sur.trim().isEmpty()){
//                if(surfaces.size()>0)
//                    sen_ends.add(surfaces.size());
                continue;
            }
            surfaces.add(sur);
            offsets.add(new IntPair(start, end));
        }
        if(surfaces.size()>0 && (sen_ends.size() == 0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size()))
            sen_ends.add(surfaces.size());

        IntPair[] offs = new IntPair[offsets.size()];
        offs = offsets.toArray(offs);
        String[] surfs = new String[surfaces.size()];
        surfs = surfaces.toArray(surfs);
        int[] ends = new int[sen_ends.size()];
        for(int i = 0; i < sen_ends.size(); i++)
            ends[i] = sen_ends.get(i);

//        System.out.println(text);
//        System.out.println(offsets);
//        System.out.println(sen_ends);
        TextAnnotation ta = new TextAnnotation("", "", text, offs,
                surfs, ends);
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
        List<IntPair> offsets = new ArrayList<>();
        List<String> surfaces = new ArrayList<>();
        List<Integer> sen_ends = new ArrayList<>();
        BreakIterator boundary = BreakIterator.getWordInstance(new Locale("th", "TH", "TH"));
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
//            System.out.println(start+" "+end+" "+text.length());
            String sur = text.substring(start, end);
            if(sur.trim().isEmpty()){
//                if(surfaces.size()>0)
//                    sen_ends.add(surfaces.size());
                continue;
            }
            surfaces.add(sur);
            offsets.add(new IntPair(start, end));
        }
        if(surfaces.size()>0 && (sen_ends.size() == 0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size()))
            sen_ends.add(surfaces.size());

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
        BreakIterator boundary = BreakIterator.getWordInstance(new Locale("th", "TH", "TH"));
        boundary.setText(textSpan);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
//            System.out.println(start+" "+end+" "+text.length());
            String sur = textSpan.substring(start, end);
            if(sur.trim().isEmpty()){
//                if(surfaces.size()>0)
//                    sen_ends.add(surfaces.size());
                continue;
            }
            surfaces.add(sur);
            offsets.add(new IntPair(start, end));
        }
        if(surfaces.size()>0 && (sen_ends.size() == 0 || sen_ends.get(sen_ends.size()-1)!=surfaces.size()))
            sen_ends.add(surfaces.size());

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
