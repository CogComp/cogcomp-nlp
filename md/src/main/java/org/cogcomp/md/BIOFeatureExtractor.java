package org.cogcomp.md;

import org.cogcomp.md.LbjGen.*;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.MyString;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the FeatureExtractor Class for the lbj file
 * We define most of the features here
 * Each static function performs an extraction on a given target
 */
public class BIOFeatureExtractor {
    public static List<Pair<Integer, String>> getGazetteerFeatures(Constituent c){
        List<Pair<Integer, String>> ret_features = new ArrayList<>();
        View bioView = c.getTextAnnotation().getView("BIO");
        for (int i = -1 ; i < 3; i++){
            int curId = c.getStartSpan() + i;
            if (curId < 0 || curId >= bioView.getEndSpan()){
                continue;
            }
            Constituent cCur = bioView.getConstituentsCoveringToken(c.getStartSpan() + i).get(0);
            if (cCur != null){
                String[] features = cCur.getAttribute("GAZ").split(",");
                for (String f : features){
                    if (f == null){
                        continue;
                    }
                    ret_features.add(new Pair<>(i, f));
                }
            }
        }

        return ret_features;
    }

    public static List<String> getGazetteerFeaturesSingle(Constituent c){
        List<String> ret_features = new ArrayList<>();
        String[] features = c.getAttribute("GAZ").split(",");
        for (String f : features) {
            if (f == null) {
                continue;
            }
            ret_features.add(f);
        }
        return ret_features;
    }

    public static List<Pair<String, String>> getGazetteerFeaturesHead(Relation r){
        TextAnnotation ta = r.getTarget().getTextAnnotation();
        List<Pair<String, String>> ret_features = new ArrayList<>();
        for (int i = r.getTarget().getStartSpan(); i < r.getTarget().getEndSpan(); i++){
            String curGazFeature = r.getTarget().getAttribute("GAZ" + i);
            String[] features = curGazFeature.split(",");
            for (String f : features){
                if (f == null){
                    continue;
                }
                ret_features.add(new Pair<>(Integer.toString(i - r.getTarget().getStartSpan()), f));
            }
        }
        String phraseGazFeature = r.getTarget().getAttribute("GAZ");
        String[] features = phraseGazFeature.split(",");
        for (String f : features){
            if (f == null){
                continue;
            }
            ret_features.add(new Pair<>("PhraseGaz" , f));
        }
        return ret_features;
    }

    public static String isSentenceStart (Constituent c){
        Sentence sentence = c.getTextAnnotation().getSentenceFromToken(c.getStartSpan());
        int sentenceStart = sentence.getStartSpan();
        if (c.getStartSpan() == sentenceStart){
            return "1";
        }
        return "0";
    }

    public static List<Pair<Integer, String>> getWordFormFeatures (Constituent c){
        List<Pair<Integer, String>> ret_features = new ArrayList<>();
        TextAnnotation ta = c.getTextAnnotation();
        Sentence sentence = ta.getSentenceFromToken(c.getStartSpan());
        int sentenceStart = sentence.getStartSpan();
        int sentenceEnd = sentence.getEndSpan();
        if (c.getStartSpan() > sentenceStart){
            ret_features.add(new Pair<>(-1, ta.getToken(c.getStartSpan() - 1)));
            ret_features.add(new Pair<>(-1, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan() - 1))));
        }
        ret_features.add(new Pair<>(0, ta.getToken(c.getStartSpan())));
        ret_features.add(new Pair<>(0, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan()))));
        if (c.getEndSpan() < sentenceEnd){
            ret_features.add(new Pair<>(1, ta.getToken(c.getStartSpan() + 1)));
            ret_features.add(new Pair<>(1, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan() + 1))));
        }
        if (c.getEndSpan() < sentenceEnd - 1){
            ret_features.add(new Pair<>(2, ta.getToken(c.getStartSpan() + 2)));
            ret_features.add(new Pair<>(2, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan() + 2))));
        }
        return ret_features;
    }

    public static List<Pair<Integer, String>> getPOSFeatures (Constituent c) {
        List<Pair<Integer, String>> ret_features = new ArrayList<>();
        View posView = c.getTextAnnotation().getView(ViewNames.POS);
        for (int i = -1; i < 3; i++){
            int curId = c.getStartSpan() + i;
            if (curId < 0 || curId >= posView.getEndSpan()){
                continue;
            }
            ret_features.add(new Pair<>(i, posView.getConstituentsCoveringToken(curId).get(0).getLabel()));
        }
        return ret_features;
    }

    public static List<Pair<String, Boolean>> getWordTypeInformation(Constituent c){
        List<Pair<String, Boolean>> ret_features = new ArrayList<>();
        //View bioView = c.getTextAnnotation().getView("BIO");
        View bioView = c.getTextAnnotation().getView(ViewNames.TOKENS);
        for (int j = -1; j < 3; j++) {
            int curId = c.getStartSpan() + j;
            if (curId < 0 || curId >= bioView.getEndSpan()){
                continue;
            }
            Constituent cCur = bioView.getConstituentsCoveringToken(c.getStartSpan() + j).get(0);
            String form = cCur.toString();
            boolean allCapitalized = true, allDigits = true, allNonLetters = true;
            for (int i = 0; i < form.length(); i++) {
                char ch = form.charAt(i);
                allCapitalized &= Character.isUpperCase(ch);
                allDigits &= (Character.isDigit(ch) || ch == '.' || ch == ',');
                allNonLetters &= !Character.isLetter(ch);
            }
            ret_features.add(new Pair<>("c" + j, allCapitalized));
            ret_features.add(new Pair<>("d" + j, allDigits));
            ret_features.add(new Pair<>("p" + j, allNonLetters));
        }
        return ret_features;
    }

    public static List<Pair<Integer, String>> getBrownClusterPaths(Constituent c){
        List<Pair<Integer, String>> ret_features = new ArrayList<>();
        View bioView = c.getTextAnnotation().getView("BIO");
        for (int i = -1; i < 3; i++){
            int curId = c.getStartSpan() + i;
            if (curId < 0 || curId >= bioView.getEndSpan()){
                continue;
            }
            Constituent cCur = bioView.getConstituentsCoveringToken(c.getStartSpan() + i).get(0);
            String[] features = cCur.getAttribute("BC").split(",");
            for (String s : features) {
                if (s == null){
                    continue;
                }
                ret_features.add(new Pair<>(i, s));
            }
        }
        return ret_features;
    }

    public static List<String> getBrownClusterPathsSingle(Constituent c){
        List<String> ret_features = new ArrayList<>();
        String[] features = c.getAttribute("BC").split(",");
        for (String s : features) {
            if (s == null){
                continue;
            }
            ret_features.add(s);
        }
        return ret_features;
    }

    public static String isInPronounList(Constituent c){
        String form = c.toString().toLowerCase();
        List<String> pronouns = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("data/Pronouns"))) {
            String line;
            while ((line = br.readLine()) != null) {
                pronouns.add(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if (pronouns.contains(form)){
            return form;
        }
        else{
            return "";
        }
    }

    public static String getWordNetTags(WordNetManager wordnet, Constituent c){
        List<String> ret = null;
        try {
            ret = wordnet.getLexicographerFileNames(c.toString().toLowerCase(), true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        String retStr = "";
        for (String s : ret){
            retStr += s + ",";
        }
        return  retStr;
    }

    public static String getWordNetHyms(WordNetManager wordnet, Constituent c){
        List<String> ret = null;
        try {
            ret = wordnet.getHypernyms(c.toString().toLowerCase(), true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        String retStr = "";
        for (String s : ret){
            retStr += s + ",";
        }
        return  retStr;
    }
}

